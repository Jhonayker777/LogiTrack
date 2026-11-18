# LogiTrack · Frontend modular

Frontend SPA en HTML, CSS y JavaScript ES Modules, sin framework. Consume la API REST de Spring Boot en `http://localhost:8080`.

## Arquitectura

```text
LogiTrack-Frontend-Pro/
├── index.html                 # Acceso
├── app.html                   # Shell del dashboard
├── styles.css                 # Design system y estilos
├── auth.js                    # Login, registro y recuperación
├── app.js                     # Composición de vistas y navegación
├── js/
│   ├── core/
│   │   ├── dom.js             # Selectores, escape HTML y formato
│   │   └── session.js         # Estado de sesión y cierre
│   ├── services/
│   │   ├── http.js            # Cliente HTTP, JWT y errores
│   │   └── index.js           # Servicios por recurso/endpoints
│   ├── ui/
│   │   └── notifications.js   # Toasts reutilizables
│   └── features/
│       └── profile.view.js   # Vista de perfil personal
└── backend-patch/             # Cambios requeridos para perfil propio
    └── ...
```

## Ejecutar

1. Inicia Spring Boot en `http://localhost:8080`.
2. Aplica los cambios de `backend-patch` (son necesarios para `/api/usuarios/me`).
3. Sirve esta carpeta desde Live Server u otro servidor estático HTTP. No abras `app.html` como `file://`, porque los módulos ES requieren servidor.
4. Abre `index.html`.

## Perfil personal

El módulo **Mi perfil** consulta `GET /api/usuarios/me` y actualiza `PUT /api/usuarios/me`. El usuario puede modificar nombre, apellido, documento, correo, teléfono, dirección y ciudad. No puede modificar su propio rol, estado ni username. La autorización real se valida en Spring Security usando el usuario del JWT; nunca se confía en un ID enviado por el navegador.

## Seguridad

El cliente añade `Authorization: Bearer <token>` a las llamadas protegidas. Ante HTTP 401 limpia la sesión y redirige al login. Los permisos del frontend solo mejoran la experiencia; la autorización definitiva debe permanecer en el backend.

## Consideraciones

- API base configurable en `js/services/http.js`.
- `js/services/index.js` centraliza los recursos y sus rutas.
- `app.js` conserva el renderizado de módulos existentes y orquesta la navegación.
- No se implementa cambio de contraseña porque el backend actual no expone un endpoint seguro para esa operación.
