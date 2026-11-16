/* ============================================================
   app.js · Dashboard: navegación, Productos, Movimientos y Auditoría
   Depende de auth.js (apiFetch, toast, logout, getUser…)
   ============================================================ */

// ---- Guardia: sin sesión válida no se entra al dashboard ----
if (!sessionValid()) logout('Inicia sesión para continuar.');

const { username, rol } = getUser();
const esAdmin = rol === 'ADMIN';          // EMPLEADO solo consulta y registra movimientos
const $ = (sel) => document.querySelector(sel);

/** Escapa texto antes de insertarlo en HTML (evita XSS con datos de la BD). */
const esc = (v) => String(v ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const fecha = (iso) => iso ? new Date(iso).toLocaleString('es-CO') : '—';
const dinero = (n) => '$ ' + Number(n).toLocaleString('es-CO', { minimumFractionDigits: 2 });

/* ---------------- Modal reutilizable ---------------- */
function openModal(titulo, cuerpoHTML, { onSubmit, okText = 'Guardar', okClass = 'btn-success' }) {
  $('#modalRoot').innerHTML = `
    <div class="overlay"><form class="modal" novalidate>
      <h2>${esc(titulo)}</h2>${cuerpoHTML}
      <p class="form-error" id="modalError" hidden></p>
      <div class="modal-actions">
        <button type="button" class="btn btn-light" id="modalCancel">Cancelar</button>
        <button type="submit" class="btn ${okClass}">${esc(okText)}</button>
      </div></form></div>`;
  const form = $('#modalRoot form');
  $('#modalCancel').onclick = closeModal;
  $('.overlay').onmousedown = (e) => { if (e.target.classList.contains('overlay')) closeModal(); };
  form.onsubmit = async (e) => {
    e.preventDefault();
    const btn = form.querySelector('[type=submit]'); btn.disabled = true;
    try { await onSubmit(form); closeModal(); }
    catch (ex) { const p = $('#modalError'); p.textContent = ex.message; p.hidden = false; }   // error del backend visible en el modal
    finally { btn.disabled = false; }
  };
  form.querySelector('input,select')?.focus();
}
const closeModal = () => { $('#modalRoot').innerHTML = ''; };

/* ---------------- Navegación (SPA) ---------------- */
const vistas = { productos: vistaProductos, movimientos: vistaMovimientos, auditoria: vistaAuditoria };

async function navegar(nombre) {
  if (nombre === 'auditoria' && !esAdmin) nombre = 'productos';
  document.querySelectorAll('.sidebar a[data-view]').forEach(a => a.classList.toggle('active', a.dataset.view === nombre));
  $('#sidebar').classList.remove('open');
  $('#content').innerHTML = '<p class="muted">Cargando…</p>';
  try { await vistas[nombre](); }
  catch (ex) { $('#content').innerHTML = `<p class="form-error">${esc(ex.message)}</p>`; }
}

/* ============================================================
   PRODUCTOS
   ============================================================ */
async function vistaProductos() {
  const productos = await apiFetch('/api/productos');
  const filas = productos.map(p => `
    <tr>
      <td>${p.id}</td><td>${esc(p.nombre)}</td><td>${esc(p.categoria)}</td>
      <td class="num">${dinero(p.precio)}</td><td class="num">${p.stockMinimo}</td>
      ${esAdmin ? `<td class="actions">
        <button class="btn btn-warn btn-sm" data-edit="${p.id}"><i class="bi bi-pencil"></i> Actualizar</button>
        <button class="btn btn-danger btn-sm" data-del="${p.id}"><i class="bi bi-trash"></i> Eliminar</button></td>` : ''}
    </tr>`).join('');

  $('#content').innerHTML = `
    <div class="page-head"><h1>Productos</h1>
      ${esAdmin ? '<button class="btn btn-success" id="newProd"><i class="bi bi-plus-lg"></i> Nuevo producto</button>' : ''}</div>
    <div class="table-wrap"><table>
      <thead><tr><th>ID</th><th>Nombre</th><th>Categoría</th><th class="num">Precio</th><th class="num">Stock mínimo</th>${esAdmin ? '<th>Acciones</th>' : ''}</tr></thead>
      <tbody>${filas || `<tr><td colspan="6" class="empty">Aún no hay productos. Crea el primero con “Nuevo producto”.</td></tr>`}</tbody>
    </table></div>`;

  if (!esAdmin) return;
  $('#newProd').onclick = () => formProducto();
  document.querySelectorAll('[data-edit]').forEach(b => b.onclick = () => formProducto(productos.find(p => p.id == b.dataset.edit)));
  document.querySelectorAll('[data-del]').forEach(b => b.onclick = () => borrarProducto(productos.find(p => p.id == b.dataset.del)));
}

/** Modal crear (POST /api/productos) o editar (PUT /api/productos/{id}). */
function formProducto(p) {
  openModal(p ? 'Actualizar producto' : 'Nuevo producto', `
    <label>Nombre</label><input name="nombre" value="${esc(p?.nombre)}" required minlength="2" maxlength="150">
    <label>Categoría</label><input name="categoria" value="${esc(p?.categoria)}" required minlength="2" maxlength="100">
    <div class="form-grid">
      <div><label>Precio</label><input name="precio" type="number" step="0.01" min="0.01" value="${p?.precio ?? ''}" required></div>
      <div><label>Stock mínimo</label><input name="stockMinimo" type="number" min="0" value="${p?.stockMinimo ?? 10}" required></div>
    </div>`, {
    onSubmit: async (f) => {
      const body = { nombre: f.nombre.value.trim(), categoria: f.categoria.value.trim(), precio: Number(f.precio.value), stockMinimo: Number(f.stockMinimo.value) };
      await apiFetch(p ? `/api/productos/${p.id}` : '/api/productos', { method: p ? 'PUT' : 'POST', body });
      toast(p ? 'Producto actualizado' : 'Producto creado', 'success');
      vistaProductos();
    }
  });
}

function borrarProducto(p) {
  openModal('Eliminar producto', `<p>¿Eliminar <strong>${esc(p.nombre)}</strong>? Esta acción no se puede deshacer.</p>`, {
    okText: 'Eliminar', okClass: 'btn-danger',
    onSubmit: async () => {
      await apiFetch(`/api/productos/${p.id}`, { method: 'DELETE' });   // si tiene stock/movimientos, el backend responde con su mensaje y se muestra en el modal
      toast('Producto eliminado', 'success');
      vistaProductos();
    }
  });
}

/* ============================================================
   MOVIMIENTOS
   ============================================================ */
async function vistaMovimientos() {
  const [productos, bodegas, movs] = await Promise.all([
    apiFetch('/api/productos'), apiFetch('/api/bodegas'), apiFetch('/api/movimientos')
  ]);
  const optBod = bodegas.filter(b => b.activo !== false).map(b => `<option value="${b.id}">${esc(b.nombre)}</option>`).join('');
  const optProd = productos.map(p => `<option value="${p.id}">${esc(p.nombre)}</option>`).join('');
  const filas = [...movs].reverse().map(m => `
    <tr><td>${m.id}</td><td>${fecha(m.fecha)}</td><td><span class="badge b-${esc(m.tipo)}">${esc(m.tipo)}</span></td>
      <td>${esc(m.usuario?.username)}</td><td>${esc(m.bodegaOrigen?.nombre) || '—'}</td><td>${esc(m.bodegaDestino?.nombre) || '—'}</td><td>${esc(m.observacion)}</td></tr>`).join('');

  $('#content').innerHTML = `
    <div class="page-head"><h1>Movimientos</h1></div>
    <form class="card" id="movForm" novalidate>
      <div class="form-grid">
        <div><label>Tipo de movimiento</label>
          <select name="tipo"><option>ENTRADA</option><option>SALIDA</option><option>TRANSFERENCIA</option></select></div>
        <div id="boxOrigen"><label>Bodega de origen</label><select name="origen"><option value="">Selecciona…</option>${optBod}</select></div>
        <div id="boxDestino"><label>Bodega de destino</label><select name="destino"><option value="">Selecciona…</option>${optBod}</select></div>
        <div><label>Producto</label><select name="producto"><option value="">Selecciona…</option>${optProd}</select></div>
        <div><label>Cantidad</label><input name="cantidad" type="number" min="1" value="1"></div>
      </div>
      <label>Observación (opcional)</label><input name="obs" maxlength="255">
      <p class="form-error" id="movError" hidden></p>
      <button class="btn btn-success btn-block" type="submit" style="width:auto"><i class="bi bi-check2-circle"></i> Registrar movimiento</button>
    </form>
    <h2 style="color:var(--navy);font-size:1.15rem">Historial</h2>
    <div class="table-wrap"><table>
      <thead><tr><th>ID</th><th>Fecha</th><th>Tipo</th><th>Usuario</th><th>Origen</th><th>Destino</th><th>Observación</th></tr></thead>
      <tbody>${filas || '<tr><td colspan="7" class="empty">Todavía no hay movimientos registrados.</td></tr>'}</tbody></table></div>`;

  const f = $('#movForm');
  // ENTRADA solo usa destino, SALIDA solo origen, TRANSFERENCIA ambos
  const ajustar = () => {
    $('#boxOrigen').hidden = f.tipo.value === 'ENTRADA';
    $('#boxDestino').hidden = f.tipo.value === 'SALIDA';
  };
  f.tipo.onchange = ajustar; ajustar();

  f.onsubmit = async (e) => {
    e.preventDefault();
    const err = $('#movError'); err.hidden = true;
    const t = f.tipo.value;
    const body = {
      tipo: t,
      bodegaOrigenId: t !== 'ENTRADA' && f.origen.value ? Number(f.origen.value) : null,
      bodegaDestinoId: t !== 'SALIDA' && f.destino.value ? Number(f.destino.value) : null,
      productoId: f.producto.value ? Number(f.producto.value) : null,
      cantidad: Number(f.cantidad.value),
      observacion: f.obs.value.trim() || null
    };
    try {
      await apiFetch('/api/movimientos', { method: 'POST', body });   // el usuario responsable lo toma el backend del token
      toast(`${t} registrada correctamente`, 'success');
      vistaMovimientos();
    } catch (ex) { err.textContent = ex.message; err.hidden = false; }
  };
}

/* ============================================================
   AUDITORÍA (solo lectura, solo ADMIN)
   ============================================================ */
/** Los valores llegan como texto JSON: se muestran indentados; si no son JSON válido, tal cual. */
function jsonBonito(txt) {
  if (!txt) return '';
  try { return JSON.stringify(JSON.parse(txt), null, 2); } catch { return txt; }
}

async function vistaAuditoria() {
  const lista = await apiFetch('/api/auditorias');
  const filas = lista.map(a => {
    const antes = jsonBonito(a.valoresAnteriores), despues = jsonBonito(a.valoresNuevos);
    return `<tr>
      <td>${fecha(a.fechaHora)}</td>
      <td><span class="badge b-${esc(a.operacion)}">${esc(a.operacion)}</span></td>
      <td>${esc(a.usuario?.username) || '—'}</td>
      <td>${esc(a.entidadAfectada)} #${a.registroId ?? '—'}</td>
      <td>${antes ? `<pre class="json"><code>Antes:\n${esc(antes)}</code></pre>` : ''}
          ${despues ? `<pre class="json" style="margin-top:.4rem"><code>Después:\n${esc(despues)}</code></pre>` : ''}</td>
    </tr>`;
  }).join('');

  $('#content').innerHTML = `
    <div class="page-head"><h1>Auditoría</h1></div>
    <div class="table-wrap"><table>
      <thead><tr><th>Fecha</th><th>Operación</th><th>Usuario</th><th>Entidad afectada</th><th>Cambios</th></tr></thead>
      <tbody>${filas || '<tr><td colspan="5" class="empty">No hay registros de auditoría.</td></tr>'}</tbody></table></div>`;
}

/* ---------------- Arranque ---------------- */
$('#userInfo').textContent = `${username} (${rol})`;
if (!esAdmin) $('#navAudit').hidden = true;          // el backend también lo bloquea (403)
document.querySelectorAll('.sidebar a[data-view]').forEach(a => a.onclick = (e) => { e.preventDefault(); navegar(a.dataset.view); });
$('#logoutTop').onclick = $('#logoutSide').onclick = (e) => { e.preventDefault(); logout(); };
$('#menuBtn').onclick = () => $('#sidebar').classList.toggle('open');
navegar('productos');
