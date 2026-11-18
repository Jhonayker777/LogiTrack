const SESSION_KEY = "logitrack_session";

const Session = {
  get() {
    try {
      return JSON.parse(localStorage.getItem(SESSION_KEY));
    } catch {
      return null;
    }
  },
  set(login) {
    const session = {
      token: login.token,
      tipo: login.tipo || "Bearer",
      username: login.username,
      rol: login.rol,
      expiraEn: Date.now() + (login.expiraEnSegundos || 1800) * 1000,
    };
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
    return session;
  },
  clear() {
    localStorage.removeItem(SESSION_KEY);
  },
  isValid() {
    const s = this.get();
    return Boolean(s && s.token && s.expiraEn > Date.now());
  },
  isAdmin() {
    const s = this.get();
    return Boolean(s && String(s.rol).replace("ROLE_", "") === "ADMIN");
  },
};

class ApiError extends Error {
  constructor(status, message, fieldErrors = null) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

async function api(path, { method = "GET", body, auth = true } = {}) {
  const headers = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";

  const session = Session.get();
  if (auth && session?.token) headers.Authorization = `${session.tipo} ${session.token}`;

  let res;
  try {
    res = await fetch(window.LOGITRACK_CONFIG.API_URL + path, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, "No se pudo conectar con el servidor. Verifica que el backend esté en ejecución.");
  }

  if (res.status === 204) return null;

  const text = await res.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }

  if (!res.ok) {
    if (res.status === 401 && auth) {
      Session.clear();
      window.location.href = "index.html?expired=1";
    }
    let message = `Error ${res.status}`;
    if (data && typeof data === "object") {
      if (data.errors && Object.keys(data.errors).length) {
        message = Object.values(data.errors).join(". ");
      } else if (data.message) {
        message = data.message;
      }
    } else if (typeof data === "string" && data) {
      message = data;
    }
    if (res.status === 403 && message === `Error 403`) message = "No tienes permisos para realizar esta acción.";
    throw new ApiError(res.status, message, data?.errors || null);
  }

  return data;
}
