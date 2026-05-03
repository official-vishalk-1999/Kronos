async function apiCall(method, path, body = null) {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const opts = { method, headers };
  if (body) opts.body = JSON.stringify(body);

  const res = await fetch(`/api${path}`, opts);
  const data = await res.json();

  if (res.status === 401) {
    clearAuth();
    window.location.href = '/login.html';
    return null;
  }
  return data;
}

const API = {
  getProfile:      ()       => apiCall('GET',    '/user/profile'),
  updateSettings:  (body)   => apiCall('PUT',    '/user/settings', body),
  changePassword:  (body)   => apiCall('PUT',    '/user/change-password', body),
  deleteAccount:   ()       => apiCall('DELETE', '/user/delete-account'),

  generatePlanWithHistory: (history) => apiCall('POST', '/plans/generate', {
    message: history[history.length - 1].content,
    history: history
  }),
  generatePlan:    (msg)    => apiCall('POST', '/plans/generate', { message: msg }),
  confirmPlan:     (tasks)  => apiCall('POST', '/plans/confirm', { tasks }),
  getTodayPlan:    ()       => apiCall('GET',  '/plans/today'),
  getPlanByDate:   (date)   => apiCall('GET',  `/plans/date/${date}`),
  regeneratePlan:  (msg)    => apiCall('PUT',  '/plans/regenerate', { message: msg }),

  completeTask:    (id)     => apiCall('PUT',    `/tasks/${id}/complete`),
  deleteTask:      (id)     => apiCall('DELETE', `/tasks/${id}`),
  updateTask:      (id, b)  => apiCall('PUT',    `/tasks/${id}`, b),

  getNotifications:  ()    => apiCall('GET',    '/notifications'),
  markRead:          (id)  => apiCall('PUT',    `/notifications/${id}/read`),
  getUnreadCount:    ()    => apiCall('GET',    '/notifications/unread-count'),

  getTodayStats:   ()      => apiCall('GET',    '/dashboard/today-stats'),
  getWeeklyStats:  ()      => apiCall('GET',    '/dashboard/weekly-stats'),

  getUsers:        ()      => apiCall('GET',    '/admin/users'),
  toggleUser:      (id)    => apiCall('PUT',    `/admin/users/${id}/toggle`),
  deleteUser:      (id)    => apiCall('DELETE', `/admin/users/${id}`),
  getAdminStats:   ()      => apiCall('GET',    '/admin/stats'),
};