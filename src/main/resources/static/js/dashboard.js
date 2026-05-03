async function initDashboard() {
  if (!requireAuth()) return;
  fillSidebarUser();
  await loadStats();
  await loadTodayPlan();
  await loadUnreadCount();
}

async function loadStats() {
  const res = await API.getTodayStats();

  if (res && res.success) {
    const s = res.data;
    document.getElementById('stat-total').textContent     = s.totalTasks;
    document.getElementById('stat-completed').textContent = s.completedTasks;
    document.getElementById('stat-missed').textContent    = s.missedTasks;
    document.getElementById('stat-productivity').textContent = s.productivityPercentage + '%';

    document.getElementById('bar-completed').style.width =
        (s.totalTasks > 0 ? (s.completedTasks / s.totalTasks * 100) : 0) + '%';
    document.getElementById('bar-missed').style.width =
        (s.totalTasks > 0 ? (s.missedTasks / s.totalTasks * 100) : 0) + '%';
    document.getElementById('bar-productivity').style.width =
        s.productivityPercentage + '%';
  }
}

async function loadTodayPlan() {
  const res = await API.getTodayPlan();
  const container = document.getElementById('upcoming-tasks');

  if (!res || !res.success || !res.data) {
    container.innerHTML = `
      <div class="empty-state" style="grid-column:1/-1">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <rect x="3" y="4" width="18" height="18" rx="2"/>
          <line x1="16" y1="2" x2="16" y2="6"/>
          <line x1="8" y1="2" x2="8" y2="6"/>
          <line x1="3" y1="10" x2="21" y2="10"/>
        </svg>
        <h3>No plan today</h3>
        <p>Go to the AI Planner to generate your schedule</p>
        <a href="/planner.html" class="btn-primary btn-sm" style="margin-top:12px;text-decoration:none">Open Planner</a>
      </div>`;
    return;
  }

  const tasks = res.data.tasks;
  if (!tasks || tasks.length === 0) {
    container.innerHTML = '<div class="empty-state" style="grid-column:1/-1"><h3>No tasks yet</h3></div>';
    return;
  }

  const upcoming = tasks.filter(t => t.status !== 'COMPLETED' && t.status !== 'MISSED').slice(0, 6);
  if (upcoming.length === 0) {
    container.innerHTML = '<div class="empty-state" style="grid-column:1/-1"><h3>All tasks done! 🎉</h3></div>';
    return;
  }

  container.innerHTML = upcoming.map(t => {
    const color = t.status === 'IN_PROGRESS' ? 'var(--primary)' : 'var(--muted)';
    return `
    <div class="task-card ${t.status === 'IN_PROGRESS' ? 'active-task' : ''}">
      <div class="task-time-bar" style="background:${color}"></div>
      <div>
        <div class="task-title">${t.title}</div>
        <div class="task-time-range">${t.startTime} – ${t.endTime}</div>
      </div>
      <div style="margin-left:auto">
        <span class="badge badge-${t.status === 'IN_PROGRESS' ? 'progress' : 'pending'}">
          ${t.status.replace('_', ' ')}
        </span>
      </div>
    </div>`;
  }).join('');
}

async function loadUnreadCount() {
  const res = await API.getUnreadCount();
  if (res && res.success) {
    const count = res.data.count;
    const badge = document.getElementById('notif-badge');
    if (badge) {
      badge.textContent = count;
      badge.style.display = count > 0 ? 'flex' : 'none';
    }
  }
}