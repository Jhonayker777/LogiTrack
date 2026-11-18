const Options = {
  departamentos: () =>
    api("/api/departamentos").then((l) => l.map((d) => ({ value: d.id, label: `${d.nombre} (${d.pais})` }))),
  ciudades: () =>
    api("/api/ciudades").then((l) =>
      l.map((c) => ({ value: c.id, label: c.departamento ? `${c.nombre} — ${c.departamento.nombre}` : c.nombre })),
    ),
  bodegas: () =>
    api("/api/bodegas").then((l) => l.filter((b) => b.activo !== false).map((b) => ({ value: b.id, label: b.nombre }))),
  productos: () =>
    api("/api/productos").then((l) =>
      l.filter((p) => p.activo !== false).map((p) => ({ value: p.id, label: `${p.nombre} · ${p.categoria}` })),
    ),
  usuarios: () =>
    api("/api/usuarios").then((l) =>
      l.filter((u) => u.activo !== false).map((u) => ({ value: u.id, label: `${u.nombre} ${u.apellido} (${u.username})` })),
    ),
};

const ROLES = [
  { value: "EMPLEADO", label: "Empleado" },
  { value: "ADMIN", label: "Administrador" },
];
const TURNOS = [
  { value: "MAÑANA", label: "Mañana" },
  { value: "TARDE", label: "Tarde" },
  { value: "NOCHE", label: "Noche" },
];

const idCol = { label: "ID", value: (r) => `<span class="mono">#${esc(r.id)}</span>` };

function personName(u) {
  if (!u) return "—";
  return esc(u.nombreCompleto || `${u.nombre ?? ""} ${u.apellido ?? ""}`.trim() || u.username);
}

const RESOURCES = {
  departamentos: {
    title: "Departamentos",
    subtitle: "Regiones geográficas donde operan las bodegas.",
    singular: "departamento",
    endpoint: "/api/departamentos",
    adminWrite: true,
    columns: [
      idCol,
      { label: "Nombre", value: (r) => `<strong>${esc(r.nombre)}</strong>` },
      { label: "País", value: (r) => esc(r.pais) },
    ],
    search: (r) => [r.nombre, r.pais],
    fields: [
      { name: "nombre", label: "Nombre", required: true },
      { name: "pais", label: "País", required: true, placeholder: "Colombia" },
    ],
  },

  ciudades: {
    title: "Ciudades",
    subtitle: "Ciudades registradas y su departamento.",
    singular: "ciudad",
    endpoint: "/api/ciudades",
    adminWrite: true,
    filters: [{ name: "departamento", label: "Departamento", type: "select", loadOptions: Options.departamentos, placeholder: "Todos los departamentos" }],
    load: (f) => api(f.departamento ? `/api/ciudades/departamento/${f.departamento}` : "/api/ciudades"),
    columns: [
      idCol,
      { label: "Nombre", value: (r) => `<strong>${esc(r.nombre)}</strong>` },
      { label: "Código postal", value: (r) => `<span class="mono">${esc(r.codigoPostal) || "—"}</span>` },
      { label: "Departamento", value: (r) => esc(r.departamento?.nombre) || "—" },
    ],
    search: (r) => [r.nombre, r.codigoPostal, r.departamento?.nombre],
    fields: [
      { name: "nombre", label: "Nombre", required: true },
      { name: "codigoPostal", label: "Código postal" },
      { name: "departamentoId", label: "Departamento", type: "select", required: true, numeric: true, loadOptions: Options.departamentos, full: true },
    ],
    toForm: (r) => ({ ...r, departamentoId: r.departamento?.id }),
  },

  bodegas: {
    title: "Bodegas",
    subtitle: "Centros de almacenamiento, capacidad y encargados.",
    singular: "bodega",
    endpoint: "/api/bodegas",
    adminWrite: true,
    filters: [{ name: "ciudad", label: "Ciudad", type: "select", loadOptions: Options.ciudades, placeholder: "Todas las ciudades" }],
    load: (f) => api(f.ciudad ? `/api/bodegas/ciudad/${f.ciudad}` : "/api/bodegas"),
    columns: [
      idCol,
      { label: "Nombre", value: (r) => `<strong>${esc(r.nombre)}</strong><div class="cell-sub">${esc(r.ubicacion)}</div>` },
      { label: "Ciudad", value: (r) => esc(r.ciudad?.nombre) || "—" },
      { label: "Capacidad", value: (r) => fmtNumber(r.capacidad), cls: "num" },
      { label: "Encargado", value: (r) => personName(r.encargado) },
      { label: "Estado", value: (r) => activeBadge(r.activo) },
    ],
    search: (r) => [r.nombre, r.ubicacion, r.ciudad?.nombre, r.encargado?.nombreCompleto],
    fields: [
      { name: "nombre", label: "Nombre", required: true },
      { name: "capacidad", label: "Capacidad", type: "number", required: true, min: 1 },
      { name: "ubicacion", label: "Ubicación / dirección", required: true, full: true },
      { name: "ciudadId", label: "Ciudad", type: "select", required: true, numeric: true, loadOptions: Options.ciudades },
      { name: "encargadoId", label: "Encargado", type: "select", required: true, numeric: true, loadOptions: Options.usuarios },
    ],
    toForm: (r) => ({ ...r, ciudadId: r.ciudad?.id, encargadoId: r.encargado?.id }),
  },

  productos: {
    title: "Productos",
    subtitle: "Catálogo de productos, precios y stock mínimo.",
    singular: "producto",
    endpoint: "/api/productos",
    adminWrite: true,
    filters: [
      {
        name: "vista",
        label: "Vista",
        type: "select",
        placeholder: "Todos los productos",
        options: [{ value: "bajo", label: "Solo con stock bajo" }],
      },
    ],
    load: (f) => api(f.vista === "bajo" ? "/api/productos/stock-bajo" : "/api/productos"),
    columns: [
      idCol,
      { label: "Producto", value: (r) => `<strong>${esc(r.nombre)}</strong>` },
      { label: "Categoría", value: (r) => badge(r.categoria, "neutral") },
      { label: "Precio", value: (r) => fmtMoney(r.precio), cls: "num" },
      { label: "Stock mínimo", value: (r) => fmtNumber(r.stockMinimo), cls: "num" },
      { label: "Estado", value: (r) => activeBadge(r.activo) },
    ],
    search: (r) => [r.nombre, r.categoria],
    fields: [
      { name: "nombre", label: "Nombre", required: true, full: true },
      { name: "categoria", label: "Categoría", required: true },
      { name: "precio", label: "Precio", type: "number", required: true, min: 0, step: "0.01" },
      { name: "stockMinimo", label: "Stock mínimo", type: "number", required: true, min: 0 },
    ],
  },

  stock: {
    title: "Stock por bodega",
    subtitle: "Existencias de cada producto en cada bodega.",
    singular: "registro de stock",
    endpoint: "/api/stock",
    adminWrite: true,
    filters: [
      { name: "bodega", label: "Bodega", type: "select", loadOptions: Options.bodegas, placeholder: "Todas las bodegas" },
      { name: "producto", label: "Producto", type: "select", loadOptions: Options.productos, placeholder: "Todos los productos" },
    ],
    load: async (f) => {
      if (f.bodega && f.producto) {
        try {
          return [await api(`/api/stock/bodega/${f.bodega}/producto/${f.producto}`)];
        } catch (err) {
          if (err.status === 404) return [];
          throw err;
        }
      }
      if (f.bodega) return api(`/api/stock/bodega/${f.bodega}`);
      if (f.producto) return api(`/api/stock/producto/${f.producto}`);
      return api("/api/stock");
    },
    summary: async (f) => {
      if (!f.producto || f.bodega) return "";
      const total = await api(`/api/stock/producto/${f.producto}/total`);
      return `Stock total del producto en todas las bodegas: <strong>${fmtNumber(total)}</strong> unidades`;
    },
    columns: [
      idCol,
      { label: "Bodega", value: (r) => `<strong>${esc(r.bodega?.nombre)}</strong><div class="cell-sub">${esc(r.bodega?.ciudad?.nombre)}</div>` },
      { label: "Producto", value: (r) => `${esc(r.producto?.nombre)}<div class="cell-sub">${esc(r.producto?.categoria)}</div>` },
      {
        label: "Cantidad",
        cls: "num",
        value: (r) => {
          const low = r.producto && r.cantidad <= r.producto.stockMinimo;
          return `<span class="qty ${low ? "qty-low" : ""}">${fmtNumber(r.cantidad)}</span>${low ? `<div class="cell-sub warn-text">Mín. ${fmtNumber(r.producto.stockMinimo)}</div>` : ""}`;
        },
      },
    ],
    search: (r) => [r.bodega?.nombre, r.producto?.nombre, r.producto?.categoria],
    fields: [
      { name: "bodegaId", label: "Bodega", type: "select", required: true, numeric: true, loadOptions: Options.bodegas },
      { name: "productoId", label: "Producto", type: "select", required: true, numeric: true, loadOptions: Options.productos },
      { name: "cantidad", label: "Cantidad", type: "number", required: true, min: 0, full: true },
    ],
    toForm: (r) => ({ ...r, bodegaId: r.bodega?.id, productoId: r.producto?.id }),
  },

  usuarios: {
    title: "Usuarios",
    subtitle: "Personal con acceso al sistema.",
    singular: "usuario",
    endpoint: "/api/usuarios",
    adminOnly: true,
    adminWrite: true,
    columns: [
      idCol,
      { label: "Nombre", value: (r) => `<strong>${esc(r.nombre)} ${esc(r.apellido)}</strong><div class="cell-sub">${esc(r.email)}</div>` },
      { label: "Usuario", value: (r) => `<span class="mono">${esc(r.username)}</span>` },
      { label: "Rol", value: (r) => enumBadge(r.rol) },
      { label: "Ciudad", value: (r) => esc(r.ciudad?.nombre) || "—" },
      { label: "Turno", value: (r) => esc(r.turno) || "—" },
      { label: "Estado", value: (r) => activeBadge(r.activo) },
    ],
    search: (r) => [r.nombre, r.apellido, r.username, r.email, r.documento],
    fields: [
      { name: "nombre", label: "Nombre", required: true },
      { name: "apellido", label: "Apellido", required: true },
      { name: "documento", label: "Documento", required: true },
      { name: "email", label: "Correo", type: "email", required: true },
      { name: "telefono", label: "Teléfono", type: "tel" },
      { name: "direccion", label: "Dirección" },
      { name: "ciudadId", label: "Ciudad", type: "select", required: true, numeric: true, loadOptions: Options.ciudades },
      { name: "username", label: "Usuario", required: true },
      { name: "rol", label: "Rol", type: "select", required: true, options: ROLES },
      { name: "turno", label: "Turno", type: "select", options: TURNOS },
      { name: "fechaIngreso", label: "Fecha de ingreso", type: "date" },
    ],
    toForm: (r) => ({ ...r, ciudadId: r.ciudad?.id }),
  },

  movimientos: {
    title: "Movimientos",
    subtitle: "Entradas, salidas y transferencias de inventario.",
    singular: "movimiento",
    endpoint: "/api/movimientos",
    canCreate: () => true,
    createLabel: "Registrar movimiento",
    filters: [
      {
        name: "tipo",
        label: "Tipo",
        type: "select",
        placeholder: "Todos los tipos",
        options: [
          { value: "ENTRADA", label: "Entradas" },
          { value: "SALIDA", label: "Salidas" },
          { value: "TRANSFERENCIA", label: "Transferencias" },
        ],
      },
    ],
    load: async (f) => {
      const list = await api("/api/movimientos");
      return list.filter((m) => !f.tipo || m.tipo === f.tipo).sort((a, b) => b.id - a.id);
    },
    columns: [
      idCol,
      { label: "Tipo", value: (r) => enumBadge(r.tipo) },
      { label: "Fecha", value: (r) => fmtDate(r.fecha) },
      { label: "Origen", value: (r) => esc(r.bodegaOrigen?.nombre) || '<span class="muted-text">—</span>' },
      { label: "Destino", value: (r) => esc(r.bodegaDestino?.nombre) || '<span class="muted-text">—</span>' },
      { label: "Registrado por", value: (r) => personName(r.usuario) },
    ],
    search: (r) => [r.tipo, r.bodegaOrigen?.nombre, r.bodegaDestino?.nombre, r.usuario?.nombreCompleto, r.observacion],
    rowActions: () => [{ action: "view", icon: "eye", label: "Ver detalle" }],
  },

  solicitudes: {
    title: "Solicitudes de registro",
    subtitle: "Aprueba o rechaza las solicitudes de acceso.",
    singular: "solicitud",
    endpoint: "/api/solicitudes",
    adminOnly: true,
    filters: [
      {
        name: "estado",
        label: "Estado",
        type: "select",
        placeholder: "Todos los estados",
        defaultValue: "PENDIENTE",
        options: [
          { value: "PENDIENTE", label: "Pendientes" },
          { value: "APROBADA", label: "Aprobadas" },
          { value: "RECHAZADA", label: "Rechazadas" },
        ],
      },
    ],
    load: (f) => api(f.estado ? `/api/solicitudes/estado/${f.estado}` : "/api/solicitudes"),
    columns: [
      idCol,
      { label: "Solicitante", value: (r) => `<strong>${esc(r.nombre)} ${esc(r.apellido)}</strong><div class="cell-sub">${esc(r.email)}</div>` },
      { label: "Usuario", value: (r) => `<span class="mono">${esc(r.username)}</span>` },
      { label: "Rol", value: (r) => enumBadge(r.rolSolicitado) },
      { label: "Fecha", value: (r) => fmtDate(r.fechaSolicitud) },
      { label: "Estado", value: (r) => enumBadge(r.estado) },
    ],
    search: (r) => [r.nombre, r.apellido, r.username, r.email, r.documento],
    rowActions: (r) => {
      const actions = [{ action: "view", icon: "eye", label: "Ver solicitud" }];
      if (r.estado === "PENDIENTE") {
        actions.push({ action: "approve", icon: "check", label: "Aprobar", tone: "success" });
        actions.push({ action: "reject", icon: "x", label: "Rechazar", tone: "danger" });
      }
      return actions;
    },
  },

  auditoria: {
    title: "Auditoría",
    subtitle: "Historial de cambios sobre bodegas, productos, movimientos y usuarios.",
    singular: "registro",
    endpoint: "/api/auditorias",
    adminOnly: true,
    filters: [
      {
        name: "entidad",
        label: "Entidad",
        type: "select",
        placeholder: "Todas las entidades",
        options: ["Bodega", "Producto", "Movimiento", "Usuario"].map((e) => ({ value: e, label: e })),
      },
      { name: "desde", label: "Desde", type: "date" },
      { name: "hasta", label: "Hasta", type: "date" },
    ],
    load: async (f) => {
      let list;
      if (f.desde || f.hasta) {
        const desde = `${f.desde || "2000-01-01"}T00:00:00`;
        const hasta = `${f.hasta || "2100-12-31"}T23:59:59`;
        list = await api(`/api/auditorias/rango?desde=${encodeURIComponent(desde)}&hasta=${encodeURIComponent(hasta)}`);
        if (f.entidad) list = list.filter((a) => a.entidadAfectada === f.entidad);
      } else {
        list = await api(f.entidad ? `/api/auditorias/entidad/${encodeURIComponent(f.entidad)}` : "/api/auditorias");
      }
      return list;
    },
    columns: [
      idCol,
      { label: "Operación", value: (r) => enumBadge(r.operacion) },
      { label: "Entidad", value: (r) => `<strong>${esc(r.entidadAfectada)}</strong> <span class="mono muted-text">#${esc(r.registroId)}</span>` },
      { label: "Usuario", value: (r) => personName(r.usuario) },
      { label: "Fecha", value: (r) => fmtDate(r.fechaHora) },
      { label: "IP", value: (r) => `<span class="mono">${esc(r.ipOrigen) || "—"}</span>` },
    ],
    search: (r) => [r.entidadAfectada, r.operacion, r.usuario?.nombreCompleto, r.usuario?.username, r.ipOrigen],
    rowActions: () => [{ action: "view", icon: "eye", label: "Ver cambios" }],
  },
};
