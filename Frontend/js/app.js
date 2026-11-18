(function () {
  if (!Session.isValid()) {
    Session.clear();
    window.location.replace("index.html");
    return;
  }

  const session = Session.get();
  const isAdmin = Session.isAdmin();

  const NAV = [
    { group: "Operación", items: [
      { id: "dashboard", label: "Panel", icon: "dashboard" },
      { id: "movimientos", label: "Movimientos", icon: "movimientos" },
      { id: "stock", label: "Stock", icon: "stock" },
    ] },
    { group: "Catálogo", items: [
      { id: "productos", label: "Productos", icon: "productos" },
      { id: "bodegas", label: "Bodegas", icon: "bodegas" },
      { id: "ciudades", label: "Ciudades", icon: "ciudades" },
      { id: "departamentos", label: "Departamentos", icon: "departamentos" },
    ] },
    { group: "Administración", admin: true, items: [
      { id: "usuarios", label: "Usuarios", icon: "usuarios" },
      { id: "solicitudes", label: "Solicitudes", icon: "solicitudes" },
      { id: "auditoria", label: "Auditoría", icon: "auditoria" },
    ] },
  ];

  const $ = (sel) => document.querySelector(sel);
  const main = $("#main");
  const sidebar = $("#sidebar");
  const menuBtn = $("#menu-btn");

  // ---------- Shell ----------
  $("#brand-mark").innerHTML = icon("truck", 20);
  $("#user-name").textContent = session.username;
  $("#user-role").textContent = isAdmin ? "Administrador" : "Empleado";
  $("#user-avatar").textContent = (session.username || "?").slice(0, 2).toUpperCase();
  $("#logout-btn").innerHTML = `${icon("logout")}<span>Cerrar sesión</span>`;
  menuBtn.innerHTML = icon("menu", 20);

  $("#nav").innerHTML = NAV.filter((g) => !g.admin || isAdmin)
    .map(
      (g) => `
      <p class="nav-group">${esc(g.group)}</p>
      <ul class="nav-list">
        ${g.items.map((i) => `<li><a href="#/${i.id}" class="nav-link" data-route="${i.id}">${icon(i.icon)}<span>${esc(i.label)}</span></a></li>`).join("")}
      </ul>`,
    )
    .join("");

  function logout() {
    Session.clear();
    window.location.replace("index.html");
  }
  $("#logout-btn").addEventListener("click", logout);
  setTimeout(() => {
    Session.clear();
    window.location.replace("index.html?expired=1");
  }, Math.max(session.expiraEn - Date.now(), 0));

  function setSidebar(open) {
    document.body.classList.toggle("sidebar-open", open);
    menuBtn.setAttribute("aria-expanded", String(open));
  }
  menuBtn.addEventListener("click", () => setSidebar(!document.body.classList.contains("sidebar-open")));
  $("#sidebar-backdrop").addEventListener("click", () => setSidebar(false));
  sidebar.addEventListener("click", (e) => {
    if (e.target.closest(".nav-link")) setSidebar(false);
  });

  function setHeader(title, subtitle = "", actions = "") {
    $("#page-title").textContent = title;
    $("#page-subtitle").textContent = subtitle;
    $("#page-actions").innerHTML = actions;
    document.title = `${title} | LogiTrack`;
  }

  // ---------- Router ----------
  const VIEWS = { dashboard: renderDashboard };

  function route() {
    let id = location.hash.replace(/^#\/?/, "") || "dashboard";
    const cfg = RESOURCES[id];
    if (!VIEWS[id] && !cfg) id = "dashboard";
    if (cfg?.adminOnly && !isAdmin) id = "dashboard";

    document.querySelectorAll(".nav-link").forEach((a) => {
      const active = a.dataset.route === id;
      a.classList.toggle("is-active", active);
      if (active) a.setAttribute("aria-current", "page");
      else a.removeAttribute("aria-current");
    });

    document.querySelectorAll(".modal-overlay").forEach((o) => o.remove());
    main.innerHTML = "";
    if (VIEWS[id]) VIEWS[id]();
    else renderResource(id, RESOURCES[id]);
    main.focus({ preventScroll: true });
    window.scrollTo(0, 0);
  }
  window.addEventListener("hashchange", route);

  // ---------- Shared render helpers ----------
  function loadingState(rows = 5) {
    return `<div class="skeleton-list" aria-busy="true" aria-label="Cargando">${'<div class="skeleton"></div>'.repeat(rows)}</div>`;
  }

  function emptyState(text) {
    return `<div class="empty">${icon("search", 28)}<p>${esc(text)}</p></div>`;
  }

  function errorState(text) {
    return `<div class="empty empty-error">${icon("alert", 28)}<p>${esc(text)}</p><button type="button" class="btn btn-ghost" data-retry>${icon("refresh")}<span>Reintentar</span></button></div>`;
  }

  // ---------- Generic resource view ----------
  async function renderResource(id, cfg) {
    const canWrite = cfg.adminWrite && isAdmin;
    const canCreate = cfg.canCreate ? cfg.canCreate() : canWrite;

    setHeader(
      cfg.title,
      cfg.subtitle,
      `<button type="button" class="btn btn-ghost" data-refresh>${icon("refresh")}<span class="hide-sm">Actualizar</span></button>
       ${canCreate ? `<button type="button" class="btn btn-primary" data-create>${icon("plus")}<span>${esc(cfg.createLabel || `Nuevo ${cfg.singular}`)}</span></button>` : ""}`,
    );

    const filterState = {};
    (cfg.filters || []).forEach((f) => (filterState[f.name] = f.defaultValue || ""));

    main.innerHTML = `
      <section class="toolbar" aria-label="Filtros">
        <div class="search">
          ${icon("search")}
          <label for="table-search" class="sr-only">Buscar</label>
          <input id="table-search" type="search" placeholder="Buscar ${esc(cfg.title.toLowerCase())}…">
        </div>
        <div class="filters" id="filters"></div>
      </section>
      <p class="summary" id="summary" hidden></p>
      <section class="card table-card">
        <div id="table-wrap">${loadingState()}</div>
        <footer class="table-foot" id="table-foot"></footer>
      </section>`;

    const filtersEl = $("#filters");
    const wrap = $("#table-wrap");
    const foot = $("#table-foot");
    const summaryEl = $("#summary");

    for (const f of cfg.filters || []) {
      let control;
      if (f.type === "select") {
        let options = f.options || [];
        if (f.loadOptions) {
          try {
            options = await f.loadOptions();
          } catch {
            options = [];
          }
        }
        control = `<select id="flt-${f.name}" data-filter="${f.name}">
          <option value="">${esc(f.placeholder || "Todos")}</option>
          ${options.map((o) => `<option value="${esc(o.value)}" ${String(o.value) === String(filterState[f.name]) ? "selected" : ""}>${esc(o.label)}</option>`).join("")}
        </select>`;
      } else {
        control = `<input id="flt-${f.name}" type="${f.type}" data-filter="${f.name}" value="${esc(filterState[f.name])}">`;
      }
      filtersEl.insertAdjacentHTML("beforeend", `<div class="filter"><label for="flt-${f.name}">${esc(f.label)}</label>${control}</div>`);
    }
    if (!main.contains(filtersEl)) return;

    let rows = [];
    let query = "";

    async function load() {
      wrap.innerHTML = loadingState();
      foot.textContent = "";
      summaryEl.hidden = true;
      try {
        rows = (await (cfg.load ? cfg.load(filterState) : api(cfg.endpoint))) || [];
        draw();
        if (cfg.summary) {
          const html = await cfg.summary(filterState).catch(() => "");
          summaryEl.innerHTML = html;
          summaryEl.hidden = !html;
        }
      } catch (err) {
        wrap.innerHTML = errorState(err.message);
      }
    }

    function draw() {
      const q = query.toLowerCase();
      const filtered = q
        ? rows.filter((r) => cfg.search(r).filter(Boolean).join(" ").toLowerCase().includes(q))
        : rows;
      foot.textContent = `${filtered.length} de ${rows.length} registros`;
      if (!filtered.length) {
        wrap.innerHTML = emptyState(rows.length ? "No hay resultados para tu búsqueda." : `Aún no hay ${cfg.title.toLowerCase()} registrados.`);
        return;
      }
      const hasActions = canWrite || cfg.rowActions;
      wrap.innerHTML = `
        <div class="table-scroll">
          <table class="table">
            <thead><tr>
              ${cfg.columns.map((c) => `<th scope="col" class="${c.cls || ""}">${esc(c.label)}</th>`).join("")}
              ${hasActions ? '<th scope="col" class="actions-col"><span class="sr-only">Acciones</span></th>' : ""}
            </tr></thead>
            <tbody>
              ${filtered
                .map((r) => {
                  const actions = [...(cfg.rowActions ? cfg.rowActions(r) : [])];
                  if (canWrite) {
                    actions.push({ action: "edit", icon: "edit", label: "Editar" });
                    actions.push({ action: "delete", icon: "trash", label: "Eliminar", tone: "danger" });
                  }
                  return `<tr>
                    ${cfg.columns.map((c) => `<td class="${c.cls || ""}" data-label="${esc(c.label)}">${c.value(r)}</td>`).join("")}
                    ${hasActions ? `<td class="actions-col"><div class="row-actions">
                      ${actions.map((a) => `<button type="button" class="icon-btn ${a.tone ? `icon-btn-${a.tone}` : ""}" data-action="${a.action}" data-id="${esc(r.id)}" title="${esc(a.label)}" aria-label="${esc(a.label)} ${esc(cfg.singular)} #${esc(r.id)}">${icon(a.icon, 16)}</button>`).join("")}
                    </div></td>` : ""}
                  </tr>`;
                })
                .join("")}
            </tbody>
          </table>
        </div>`;
    }

    $("#table-search").addEventListener("input", (e) => {
      query = e.target.value.trim();
      draw();
    });

    filtersEl.addEventListener("change", (e) => {
      const name = e.target.dataset.filter;
      if (!name) return;
      filterState[name] = e.target.value;
      load();
    });

    $("#page-actions").querySelector("[data-refresh]").addEventListener("click", load);

    const createBtn = $("#page-actions").querySelector("[data-create]");
    createBtn?.addEventListener("click", () => {
      if (id === "movimientos") return openMovementForm(load);
      openForm({
        title: `Nuevo ${cfg.singular}`,
        fields: cfg.fields,
        submitText: "Crear",
        onSubmit: async (data) => {
          await api(cfg.endpoint, { method: "POST", body: data });
          toast(`${capitalize(cfg.singular)} creado correctamente.`);
          load();
        },
      });
    });

    wrap.addEventListener("click", async (e) => {
      if (e.target.closest("[data-retry]")) return load();
      const btn = e.target.closest("[data-action]");
      if (!btn) return;
      const row = rows.find((r) => String(r.id) === btn.dataset.id);
      if (!row) return;
      const action = btn.dataset.action;

      if (action === "edit") {
        openForm({
          title: `Editar ${cfg.singular} #${row.id}`,
          fields: cfg.fields,
          values: cfg.toForm ? cfg.toForm(row) : row,
          onSubmit: async (data) => {
            await api(`${cfg.endpoint}/${row.id}`, { method: "PUT", body: data });
            toast(`${capitalize(cfg.singular)} actualizado.`);
            load();
          },
        });
      } else if (action === "delete") {
        const ok = await confirmDialog(`¿Seguro que deseas eliminar ${cfg.singular} #${row.id}? Esta acción no se puede deshacer.`);
        if (!ok) return;
        try {
          await api(`${cfg.endpoint}/${row.id}`, { method: "DELETE" });
          toast(`${capitalize(cfg.singular)} eliminado.`);
          load();
        } catch (err) {
          toast(err.message, "error");
        }
      } else if (action === "view") {
        if (id === "movimientos") showMovement(row);
        else if (id === "solicitudes") showSolicitud(row);
        else if (id === "auditoria") showAuditoria(row);
      } else if (action === "approve") {
        const ok = await confirmDialog(`¿Aprobar la solicitud de ${row.nombre} ${row.apellido}? Se creará su usuario con rol ${row.rolSolicitado}.`, { confirmText: "Aprobar", danger: false });
        if (!ok) return;
        try {
          await api(`/api/solicitudes/${row.id}/aprobar`, { method: "PUT" });
          toast("Solicitud aprobada. El usuario ya puede iniciar sesión.");
          load();
        } catch (err) {
          toast(err.message, "error");
        }
      } else if (action === "reject") {
        openForm({
          title: `Rechazar solicitud #${row.id}`,
          submitText: "Rechazar",
          fields: [{ name: "motivo", label: "Motivo del rechazo", type: "textarea", required: true, full: true }],
          onSubmit: async (data) => {
            await api(`/api/solicitudes/${row.id}/rechazar`, { method: "PUT", body: data });
            toast("Solicitud rechazada.");
            load();
          },
        });
      }
    });

    load();
  }

  function capitalize(s) {
    return s.charAt(0).toUpperCase() + s.slice(1);
  }

  // ---------- Movimientos ----------
  async function openMovementForm(onDone) {
    const overlay = await openForm({
      title: "Registrar movimiento",
      submitText: "Registrar",
      fields: [
        {
          name: "tipo",
          label: "Tipo de movimiento",
          type: "select",
          required: true,
          options: [
            { value: "ENTRADA", label: "Entrada (ingreso a bodega)" },
            { value: "SALIDA", label: "Salida (despacho de bodega)" },
            { value: "TRANSFERENCIA", label: "Transferencia entre bodegas" },
          ],
          full: true,
        },
        { name: "productoId", label: "Producto", type: "select", required: true, numeric: true, loadOptions: Options.productos },
        { name: "cantidad", label: "Cantidad", type: "number", required: true, min: 1 },
        { name: "bodegaOrigenId", label: "Bodega de origen", type: "select", numeric: true, loadOptions: Options.bodegas },
        { name: "bodegaDestinoId", label: "Bodega de destino", type: "select", numeric: true, loadOptions: Options.bodegas },
        { name: "observacion", label: "Observación", type: "textarea", full: true, placeholder: "Ej: Factura #123, proveedor…" },
      ],
      onSubmit: async (data) => {
        if (data.tipo === "ENTRADA") data.bodegaOrigenId = null;
        if (data.tipo === "SALIDA") data.bodegaDestinoId = null;
        if (data.tipo !== "SALIDA" && !data.bodegaDestinoId) throw new Error("Selecciona la bodega de destino.");
        if (data.tipo !== "ENTRADA" && !data.bodegaOrigenId) throw new Error("Selecciona la bodega de origen.");
        if (data.tipo === "TRANSFERENCIA" && data.bodegaOrigenId === data.bodegaDestinoId)
          throw new Error("La bodega de origen y destino no pueden ser la misma.");
        await api("/api/movimientos", { method: "POST", body: data });
        toast("Movimiento registrado y stock actualizado.");
        onDone?.();
      },
    });

    const form = overlay.querySelector("#modal-form");
    const origenField = form.elements.bodegaOrigenId.closest(".field");
    const destinoField = form.elements.bodegaDestinoId.closest(".field");
    const hint = document.createElement("p");
    hint.className = "hint";
    hint.setAttribute("aria-live", "polite");
    origenField.appendChild(hint);

    function syncTipo() {
      const tipo = form.elements.tipo.value;
      origenField.hidden = !(tipo === "SALIDA" || tipo === "TRANSFERENCIA");
      destinoField.hidden = !(tipo === "ENTRADA" || tipo === "TRANSFERENCIA");
      updateStockHint();
    }

    async function updateStockHint() {
      const bodega = form.elements.bodegaOrigenId.value;
      const producto = form.elements.productoId.value;
      if (origenField.hidden || !bodega || !producto) {
        hint.textContent = "";
        return;
      }
      hint.textContent = "Consultando stock…";
      try {
        const s = await api(`/api/stock/bodega/${bodega}/producto/${producto}`);
        hint.textContent = `Disponible en esta bodega: ${fmtNumber(s.cantidad)} unidades`;
      } catch (err) {
        hint.textContent = err.status === 404 ? "Este producto no tiene stock en la bodega seleccionada." : "";
      }
    }

    form.elements.tipo.addEventListener("change", syncTipo);
    form.elements.bodegaOrigenId.addEventListener("change", updateStockHint);
    form.elements.productoId.addEventListener("change", updateStockHint);
    syncTipo();
  }

  async function showMovement(m) {
    const overlay = openModal({
      title: `Movimiento #${m.id}`,
      size: "lg",
      body: `
        <dl class="detail-grid">
          <div><dt>Tipo</dt><dd>${enumBadge(m.tipo)}</dd></div>
          <div><dt>Fecha</dt><dd>${fmtDate(m.fecha)}</dd></div>
          <div><dt>Bodega origen</dt><dd>${esc(m.bodegaOrigen?.nombre) || "—"}</dd></div>
          <div><dt>Bodega destino</dt><dd>${esc(m.bodegaDestino?.nombre) || "—"}</dd></div>
          <div><dt>Registrado por</dt><dd>${personName(m.usuario)}</dd></div>
          <div class="detail-full"><dt>Observación</dt><dd>${esc(m.observacion) || "—"}</dd></div>
        </dl>
        <h3 class="section-title">Productos del movimiento</h3>
        <div id="mov-detalles">${loadingState(2)}</div>`,
    });
    const target = overlay.querySelector("#mov-detalles");
    try {
      const detalles = await api(`/api/detalles-movimiento/movimiento/${m.id}`);
      target.innerHTML = detalles.length
        ? `<div class="table-scroll"><table class="table table-compact">
            <thead><tr><th scope="col">Producto</th><th scope="col">Categoría</th><th scope="col" class="num">Cantidad</th><th scope="col" class="num">Valor</th></tr></thead>
            <tbody>${detalles
              .map((d) => `<tr>
                <td>${esc(d.producto?.nombre)}</td>
                <td>${esc(d.producto?.categoria)}</td>
                <td class="num">${fmtNumber(d.cantidad)}</td>
                <td class="num">${fmtMoney((d.producto?.precio || 0) * d.cantidad)}</td>
              </tr>`)
              .join("")}</tbody>
          </table></div>`
        : emptyState("Este movimiento no tiene detalles.");
    } catch (err) {
      target.innerHTML = `<p class="form-error">${esc(err.message)}</p>`;
    }
  }

  function showSolicitud(s) {
    openModal({
      title: `Solicitud #${s.id}`,
      size: "lg",
      body: `
        <dl class="detail-grid">
          <div><dt>Nombre</dt><dd>${esc(s.nombre)} ${esc(s.apellido)}</dd></div>
          <div><dt>Documento</dt><dd>${esc(s.documento)}</dd></div>
          <div><dt>Correo</dt><dd>${esc(s.email)}</dd></div>
          <div><dt>Teléfono</dt><dd>${esc(s.telefono) || "—"}</dd></div>
          <div><dt>Dirección</dt><dd>${esc(s.direccion) || "—"}</dd></div>
          <div><dt>Ciudad</dt><dd>${esc(s.ciudad?.nombre) || "—"}</dd></div>
          <div><dt>Usuario</dt><dd class="mono">${esc(s.username)}</dd></div>
          <div><dt>Rol solicitado</dt><dd>${enumBadge(s.rolSolicitado)}</dd></div>
          <div><dt>Turno</dt><dd>${esc(s.turno) || "—"}</dd></div>
          <div><dt>Fecha de ingreso</dt><dd>${fmtDate(s.fechaIngreso, false)}</dd></div>
          <div><dt>Estado</dt><dd>${enumBadge(s.estado)}</dd></div>
          <div><dt>Fecha solicitud</dt><dd>${fmtDate(s.fechaSolicitud)}</dd></div>
          ${s.fechaResolucion ? `<div><dt>Resuelta</dt><dd>${fmtDate(s.fechaResolucion)}</dd></div>` : ""}
          ${s.resueltoPor ? `<div><dt>Resuelta por</dt><dd>${personName(s.resueltoPor)}</dd></div>` : ""}
          ${s.motivoRechazo ? `<div class="detail-full"><dt>Motivo de rechazo</dt><dd>${esc(s.motivoRechazo)}</dd></div>` : ""}
        </dl>`,
    });
  }

  function prettyJson(value) {
    if (!value) return '<span class="muted-text">— Sin datos —</span>';
    try {
      return esc(JSON.stringify(JSON.parse(value), null, 2));
    } catch {
      return esc(value);
    }
  }

  function showAuditoria(a) {
    openModal({
      title: `Auditoría #${a.id}`,
      size: "lg",
      body: `
        <dl class="detail-grid">
          <div><dt>Operación</dt><dd>${enumBadge(a.operacion)}</dd></div>
          <div><dt>Entidad</dt><dd>${esc(a.entidadAfectada)} #${esc(a.registroId)}</dd></div>
          <div><dt>Usuario</dt><dd>${personName(a.usuario)}</dd></div>
          <div><dt>Fecha</dt><dd>${fmtDate(a.fechaHora)}</dd></div>
          <div><dt>IP de origen</dt><dd class="mono">${esc(a.ipOrigen) || "—"}</dd></div>
          <div class="detail-full"><dt>Navegador</dt><dd class="cell-sub">${esc(a.userAgent) || "—"}</dd></div>
        </dl>
        <div class="diff">
          <div><h3 class="section-title">Valores anteriores</h3><pre class="code">${prettyJson(a.valoresAnteriores)}</pre></div>
          <div><h3 class="section-title">Valores nuevos</h3><pre class="code">${prettyJson(a.valoresNuevos)}</pre></div>
        </div>`,
    });
  }

  // ---------- Dashboard ----------
  async function renderDashboard() {
    setHeader(
      `Hola, ${session.username}`,
      "Resumen general del inventario.",
      `<a href="#/movimientos" class="btn btn-primary" id="quick-mov">${icon("plus")}<span>Registrar movimiento</span></a>`,
    );
    $("#quick-mov").addEventListener("click", (e) => {
      e.preventDefault();
      openMovementForm(() => renderDashboard());
    });

    main.innerHTML = `
      <section class="stats" id="stats">${'<div class="stat skeleton-stat"></div>'.repeat(4)}</section>
      <div class="dash-grid">
        <section class="card">
          <header class="card-header"><h2>Movimientos recientes</h2><a href="#/movimientos" class="link">Ver todos</a></header>
          <div id="recent">${loadingState(4)}</div>
        </section>
        <section class="card">
          <header class="card-header"><h2>Productos con stock bajo</h2><a href="#/productos" class="link">Ver catálogo</a></header>
          <div id="low">${loadingState(4)}</div>
        </section>
        <section class="card">
          <header class="card-header"><h2>Movimientos por tipo</h2></header>
          <div id="by-type">${loadingState(3)}</div>
        </section>
        <section class="card">
          <header class="card-header"><h2>Ocupación de bodegas</h2><a href="#/stock" class="link">Ver stock</a></header>
          <div id="occupancy">${loadingState(3)}</div>
        </section>
      </div>`;

    const [productos, bodegas, movimientos, bajo, stock, pendientes] = await Promise.all([
      api("/api/productos").catch(() => null),
      api("/api/bodegas").catch(() => null),
      api("/api/movimientos").catch(() => null),
      api("/api/productos/stock-bajo").catch(() => null),
      api("/api/stock").catch(() => null),
      isAdmin ? api("/api/solicitudes/estado/PENDIENTE").catch(() => null) : Promise.resolve(null),
    ]);
    if (!main.querySelector("#stats")) return;

    if (!productos && !bodegas && !movimientos) {
      main.innerHTML = errorState("No se pudo conectar con el servidor. Verifica que el backend esté en ejecución.");
      main.querySelector("[data-retry]").addEventListener("click", renderDashboard);
      return;
    }

    const totalUnidades = (stock || []).reduce((sum, s) => sum + (s.cantidad || 0), 0);
    const stats = [
      { label: "Productos activos", value: (productos || []).filter((p) => p.activo !== false).length, icon: "productos", href: "#/productos" },
      { label: "Bodegas", value: (bodegas || []).filter((b) => b.activo !== false).length, icon: "bodegas", href: "#/bodegas" },
      { label: "Unidades en stock", value: totalUnidades, icon: "stock", href: "#/stock" },
      isAdmin
        ? { label: "Solicitudes pendientes", value: (pendientes || []).length, icon: "solicitudes", href: "#/solicitudes", tone: (pendientes || []).length ? "warning" : "" }
        : { label: "Productos en stock bajo", value: (bajo || []).length, icon: "alert", href: "#/productos", tone: (bajo || []).length ? "warning" : "" },
    ];
    $("#stats").innerHTML = stats
      .map(
        (s) => `<a class="stat ${s.tone ? `stat-${s.tone}` : ""}" href="${s.href}">
          <span class="stat-icon">${icon(s.icon, 20)}</span>
          <span class="stat-value">${fmtNumber(s.value)}</span>
          <span class="stat-label">${esc(s.label)}</span>
        </a>`,
      )
      .join("");

    const recent = [...(movimientos || [])].sort((a, b) => b.id - a.id).slice(0, 6);
    $("#recent").innerHTML = recent.length
      ? `<ul class="feed">${recent
          .map(
            (m) => `<li>
              ${enumBadge(m.tipo)}
              <div class="feed-body">
                <span>${m.tipo === "TRANSFERENCIA" ? `${esc(m.bodegaOrigen?.nombre)} → ${esc(m.bodegaDestino?.nombre)}` : esc((m.bodegaDestino || m.bodegaOrigen)?.nombre)}</span>
                <span class="cell-sub">${personName(m.usuario)} · ${fmtDate(m.fecha)}</span>
              </div>
            </li>`,
          )
          .join("")}</ul>`
      : emptyState("Todavía no hay movimientos registrados.");

    $("#low").innerHTML = (bajo || []).length
      ? `<ul class="feed">${bajo
          .slice(0, 6)
          .map(
            (p) => `<li>
              <span class="dot dot-warning" aria-hidden="true"></span>
              <div class="feed-body"><span>${esc(p.nombre)}</span><span class="cell-sub">${esc(p.categoria)}</span></div>
              <span class="cell-sub">Mín. ${fmtNumber(p.stockMinimo)}</span>
            </li>`,
          )
          .join("")}</ul>`
      : emptyState("Todos los productos están por encima del stock mínimo.");

    const counts = { ENTRADA: 0, SALIDA: 0, TRANSFERENCIA: 0 };
    (movimientos || []).forEach((m) => (counts[m.tipo] = (counts[m.tipo] || 0) + 1));
    const maxCount = Math.max(1, ...Object.values(counts));
    $("#by-type").innerHTML = `<ul class="bars">${Object.entries(counts)
      .map(
        ([tipo, n]) => `<li>
          <div class="bar-label"><span>${esc(tipo.charAt(0) + tipo.slice(1).toLowerCase())}</span><strong>${fmtNumber(n)}</strong></div>
          <div class="bar-track"><div class="bar bar-${TONES[tipo]}" style="width:${(n / maxCount) * 100}%"></div></div>
        </li>`,
      )
      .join("")}</ul>`;

    const perBodega = (bodegas || [])
      .filter((b) => b.activo !== false)
      .map((b) => {
        const used = (stock || []).filter((s) => s.bodega?.id === b.id).reduce((sum, s) => sum + (s.cantidad || 0), 0);
        return { nombre: b.nombre, used, capacidad: b.capacidad || 0 };
      })
      .slice(0, 6);
    $("#occupancy").innerHTML = perBodega.length
      ? `<ul class="bars">${perBodega
          .map((b) => {
            const pct = b.capacidad ? Math.min(100, Math.round((b.used / b.capacidad) * 100)) : 0;
            const tone = pct >= 90 ? "danger" : pct >= 70 ? "warning" : "accent";
            return `<li>
              <div class="bar-label"><span>${esc(b.nombre)}</span><span class="cell-sub">${fmtNumber(b.used)} / ${fmtNumber(b.capacidad)} · <strong>${pct}%</strong></span></div>
              <div class="bar-track" role="progressbar" aria-valuenow="${pct}" aria-valuemin="0" aria-valuemax="100" aria-label="Ocupación ${esc(b.nombre)}"><div class="bar bar-${tone}" style="width:${pct}%"></div></div>
            </li>`;
          })
          .join("")}</ul>`
      : emptyState("No hay bodegas registradas.");
  }

  route();
})();
