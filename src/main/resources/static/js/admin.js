async function initAdmin() {
  if (!requireAdmin()) return;
  await loadStats();
  await loadUsers();
}

async function loadStats() {
  const res = await API.getAdminStats();
  if (!res || !res.success) return;
  const s = res.data;
  document.getElementById('stat-total').textContent    = s.totalUsers;
  document.getElementById('stat-active').textContent   = s.activeUsers;
  document.getElementById('stat-disabled').textContent = s.disabledUsers;
  if (document.getElementById('stat-plans'))  document.getElementById('stat-plans').textContent  = s.totalPlans;
  if (document.getElementById('stat-tasks'))  document.getElementById('stat-tasks').textContent  = s.totalTasks;
}

async function loadUsers() {
  const res = await API.getUsers();
  const tbody = document.getElementById('users-tbody');
  if (!tbody) return;

  if (!res || !res.success) {
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:32px">Failed to load users</td></tr>';
    return;
  }

  const users = res.data;
  if (!users || users.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:32px">No users yet</td></tr>';
    return;
  }

  const colors = ['#7C6FFF','#FF6B8A','#43D9AC','#FFB347','#5A4FD4','#FF5C7A'];
  tbody.innerHTML = users.map((u, i) => {
    const color   = colors[i % colors.length];
    const initial = u.name.charAt(0).toUpperCase();
    const joined  = new Date(u.createdAt).toLocaleDateString('en-US', { month:'short', day:'numeric', year:'numeric' });
    return `
    <tr>
      <td>
        <div style="display:flex;align-items:center;gap:12px">
          <div style="width:36px;height:36px;border-radius:50%;background:${color};display:flex;align-items:center;justify-content:center;font-weight:700;font-size:14px">${initial}</div>
          <div>
            <div style="font-weight:500">${escapeHtml(u.name)}</div>
            <div style="font-size:12px;color:var(--muted)">${escapeHtml(u.email)}</div>
          </div>
        </div>
      </td>
      <td>${joined}</td>
      <td>${u.plansCount}</td>
      <td><span class="badge ${u.enabled ? 'badge-active' : 'badge-disabled'}">${u.enabled ? 'Active' : 'Disabled'}</span></td>
      <td>
        <div style="display:flex;gap:8px">
          <button class="btn-secondary btn-sm" onclick="toggleUser(${u.id})">${u.enabled ? 'Disable' : 'Enable'}</button>
          <button class="btn-danger btn-sm" onclick="deleteUser(${u.id}, '${escapeHtml(u.name)}')">Delete</button>
        </div>
      </td>
    </tr>`;
  }).join('');
}

async function toggleUser(id) {
  const res = await API.toggleUser(id);
  if (res && res.success) {
    showToast('User status updated');
    await loadUsers();
    await loadStats();
  } else {
    showToast('Failed to update', 'error');
  }
}

async function deleteUser(id, name) {
  if (!confirm(`Delete user "${name}" and all their data? This cannot be undone.`)) return;
  const res = await API.deleteUser(id);
  if (res && res.success) {
    showToast('User deleted');
    await loadUsers();
    await loadStats();
  } else {
    showToast(res?.message || 'Failed to delete', 'error');
  }
}

function initAdminSettings() {
  if (!requireAdmin()) return;
  loadStats();

  const el = document.getElementById('last-login');
  if (el) el.textContent = new Date().toLocaleString();

  document.getElementById('admin-pw-form')?.addEventListener('submit', async e => {
    e.preventDefault();
    const current = document.getElementById('current-pw').value;
    const newPw   = document.getElementById('new-pw').value;
    const confirm = document.getElementById('confirm-pw').value;
    if (newPw !== confirm) { showToast('Passwords do not match', 'error'); return; }
    showToast('Password updated (demo mode)');
    e.target.reset();
  });
}

function escapeHtml(str) {
  return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}
