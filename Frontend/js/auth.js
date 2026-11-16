/* ============================================================
   auth.js · Sesión, token JWT y cliente HTTP compartido
   ============================================================ */
const API_URL = 'http://localhost:8080';   // Cambia aquí si la API usa otro host/puerto

/* ---------- Sesión (sessionStorage: se borra al cerrar la pestaña) ---------- */
const getToken = () => sessionStorage.getItem('lt_token');
const getUser  = () => ({ username: sessionStorage.getItem('lt_user'), rol: sessionStorage.getItem('lt_rol') });

function saveSession({ token, username, rol, expiraEnSegundos }) {
  sessionStorage.setItem('lt_token', token);
  sessionStorage.setItem('lt_user', username);
  sessionStorage.setItem('lt_rol', rol);
  sessionStorage.setItem('lt_exp', Date.now() + expiraEnSegundos * 1000); // el backend informa la vigencia
}
function sessionValid() {
  return !!getToken() && Date.now() < Number(sessionStorage.getItem('lt_exp') || 0);
}
/** Cierra sesión: borra el token y vuelve al login (opcionalmente con un mensaje). */
function logout(mensaje) {
  ['lt_token', 'lt_user', 'lt_rol', 'lt_exp'].forEach(k => sessionStorage.removeItem(k));
  if (mensaje) sessionStorage.setItem('lt_flash', mensaje);
  window.location.href = 'index.html';
}

/* ---------- Toast (aviso temporal) ---------- */
function toast(msg, tipo = 'info') {
  const box = document.getElementById('toasts');
  const el = document.createElement('div');
  el.className = `toast toast-${tipo}`;
  el.textContent = msg;
  box.appendChild(el);
  setTimeout(() => el.remove(), 5000);
}

/* ---------- Cliente HTTP: añade el Bearer token y traduce los errores ---------- */
async function apiFetch(path, { method = 'GET', body } = {}) {
  let res;
  try {
    res = await fetch(API_URL + path, {
      method,
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${getToken()}`
      },
      body: body ? JSON.stringify(body) : undefined
    });
  } catch {
    throw new Error('No se pudo conectar con el servidor. Verifica que la API esté en ' + API_URL);
  }

  // 401: token ausente, inválido o vencido → de vuelta al login
  if (res.status === 401) { logout('Tu sesión expiró. Inicia sesión de nuevo.'); throw new Error('Sesión expirada'); }
  if (res.status === 204) return null;

  const data = await res.json().catch(() => null);
  if (!res.ok) throw new Error(mensajeDeError(data, res.status));
  return data;
}

/** Extrae el mensaje del backend: {message} o {errors:{campo:msg}} (validaciones). */
function mensajeDeError(data, status) {
  if (data?.errors) return Object.values(data.errors).join(' · ');
  if (data?.message) return data.message;      // 403 también llega aquí: "No tienes permisos…"
  return `Error inesperado (${status})`;
}

/* ---------- Pantalla de login (index.html) ---------- */
function initLoginPage() {
  if (sessionValid()) { window.location.href = 'dashboard.html'; return; }

  const form = document.getElementById('loginForm');
  const err  = document.getElementById('loginError');
  const btn  = document.getElementById('loginBtn');

  // Mensaje pendiente (por ejemplo, "Tu sesión expiró")
  const flash = sessionStorage.getItem('lt_flash');
  if (flash) { toast(flash, 'warn'); sessionStorage.removeItem('lt_flash'); }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    err.hidden = true;
    const username = form.username.value.trim();
    const password = form.password.value;
    if (!username || !password) { err.textContent = 'Ingresa tu usuario y contraseña.'; err.hidden = false; return; }

    btn.disabled = true; btn.textContent = 'Ingresando…';
    try {
      const res = await fetch(`${API_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      const data = await res.json().catch(() => null);
      if (!res.ok) throw new Error(res.status === 401 ? 'Usuario o contraseña incorrectos.' : mensajeDeError(data, res.status));
      saveSession(data);
      window.location.href = 'dashboard.html';
    } catch (ex) {
      err.textContent = ex instanceof TypeError ? 'No se pudo conectar con el servidor.' : ex.message;
      err.hidden = false;
    } finally {
      btn.disabled = false; btn.textContent = 'Iniciar sesión';
    }
  });
}
