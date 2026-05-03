let pendingTasks = [];
let chatHistory  = [];
let hasExistingPlan = false;

async function initPlanner() {
  if (!requireAuth()) return;
  fillSidebarUser();
  await loadUnreadCount();

  document.getElementById('chat-input').addEventListener('keydown', e => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(); }
  });

  addAiMessage("Hello! I'm Kronos AI, your personal daily scheduler. Tell me about your goals for today, any fixed meetings or commitments, and I'll build an optimized schedule for you! 🗓️");

  const res = await API.getTodayPlan();
  if (res && res.success && res.data) {
    hasExistingPlan = true;
    showExistingPlan(res.data);
    showStartFreshBtn();
  }
}

function showStartFreshBtn() {
  const btn = document.getElementById('start-fresh-btn');
  if (btn) btn.style.display = 'flex';
}

function hideStartFreshBtn() {
  const btn = document.getElementById('start-fresh-btn');
  if (btn) btn.style.display = 'none';
}

function confirmStartFresh() {
  document.getElementById('fresh-modal').style.display = 'flex';
}

function closeFreshModal() {
  document.getElementById('fresh-modal').style.display = 'none';
}

async function startFresh() {
  closeFreshModal();

  const btn = document.getElementById('start-fresh-btn');
  if (btn) { btn.style.opacity = '0.5'; btn.style.pointerEvents = 'none'; }

  try {
    await apiCall('DELETE', '/plans/today');
  } catch(e) {
  }

  hasExistingPlan = false;
  pendingTasks    = [];
  chatHistory     = [];

  document.getElementById('chat-messages').innerHTML = '';

  document.getElementById('plan-preview').innerHTML = `
    <div class="empty-state">
      <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z"/>
        <polyline points="12 6 12 12 16 14"/>
      </svg>
      <p style="margin-top:12px;font-size:13px;color:var(--muted)">Your AI-generated plan will appear here</p>
    </div>`;
  document.getElementById('plan-actions').style.display = 'none';

  hideStartFreshBtn();

  addAiMessage("Your previous plan has been deleted! 🗑️ Start fresh — tell me about your goals for today and I'll build a brand new schedule for you!");

  showToast('Previous plan deleted. Starting fresh!');
}

function addUserMessage(text) {
  const chat = document.getElementById('chat-messages');
  const user  = getUser();
  const initial = user ? user.name.charAt(0).toUpperCase() : 'U';
  const div = document.createElement('div');
  div.className = 'chat-message user-message';
  div.innerHTML = `
    <div class="msg-avatar user-msg-avatar">${initial}</div>
    <div class="user-bubble chat-bubble">${escapeHtml(text)}</div>`;
  chat.appendChild(div);
  scrollChat();
}

function addAiMessage(text) {
  const chat = document.getElementById('chat-messages');
  const div  = document.createElement('div');
  div.className = 'chat-message ai-message';
  div.innerHTML = `
    <div class="msg-avatar ai-msg-avatar">K</div>
    <div>
      <div class="chat-label">Kronos AI</div>
      <div class="ai-bubble chat-bubble">${text}</div>
    </div>`;
  chat.appendChild(div);
  scrollChat();
}

function showTyping() {
  const chat = document.getElementById('chat-messages');
  const div  = document.createElement('div');
  div.className = 'chat-message ai-message';
  div.id = 'typing-indicator';
  div.innerHTML = `
    <div class="msg-avatar ai-msg-avatar">K</div>
    <div>
      <div class="chat-label">Kronos AI</div>
      <div class="ai-bubble chat-bubble">
        <div class="typing-dots"><span></span><span></span><span></span></div>
      </div>
    </div>`;
  chat.appendChild(div);
  scrollChat();
}

function removeTyping() {
  const el = document.getElementById('typing-indicator');
  if (el) el.remove();
}

function scrollChat() {
  const chat = document.getElementById('chat-messages');
  setTimeout(() => { chat.scrollTop = chat.scrollHeight; }, 10);
}

async function sendMessage() {
  const input = document.getElementById('chat-input');
  const text  = input.value.trim();
  if (!text) return;

  input.value = '';
  input.style.height = 'auto';

  const sendBtn = document.getElementById('send-btn');
  sendBtn.disabled = true;

  chatHistory.push({ role: 'user', content: text });
  addUserMessage(text);
  showTyping();

  const res = await API.generatePlanWithHistory(chatHistory);
  removeTyping();
  sendBtn.disabled = false;

  if (res && res.success) {
    const plan   = res.data;
    const aiText = plan.message || 'Here is your updated plan!';
    pendingTasks = plan.tasks || [];

    const assistantContent = JSON.stringify({ message: aiText, tasks: pendingTasks });
    chatHistory.push({ role: 'assistant', content: assistantContent });

    addAiMessage(aiText);
    if (pendingTasks.length > 0) {
      renderPlanPreview(pendingTasks);
      showStartFreshBtn();
    }
  } else {
    const errMsg = res?.message || 'Sorry, something went wrong. Please try again.';
    addAiMessage(errMsg);
  }
}

function renderPlanPreview(tasks) {
  const preview = document.getElementById('plan-preview');
  const actions = document.getElementById('plan-actions');

  if (!tasks || tasks.length === 0) {
    preview.innerHTML = '<div class="empty-state"><p style="font-size:13px;margin-top:12px;color:var(--muted)">No tasks yet</p></div>';
    actions.style.display = 'none';
    return;
  }

  preview.innerHTML = tasks.map((t, i) => {
    const colors = ['var(--primary)', 'var(--pink)', 'var(--green)', 'var(--amber)'];
    const color  = colors[i % colors.length];
    return `
    <div class="task-card" style="border-left:3px solid ${color};margin-bottom:8px;">
      <div>
        <div class="task-title">${escapeHtml(t.title)}</div>
        <div class="task-time-range">${t.startTime} – ${t.endTime}</div>
      </div>
    </div>`;
  }).join('');

  actions.style.display = 'block';
}

async function confirmPlan() {
  if (!pendingTasks.length) return;
  const btn = document.getElementById('confirm-btn');
  btn.disabled = true;
  btn.textContent = 'Saving...';

  const res = await API.confirmPlan(pendingTasks);
  if (res && res.success) {
    hasExistingPlan = true;
    showToast('Plan confirmed! Head to your schedule 🎉');
    setTimeout(() => window.location.href = '/schedule.html', 1000);
  } else {
    showToast(res?.message || 'Failed to confirm plan', 'error');
    btn.disabled = false;
    btn.innerHTML = `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> Confirm Plan`;
  }
}

function showExistingPlan(plan) {
  const msg = `You already have a plan for today with ${plan.tasks.length} tasks. Keep chatting to modify it, or <a href="/schedule.html" style="color:var(--primary)">view your schedule</a>. To start completely fresh, click <strong style="color:var(--pink)">Start Fresh</strong> above.`;
  addAiMessage(msg);
  if (plan.tasks.length > 0) {
    pendingTasks = plan.tasks.map(t => ({
      title: t.title, startTime: t.startTime, endTime: t.endTime
    }));
    renderPlanPreview(pendingTasks);
    chatHistory.push({
      role: 'assistant',
      content: JSON.stringify({ message: 'Existing plan loaded.', tasks: pendingTasks })
    });
  }
}

function escapeHtml(str) {
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

async function loadUnreadCount() {
  const res = await API.getUnreadCount();
  if (res && res.success) {
    const badge = document.getElementById('notif-badge');
    if (badge) {
      badge.textContent  = res.data.count;
      badge.style.display = res.data.count > 0 ? 'flex' : 'none';
    }
  }
}