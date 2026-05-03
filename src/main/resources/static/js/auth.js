const API_BASE = '/api';

function getToken() { return localStorage.getItem('kronos_token'); }
function getUser()  { return JSON.parse(localStorage.getItem('kronos_user') || 'null'); }
function getRole()  { return localStorage.getItem('kronos_role'); }

function saveAuth(data) {
  localStorage.setItem('kronos_token', data.token);
  localStorage.setItem('kronos_role', data.role);
  localStorage.setItem('kronos_user', JSON.stringify({ name: data.name, email: data.email }));
}

function clearAuth() {
  localStorage.removeItem('kronos_token');
  localStorage.removeItem('kronos_role');
  localStorage.removeItem('kronos_user');
}

function logout() {
  clearAuth();
  window.location.href = '/login.html';
}

function adminLogout() {
  clearAuth();
  window.location.href = '/admin-login.html';
}

function requireAuth() {
  if (!getToken()) {
    window.location.href = '/login.html';
    return false;
  }
  return true;
}

function requireAdmin() {
  if (!getToken() || getRole() !== 'ADMIN') {
    window.location.href = '/admin-login.html';
    return false;
  }
  return true;
}

function fillSidebarUser() {
  const user = getUser();
  if (!user) return;
  const nameEl = document.getElementById('sidebar-user-name');
  const avatarEl = document.getElementById('sidebar-avatar');
  if (nameEl) nameEl.textContent = user.name;
  if (avatarEl) avatarEl.textContent = user.name.charAt(0).toUpperCase();
}

function showToast(message, type = 'success') {
  let toast = document.getElementById('toast');
  if (!toast) {
    toast = document.createElement('div');
    toast.id = 'toast';
    document.body.appendChild(toast);
  }
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      ${type === 'success'
        ? '<polyline points="20 6 9 17 4 12"></polyline>'
        : '<circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line>'}
    </svg>
    ${message}`;
  void toast.offsetWidth;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 3200);
}

async function doSignup(name, email, password) {
  const res = await fetch(`${API_BASE}/auth/signup`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, email, password })
  });
  const data = await res.json();
  if (data.success) {
    saveAuth(data.data);
    showToast('Account created! Welcome to Kronos.');
    setTimeout(() => window.location.href = '/dashboard.html', 800);
  } else {
    throw new Error(data.message || 'Signup failed');
  }
}

async function doLogin(email, password) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  const data = await res.json();
  if (data.success) {
    saveAuth(data.data);
    showToast('Welcome back!');
    setTimeout(() => window.location.href = '/dashboard.html', 800);
  } else {
    throw new Error(data.message || 'Login failed');
  }
}

async function doAdminLogin(username, password) {
  const res = await fetch(`${API_BASE}/auth/admin-login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  const data = await res.json();
  if (data.success) {
    saveAuth(data.data);
    showToast('Welcome, Administrator!');
    setTimeout(() => window.location.href = '/admin.html', 800);
  } else {
    throw new Error(data.message || 'Invalid admin credentials');
  }
}

function showError(elId, msg) {
  const el = document.getElementById(elId);
  if (el) { el.textContent = msg; el.classList.add('visible'); }
}
function clearErrors() {
  document.querySelectorAll('.error-msg').forEach(e => e.classList.remove('visible'));
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function isValidPassword(pw) {
  return pw.length >= 8 && /\d/.test(pw);
}
