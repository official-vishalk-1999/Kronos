async function initSettings() {
  if (!requireAuth()) return;
  fillSidebarUser();
  await loadProfile();
  await loadUnreadCount();
  bindPasswordForm();
}

async function loadProfile() {
  const res = await API.getProfile();
  if (!res || !res.success) return;
  const p = res.data;

  document.getElementById('profile-name').textContent       = p.name;
  document.getElementById('profile-email').textContent      = p.email;
  document.getElementById('profile-role').textContent       = p.role;
  document.getElementById('profile-profession').textContent = p.profession || 'Not set';

  document.getElementById('profession-input').value = p.profession || '';
  document.getElementById('start-time-input').value = p.dayStartTime || '07:00';
  document.getElementById('end-time-input').value   = p.dayEndTime   || '22:00';
  document.getElementById('break-duration').value   = p.breakDuration || 15;

  setToggle('toggle-morning', p.morningFocus);
}

function setToggle(id, val) {
  const el = document.getElementById(id);
  if (el) el.checked = !!val;
}

async function saveSettings() {
  const body = {
    profession:    document.getElementById('profession-input').value,
    dayStartTime:  document.getElementById('start-time-input').value,
    dayEndTime:    document.getElementById('end-time-input').value,
    breakDuration: parseInt(document.getElementById('break-duration').value),
    morningFocus:  document.getElementById('toggle-morning').checked,
  };

  const res = await API.updateSettings(body);
  if (res && res.success) {
    showToast('Settings saved!');
    // Update profile display
    document.getElementById('profile-profession').textContent =
        body.profession || 'Not set';
  } else {
    showToast(res?.message || 'Save failed', 'error');
  }
}

function bindPasswordForm() {
  document.getElementById('password-form').addEventListener('submit', async e => {
    e.preventDefault();
    clearErrors();
    const current = document.getElementById('current-password').value;
    const newPw   = document.getElementById('new-password').value;
    const confirm = document.getElementById('confirm-password').value;

    if (newPw !== confirm) { showError('pw-error', 'Passwords do not match'); return; }
    if (!isValidPassword(newPw)) { showError('pw-error', 'Password must be 8+ chars with a number'); return; }

    const res = await API.changePassword({ currentPassword: current, newPassword: newPw });
    if (res && res.success) {
      showToast('Password updated!');
      document.getElementById('password-form').reset();
    } else {
      showError('pw-error', res?.message || 'Failed to update password');
    }
  });
}

async function clearAllTasks() {
  showConfirmModal(
      'Clear All Tasks',
      'This will delete all your pending tasks for today. Are you sure?',
      async () => {
        const btn = document.getElementById('clear-tasks-btn');
        btn.disabled = true;
        btn.textContent = 'Clearing...';
        try {
          const res = await API.getTodayPlan();
          if (res && res.success && res.data) {
            const tasks = res.data.tasks;
            for (const task of tasks) {
              if (task.status === 'PENDING') {
                await API.deleteTask(task.id);
              }
            }
          }
          showToast('All pending tasks cleared!');
        } catch (e) {
          showToast('Failed to clear tasks', 'error');
        }
        btn.disabled = false;
        btn.textContent = 'Clear All Tasks';
      }
  );
}

function deleteAccount() {
  showDeleteAccountModal();
}

async function performDeleteAccount() {
  const input = document.getElementById('delete-confirm-input');
  if (!input || input.value !== 'DELETE') {
    document.getElementById('delete-input-error').style.display = 'block';
    return;
  }

  const btn = document.getElementById('confirm-delete-btn');
  btn.disabled = true;
  btn.textContent = 'Deleting...';

  try {
    const res = await API.deleteAccount();
    if (res && res.success) {
      closeModal('delete-account-modal');
      clearAuth();
      showToast('Account deleted successfully. Goodbye!');
      setTimeout(() => window.location.href = '/', 1500);
    } else {
      showToast(res?.message || 'Failed to delete account', 'error');
      btn.disabled = false;
      btn.textContent = 'Delete My Account';
    }
  } catch (e) {
    showToast('Failed to delete account', 'error');
    btn.disabled = false;
    btn.textContent = 'Delete My Account';
  }
}

function showConfirmModal(title, message, onConfirm) {
  document.getElementById('confirm-modal-title').textContent   = title;
  document.getElementById('confirm-modal-message').textContent = message;
  document.getElementById('confirm-modal').style.display       = 'flex';
  document.getElementById('modal-confirm-btn').onclick = () => {
    closeModal('confirm-modal');
    onConfirm();
  };
}

function showDeleteAccountModal() {
  document.getElementById('delete-account-modal').style.display = 'flex';
  document.getElementById('delete-confirm-input').value = '';
  document.getElementById('delete-input-error').style.display = 'none';
}

function closeModal(id) {
  document.getElementById(id).style.display = 'none';
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

function showError(elId, msg) {
  const el = document.getElementById(elId);
  if (el) { el.textContent = msg; el.style.display = 'block'; }
}

function clearErrors() {
  document.querySelectorAll('.error-msg').forEach(e => {
    e.style.display = 'none';
    e.textContent = '';
  });
}