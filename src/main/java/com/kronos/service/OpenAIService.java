package com.kronos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kronos.dto.response.ResponseDTOs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String openaiUrl;

    @Value("${openai.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String SYSTEM_PROMPT_TEMPLATE =
            "You are Kronos AI, a personal daily scheduler assistant. " +
                    "You help users build a daily schedule through conversation. " +
                    "\n\nUSER PROFILE SETTINGS (use these as defaults unless user specifies otherwise):\n" +
                    "- Day starts at: %s\n" +
                    "- Day ends at: %s\n" +
                    "- Break duration preference: %d minutes\n" +
                    "- Morning focus (harder tasks in morning): %s\n" +
                    "\nSTRICT RULES:\n" +
                    "1. ALWAYS respond with ONLY a raw JSON object. No markdown, no code fences, no plain text. ONLY JSON.\n" +
                    "2. JSON format must be exactly: {\"message\": \"string\", \"tasks\": [{\"title\": \"string\", \"startTime\": \"HH:mm\", \"endTime\": \"HH:mm\"}]}\n" +
                    "3. If the user mentions BLOCKED time (job, office, work, class etc.), NEVER place any task during that period. This is absolute.\n" +
                    "4. Use the user's day start and end times from their profile settings UNLESS they explicitly say different times in chat.\n" +
                    "5. Read ALL previous messages and remember every constraint. Each new message refines the plan.\n" +
                    "6. When the user adds a constraint, rebuild the COMPLETE schedule respecting ALL constraints from ALL previous messages.\n" +
                    "7. The assistant messages in history contain JSON — use the tasks list to understand the previous schedule.\n" +
                    "8. Always include a 'Work/Job' blocked task during office hours if user mentions job/office.\n" +
                    "9. DO NOT ask the user for their day start/end time — you already have it from their profile settings above.\n" +
                    "10. Use the break duration from settings between tasks unless user specifies otherwise.\n" +
                    "11. If morning focus is true, schedule harder/study tasks in the morning before easier tasks.";

    public ResponseDTOs.AiPlanResponse generatePlan(String userMessage, String dayStart, String dayEnd) {
        List<Map<String, String>> history = new ArrayList<>();
        history.add(Map.of("role", "user", "content", userMessage));
        return generatePlanWithHistory(history, dayStart, dayEnd);
    }

    public ResponseDTOs.AiPlanResponse generatePlanWithHistory(
            List<Map<String, String>> history, String dayStart, String dayEnd) {
        return generatePlanWithHistory(history, dayStart, dayEnd, true, 15);
    }

    public ResponseDTOs.AiPlanResponse generatePlanWithHistory(
            List<Map<String, String>> history, String dayStart, String dayEnd,
            boolean morningFocus, int breakDuration) {

        try {
            List<Map<String, String>> messages = new ArrayList<>();

            String systemPrompt = String.format(SYSTEM_PROMPT_TEMPLATE,
                    dayStart, dayEnd, breakDuration,
                    morningFocus ? "Yes" : "No");

            messages.add(Map.of("role", "system", "content", systemPrompt));

            for (Map<String, String> turn : history) {
                String role    = turn.get("role");
                String content = turn.get("content");
                if (role != null && content != null && !content.isBlank()) {
                    if ("user".equals(role) || "assistant".equals(role)) {
                        messages.add(Map.of("role", role, "content", content));
                    }
                }
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 2000);
            requestBody.put("temperature", 0.3);
            requestBody.put("response_format", Map.of("type", "json_object"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("[OpenAI] Sending " + messages.size() + " messages | Day: " + dayStart + "-" + dayEnd);

            ResponseEntity<String> response = restTemplate.exchange(
                    openaiUrl, HttpMethod.POST, entity, String.class);

            String responseBody = response.getBody();
            if (responseBody == null) throw new RuntimeException("Empty response from OpenAI");

            JsonNode root = objectMapper.readTree(responseBody);

            if (root.has("error")) {
                throw new RuntimeException("OpenAI error: " +
                        root.path("error").path("message").asText());
            }

            String content = root.path("choices").get(0)
                    .path("message").path("content").asText();

            System.out.println("[OpenAI] Content: " + content.substring(0, Math.min(200, content.length())));

            return parseAiContent(content);

        } catch (Exception e) {
            System.err.println("[OpenAI ERROR] " + e.getMessage());
            return new ResponseDTOs.AiPlanResponse(
                    "Error: " + e.getMessage(), new ArrayList<>());
        }
    }

    private ResponseDTOs.AiPlanResponse parseAiContent(String content) throws Exception {
        String cleaned = content.trim();

        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("(?s)^```[a-zA-Z]*\\s*", "")
                    .replaceAll("(?s)```\\s*$", "").trim();
        }

        int start = cleaned.indexOf('{');
        int end   = cleaned.lastIndexOf('}');
        if (start == -1 || end == -1 || end < start) {
            throw new RuntimeException("No valid JSON in AI response: " + cleaned);
        }
        cleaned = cleaned.substring(start, end + 1);

        JsonNode parsed = objectMapper.readTree(cleaned);
        String message = parsed.path("message").asText("Here is your plan!");
        List<ResponseDTOs.AiTask> tasks = new ArrayList<>();

        JsonNode tasksNode = parsed.path("tasks");
        if (tasksNode.isArray()) {
            for (JsonNode t : tasksNode) {
                String title = t.path("title").asText("").trim();
                String st    = t.path("startTime").asText("").trim();
                String et    = t.path("endTime").asText("").trim();
                if (!title.isEmpty() && !st.isEmpty() && !et.isEmpty()) {
                    tasks.add(new ResponseDTOs.AiTask(title, st, et));
                }
            }
        }

        System.out.println("[OpenAI] Parsed " + tasks.size() + " tasks");
        return new ResponseDTOs.AiPlanResponse(message, tasks);
    }
}