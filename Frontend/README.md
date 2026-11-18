# LogiTrack — Frontend

Frontend en HTML, CSS y JavaScript puro (sin dependencias ni compilación) para la API Spring Boot de LogiTrack.

## Cómo ejecutarlo

1. Inicia el backend (por defecto en `http://localhost:8080`).
2. Si tu backend usa otro puerto, edita `js/config.js`.
3. Sirve esta carpeta con un servidor estático en un puerto permitido por el CORS del backend (5500, 3000 u 8081):
   - **VS Code Live Server**: clic derecho en `index.html` → *Open with Live Server* (puerto 5500).
   - **Python**: `python -m http.server 5500`
   - **Node**: `npx serve -l 3000`
4. Abre `http://localhost:5500` (o el puerto que hayas usado).

> No abras `index.html` con doble clic (`file://`): el CORS del backend lo bloqueará.

## Funcionalidades

| Módulo | ADMIN | EMPLEADO |
|---|---|---|
| Login / solicitud de registro | ✔ | ✔ |
| Panel con indicadores, stock bajo y ocupación de bodegas | ✔ | ✔ |
| Movimientos (entrada, salida, transferencia) + detalle | Consultar y registrar | Consultar y registrar |
| Stock por bodega / producto (con total por producto) | CRUD | Consultar |
| Productos (con filtro de stock bajo), bodegas, ciudades, departamentos | CRUD | Consultar |
| Usuarios | CRUD | — |
| Solicitudes de registro (aprobar / rechazar con motivo) | ✔ | — |
| Auditoría (por entidad y rango de fechas, valores antes/después) | ✔ | — |

El token JWT se guarda en `localStorage` y la sesión se cierra automáticamente cuando expira o cuando el backend responde 401.

## Estructura

```
index.html        Login y solicitud de registro
app.html          Panel principal (SPA con rutas #/...)
css/styles.css    Estilos
js/config.js      URL del backend
js/api.js         Cliente HTTP + sesión JWT
js/ui.js          Modales, formularios, toasts, iconos
js/resources.js   Definición de cada módulo (columnas, campos, filtros)
js/auth.js        Lógica de index.html
js/app.js         Router, vistas, panel y movimientos
```
