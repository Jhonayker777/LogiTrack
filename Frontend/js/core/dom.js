export const $ = (selector, root = document) => root.querySelector(selector);
export const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];
export const esc = value => String(value ?? '—').replace(/[&<>\"']/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;','\"':'&quot;',"'":'&#39;'}[char]));
export const fmtDate = value => value ? new Date(value).toLocaleString('es-CO',{dateStyle:'medium',timeStyle:'short'}) : '—';
export const money = value => value == null ? '—' : new Intl.NumberFormat('es-CO',{style:'currency',currency:'COP',maximumFractionDigits:2}).format(value);
