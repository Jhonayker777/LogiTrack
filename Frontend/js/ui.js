function esc(value) {
  if (value === null || value === undefined) return "";
  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function fmtDate(value, withTime = true) {
  if (!value) return "—";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return esc(value);
  return d.toLocaleString("es-CO", withTime
    ? { dateStyle: "medium", timeStyle: "short" }
    : { dateStyle: "medium" });
}

function fmtMoney(value) {
  if (value === null || value === undefined || value === "") return "—";
  return Number(value).toLocaleString("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 });
}

function fmtNumber(value) {
  if (value === null || value === undefined) return "—";
  return Number(value).toLocaleString("es-CO");
}

function badge(text, tone = "neutral") {
  return `<span class="badge badge-${tone}">${esc(text)}</span>`;
}

function activeBadge(activo) {
  return activo === false ? badge("Inactivo", "muted") : badge("Activo", "success");
}

const TONES = {
  ENTRADA: "success",
  SALIDA: "danger",
  TRANSFERENCIA: "info",
  INSERT: "success",
  UPDATE: "info",
  DELETE: "danger",
  PENDIENTE: "warning",
  APROBADA: "success",
  RECHAZADA: "danger",
  ADMIN: "accent",
  EMPLEADO: "neutral",
};

function enumBadge(value) {
  return value ? badge(value, TONES[value] || "neutral") : "—";
}

function toast(message, type = "success") {
  let container = document.getElementById("toasts");
  if (!container) {
    container = document.createElement("div");
    container.id = "toasts";
    container.className = "toasts";
    container.setAttribute("role", "status");
    container.setAttribute("aria-live", "polite");
    document.body.appendChild(container);
  }
  const el = document.createElement("div");
  el.className = `toast toast-${type}`;
  el.textContent = message;
  container.appendChild(el);
  setTimeout(() => {
    el.classList.add("toast-hide");
    setTimeout(() => el.remove(), 300);
  }, 3800);
}

let lastFocused = null;

function openModal({ title, body, footer = "", size = "md" }) {
  closeModal();
  lastFocused = document.activeElement;
  const overlay = document.createElement("div");
  overlay.className = "modal-overlay";
  overlay.id = "modal";
  overlay.innerHTML = `
    <div class="modal modal-${size}" role="dialog" aria-modal="true" aria-labelledby="modal-title">
      <header class="modal-header">
        <h2 id="modal-title">${esc(title)}</h2>
        <button type="button" class="icon-btn" data-close aria-label="Cerrar">${icon("x")}</button>
      </header>
      <div class="modal-body">${body}</div>
      ${footer ? `<footer class="modal-footer">${footer}</footer>` : ""}
    </div>`;
  overlay.addEventListener("click", (e) => {
    if (e.target === overlay || e.target.closest("[data-close]")) closeModal();
  });
  document.body.appendChild(overlay);
  document.addEventListener("keydown", onModalKey);
  const focusable = overlay.querySelector("input, select, textarea, button:not([data-close])") || overlay.querySelector("button");
  focusable?.focus();
  return overlay;
}

function onModalKey(e) {
  if (e.key === "Escape") closeModal();
}

function closeModal() {
  const existing = document.getElementById("modal");
  if (existing) {
    existing.remove();
    document.removeEventListener("keydown", onModalKey);
    lastFocused?.focus?.();
  }
}

function confirmDialog(message, { confirmText = "Eliminar", danger = true } = {}) {
  return new Promise((resolve) => {
    const overlay = openModal({
      title: "Confirmar acción",
      size: "sm",
      body: `<p class="muted-text">${esc(message)}</p>`,
      footer: `
        <button type="button" class="btn btn-ghost" data-close>Cancelar</button>
        <button type="button" class="btn ${danger ? "btn-danger" : "btn-primary"}" data-confirm>${esc(confirmText)}</button>`,
    });
    let answered = false;
    overlay.querySelector("[data-confirm]").addEventListener("click", () => {
      answered = true;
      closeModal();
      resolve(true);
    });
    const observer = new MutationObserver(() => {
      if (!document.body.contains(overlay)) {
        observer.disconnect();
        if (!answered) resolve(false);
      }
    });
    observer.observe(document.body, { childList: true });
  });
}

/**
 * Formulario genérico dentro de un modal.
 * fields: [{ name, label, type, required, options | loadOptions, placeholder, min, step, full }]
 */
async function openForm({ title, fields, values = {}, submitText = "Guardar", onSubmit }) {
  const resolved = await Promise.all(
    fields.map(async (f) => {
      if (f.type === "select" && f.loadOptions) {
        try {
          return { ...f, options: await f.loadOptions() };
        } catch (err) {
          toast(`No se pudieron cargar opciones de ${f.label}: ${err.message}`, "error");
          return { ...f, options: [] };
        }
      }
      return f;
    }),
  );

  const body = `
    <form id="modal-form" class="form-grid" novalidate>
      ${resolved.map((f) => renderField(f, values[f.name])).join("")}
      <p class="form-error" id="form-error" role="alert" hidden></p>
    </form>`;
  const overlay = openModal({
    title,
    body,
    size: fields.length > 6 ? "lg" : "md",
    footer: `
      <button type="button" class="btn btn-ghost" data-close>Cancelar</button>
      <button type="submit" form="modal-form" class="btn btn-primary">${esc(submitText)}</button>`,
  });

  const form = overlay.querySelector("#modal-form");
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const errorEl = form.querySelector("#form-error");
    errorEl.hidden = true;
    form.querySelectorAll(".field-error").forEach((el) => el.remove());

    const data = {};
    let invalid = false;
    for (const f of resolved) {
      const input = form.elements[f.name];
      if (!input) continue;
      let v = input.value.trim();
      if (f.required && v === "") {
        markFieldError(input, "Este campo es obligatorio");
        invalid = true;
        continue;
      }
      if (v === "") v = null;
      else if (f.type === "number" || f.numeric) v = Number(v);
      data[f.name] = v;
    }
    if (invalid) return;

    const submitBtn = overlay.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.textContent = "Guardando…";
    try {
      await onSubmit(data);
      closeModal();
    } catch (err) {
      if (err.fieldErrors) {
        for (const [name, msg] of Object.entries(err.fieldErrors)) {
          const input = form.elements[name];
          if (input) markFieldError(input, msg);
        }
      }
      errorEl.textContent = err.message;
      errorEl.hidden = false;
      submitBtn.disabled = false;
      submitBtn.textContent = submitText;
    }
  });
  return overlay;
}

function markFieldError(input, message) {
  const p = document.createElement("p");
  p.className = "field-error";
  p.textContent = message;
  input.setAttribute("aria-invalid", "true");
  input.closest(".field").appendChild(p);
}

function renderField(f, value) {
  const id = `f-${f.name}`;
  const v = value === null || value === undefined ? "" : value;
  const req = f.required ? "required" : "";
  const label = `<label for="${id}">${esc(f.label)}${f.required ? ' <span class="req" aria-hidden="true">*</span>' : ""}</label>`;
  let control;
  if (f.type === "select") {
    const opts = (f.options || [])
      .map((o) => `<option value="${esc(o.value)}" ${String(o.value) === String(v) ? "selected" : ""}>${esc(o.label)}</option>`)
      .join("");
    control = `<select id="${id}" name="${f.name}" ${req}><option value="">${esc(f.placeholder || "Selecciona…")}</option>${opts}</select>`;
  } else if (f.type === "textarea") {
    control = `<textarea id="${id}" name="${f.name}" rows="3" ${req} placeholder="${esc(f.placeholder || "")}">${esc(v)}</textarea>`;
  } else {
    const extra = [
      f.min !== undefined ? `min="${f.min}"` : "",
      f.step !== undefined ? `step="${f.step}"` : "",
      f.autocomplete ? `autocomplete="${f.autocomplete}"` : "",
    ].join(" ");
    control = `<input id="${id}" name="${f.name}" type="${f.type || "text"}" value="${esc(v)}" ${req} ${extra} placeholder="${esc(f.placeholder || "")}">`;
  }
  return `<div class="field ${f.full ? "field-full" : ""}">${label}${control}${f.hint ? `<p class="hint">${esc(f.hint)}</p>` : ""}</div>`;
}

const ICONS = {
  dashboard: '<rect x="3" y="3" width="7" height="9" rx="1"/><rect x="14" y="3" width="7" height="5" rx="1"/><rect x="14" y="12" width="7" height="9" rx="1"/><rect x="3" y="16" width="7" height="5" rx="1"/>',
  movimientos: '<path d="m16 3 4 4-4 4"/><path d="M20 7H4"/><path d="m8 21-4-4 4-4"/><path d="M4 17h16"/>',
  stock: '<path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z"/><path d="m3.3 7 8.7 5 8.7-5"/><path d="M12 22V12"/>',
  productos: '<path d="M12.6 2.6A2 2 0 0 0 11.2 2H4a2 2 0 0 0-2 2v7.2a2 2 0 0 0 .6 1.4l8.7 8.7a2.4 2.4 0 0 0 3.4 0l6.6-6.6a2.4 2.4 0 0 0 0-3.4z"/><circle cx="7.5" cy="7.5" r=".5" fill="currentColor"/>',
  bodegas: '<path d="M22 8.35V20a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8.35A2 2 0 0 1 3.26 6.5l8-3.2a2 2 0 0 1 1.48 0l8 3.2A2 2 0 0 1 22 8.35Z"/><path d="M6 18h12"/><path d="M6 14h12"/><rect x="6" y="10" width="12" height="12"/>',
  ciudades: '<path d="M20 10c0 4.99-5.54 10.19-7.4 11.8a1 1 0 0 1-1.2 0C9.54 20.19 4 14.99 4 10a8 8 0 0 1 16 0"/><circle cx="12" cy="10" r="3"/>',
  departamentos: '<path d="M14.1 6.3 9.9 4.2a2 2 0 0 0-1.8 0L3.6 6.4A1 1 0 0 0 3 7.3v12.4a1 1 0 0 0 1.4.9l3.7-1.8a2 2 0 0 1 1.8 0l4.2 2.1a2 2 0 0 0 1.8 0l4.5-2.2a1 1 0 0 0 .6-.9V5.4a1 1 0 0 0-1.4-.9l-3.7 1.8a2 2 0 0 1-1.8 0z"/><path d="M15 6.8v14"/><path d="M9 3.2v14"/>',
  usuarios: '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>',
  solicitudes: '<path d="M2 21a8 8 0 0 1 13.29-6"/><circle cx="10" cy="8" r="5"/><path d="M19 16v6"/><path d="M22 19h-6"/>',
  auditoria: '<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10"/><path d="m9 12 2 2 4-4"/>',
  plus: '<path d="M5 12h14"/><path d="M12 5v14"/>',
  edit: '<path d="M21.17 6.81a1 1 0 0 0-3.99-3.99L3.84 16.17a2 2 0 0 0-.5.83l-1.32 4.35a.5.5 0 0 0 .62.62l4.35-1.32a2 2 0 0 0 .83-.5z"/>',
  trash: '<path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/>',
  eye: '<path d="M2.06 12.35a1 1 0 0 1 0-.7 10.75 10.75 0 0 1 19.88 0 1 1 0 0 1 0 .7 10.75 10.75 0 0 1-19.88 0"/><circle cx="12" cy="12" r="3"/>',
  x: '<path d="M18 6 6 18"/><path d="m6 6 12 12"/>',
  check: '<path d="M20 6 9 17l-5-5"/>',
  logout: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="m16 17 5-5-5-5"/><path d="M21 12H9"/>',
  menu: '<path d="M4 6h16"/><path d="M4 12h16"/><path d="M4 18h16"/>',
  search: '<circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>',
  alert: '<path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3"/><path d="M12 9v4"/><path d="M12 17h.01"/>',
  refresh: '<path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/><path d="M21 3v5h-5"/><path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/><path d="M8 16H3v5"/>',
  truck: '<path d="M14 18V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v11a1 1 0 0 0 1 1h2"/><path d="M15 18H9"/><path d="M19 18h2a1 1 0 0 0 1-1v-3.65a1 1 0 0 0-.22-.62l-3.48-4.35A1 1 0 0 0 17.52 8H14"/><circle cx="17" cy="18" r="2"/><circle cx="7" cy="18" r="2"/>',
};

function icon(name, size = 18) {
  return `<svg class="icon" width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${ICONS[name] || ""}</svg>`;
}

function get(obj, path) {
  return path.split(".").reduce((acc, key) => (acc == null ? acc : acc[key]), obj);
}
