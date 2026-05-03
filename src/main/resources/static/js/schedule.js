function todayLocal() {
  const d = new Date();
  return localDateStr(d);
}

function localDateStr(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function addDays(dateStr, n) {
  const [y, m, d] = dateStr.split('-').map(Number);
  const dt = new Date(y, m - 1, d);
  dt.setDate(dt.getDate() + n);
  return localDateStr(dt);
}

let currentDate = todayLocal();

async function initSchedule() {
  if (!requireAuth()) return;
  fillSidebarUser();
  updateDateDisplay();
  await loadSchedule();
  await loadUnreadCount();
}

function updateDateDisplay() {
  const [y, m, d] = currentDate.split('-').map(Number);
  const dt = new Date(y, m - 1, d);
  const isToday = currentDate === todayLocal();
  const formatted = dt.toLocaleDateString('en-US', {
    weekday: 'long', month: 'long', day: 'numeric',
    ...(isToday ? {} : { year: 'numeric' })
  });
  document.getElementById('date-display').textContent =
      isToday ? `Today — ${formatted}` : formatted;
}

async function loadSchedule() {
  const isToday = currentDate === todayLocal();
  let res;
  try {
    res = isToday ? await API.getTodayPlan() : await API.getPlanByDate(currentDate);
  } catch (e) {
    res = null;
  }

  const container = document.getElementById('tasks-container');

  if (!res || !res.success || !res.data) {
    container.innerHTML = `
      <div class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <rect x="3" y="4" width="18" height="18" rx="2"/>
          <line x1="16" y1="2" x2="16" y2="6"/>
          <line x1="8" y1="2" x2="8" y2="6"/>
          <line x1="3" y1="10" x2="21" y2="10"/>
        </svg>
        <h3>No plan for this day</h3>
        <p>Use the AI Planner to generate your daily schedule</p>
        <a href="/planner.html" class="btn-primary" style="margin-top:14px;text-decoration:none">Open AI Planner</a>
      </div>`;
    return;
  }

  const tasks = res.data.tasks;
  if (!tasks || tasks.length === 0) {
    container.innerHTML = '<div class="empty-state"><h3>No tasks scheduled</h3></div>';
    return;
  }

  container.innerHTML = tasks.map(t => renderTaskCard(t)).join('');
}

function renderTaskCard(t) {
  const hour = parseInt(t.startTime.split(':')[0]);
  const color = hour < 12 ? 'var(--primary)' : hour < 17 ? 'var(--amber)' : 'var(--green)';
  const isNow = t.status === 'IN_PROGRESS';

  let badgeClass = 'badge-pending';
  if (t.status === 'IN_PROGRESS') badgeClass = 'badge-progress';
  if (t.status === 'COMPLETED')   badgeClass = 'badge-done';
  if (t.status === 'MISSED')      badgeClass = 'badge-missed';

  let actions = '';
  if (t.status === 'PENDING' || t.status === 'IN_PROGRESS') {
    actions += `<button class="btn-success btn-sm" onclick="markDone(${t.id})">✓ Done</button>`;
  }
  if (t.status === 'PENDING') {
    actions += `<button class="btn-danger btn-sm" onclick="deleteTask(${t.id})">Delete</button>`;
  }

  return `
    <div class="task-card ${isNow ? 'active-task' : ''}" id="task-${t.id}">
      <div class="task-time-bar" style="background:${color}; height:54px"></div>
      <div style="flex:1">
        <div style="display:flex;align-items:center;gap:10px;margin-bottom:4px">
          <div class="task-title">${escapeHtml(t.title)}</div>
          ${isNow ? '<span style="background:var(--primary);color:#fff;font-size:10px;padding:2px 8px;border-radius:10px;font-weight:700">NOW</span>' : ''}
        </div>
        <div class="task-time-range">${t.startTime} – ${t.endTime}</div>
      </div>
      <div style="display:flex;align-items:center;gap:10px">
        <span class="badge ${badgeClass}">${t.status.replace('_', ' ')}</span>
        <div class="task-actions">${actions}</div>
      </div>
    </div>`;
}

async function markDone(id) {
  const res = await API.completeTask(id);
  if (res && res.success) {
    showToast('Task completed! 🎉');
    await loadSchedule();
  } else {
    showToast(res?.message || 'Error', 'error');
  }
}

async function deleteTask(id) {
  if (!confirm('Delete this task?')) return;
  const res = await API.deleteTask(id);
  if (res && res.success) {
    showToast('Task deleted');
    await loadSchedule();
  } else {
    showToast(res?.message || 'Error', 'error');
  }
}

function prevDay() {
  currentDate = addDays(currentDate, -1);
  updateDateDisplay();
  loadSchedule();
}

function nextDay() {
  currentDate = addDays(currentDate, 1);
  updateDateDisplay();
  loadSchedule();
}

function escapeHtml(str) {
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

async function loadUnreadCount() {
  const res = await API.getUnreadCount();
  if (res && res.success) {
    const badge = document.getElementById('notif-badge');
    if (badge) {
      badge.textContent = res.data.count;
      badge.style.display = res.data.count > 0 ? 'flex' : 'none';
    }
  }
}