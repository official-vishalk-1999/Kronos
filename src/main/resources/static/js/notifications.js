async function initNotifications() {
  if (!requireAuth()) return;
  fillSidebarUser();
  await loadNotifications();
}

async function loadNotifications() {
  const res = await API.getNotifications();
  const container = document.getElementById('notifications-container');

  if (!res || !res.success) {
    container.innerHTML = '<div class="empty-state"><h3>Failed to load notifications</h3></div>';
    return;
  }

  const notifs = res.data;
  if (!notifs || notifs.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>
        </svg>
        <h3>No notifications</h3>
        <p>You're all caught up!</p>
      </div>`;
    return;
  }

  container.innerHTML = notifs.map(n => renderNotification(n)).join('');
}

function getNotifIcon(type) {
  switch(type) {
    case 'TASK_REMINDER': return `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>`;
    case 'AI_SUGGESTION': return `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm0 18a8 8 0 1 1 0-16 8 8 0 0 1 0 16zm-1-5h2v2h-2zm0-8h2v6h-2z"/></svg>`;
    case 'SUCCESS': return `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>`;
    case 'WARNING': return `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`;
    default: return `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>`;
  }
}

function getNotifColor(type) {
  switch(type) {
    case 'TASK_REMINDER': return 'var(--primary)';
    case 'AI_SUGGESTION': return 'var(--amber)';
    case 'SUCCESS': return 'var(--green)';
    case 'WARNING': return 'var(--pink)';
    default: return 'var(--muted)';
  }
}

function renderNotification(n) {
  const color = getNotifColor(n.type);
  const icon  = getNotifIcon(n.type);
  const time  = new Date(n.createdAt).toLocaleString('en-US', { month:'short', day:'numeric', hour:'numeric', minute:'2-digit' });

  return `
    <div class="card" style="margin-bottom:12px;border-left:3px solid ${n.read ? 'transparent' : color};cursor:pointer;transition:all 0.2s" 
         id="notif-${n.id}" onclick="markRead(${n.id})">
      <div style="display:flex;align-items:flex-start;gap:14px">
        <div style="width:36px;height:36px;border-radius:10px;background:${color}22;display:flex;align-items:center;justify-content:center;color:${color};flex-shrink:0">
          ${icon}
        </div>
        <div style="flex:1">
          <div style="display:flex;align-items:center;justify-content:space-between;gap:12px">
            <div style="font-size:14px;font-weight:500">${escapeHtml(n.message)}</div>
            ${!n.read ? `<div style="width:8px;height:8px;border-radius:50%;background:${color};flex-shrink:0"></div>` : ''}
          </div>
          <div style="font-size:12px;color:var(--muted);margin-top:4px">${time}</div>
        </div>
      </div>
    </div>`;
}

async function markRead(id) {
  const card = document.getElementById(`notif-${id}`);
  if (card && card.style.borderLeftColor !== 'transparent') {
    await API.markRead(id);
    card.style.borderLeftColor = 'transparent';
    card.querySelector('div[style*="border-radius:50%"]')?.remove();
  }
}

function escapeHtml(str) {
  return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}
