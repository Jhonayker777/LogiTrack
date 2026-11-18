const saved = (() => { try { return JSON.parse(localStorage.getItem('logitrack_user') || '{}'); } catch { return {}; } })();
export const session = { username: 'Usuario', rol: 'EMPLEADO', ...saved };
export const isAdmin = () => session.rol === 'ADMIN';
export function clearSession() { localStorage.removeItem('logitrack_token'); localStorage.removeItem('logitrack_user'); }
