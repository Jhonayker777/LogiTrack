# Arquitectura de LogiTrack

## Capas

```text
Vistas HTML
  └── app.js (router liviano / composición)
       ├── features/       casos de uso y vistas por módulo
       ├── ui/              notificaciones y elementos reutilizables
       ├── core/            sesión, DOM y formato
       └── services/        comunicación REST con Spring Boot
              ├── http.js   token, headers, errores, 401
              └── index.js  endpoints agrupados por recurso

Spring Boot
  ├── Controller          endpoints REST
  ├── Service              reglas de negocio
  ├── Repository           persistencia
  └── Security/JWT         autenticación y autorización
```

## Reglas de dependencia

1. Las vistas no construyen URLs ni usan `fetch` directamente; llaman a `apiCall`.
2. `services/http.js` es el único módulo que conoce la URL base y los detalles HTTP.
3. `core/session.js` administra los datos de sesión del cliente, pero no decide permisos reales.
4. `app.js` registra navegación y compone vistas. Cada nueva funcionalidad debe vivir en `features/<nombre>/`.
5. El backend es la autoridad para validar roles, identidad y permisos. Ocultar un botón en el frontend no es una medida de seguridad.

## Perfil propio: decisión de seguridad

No se reutiliza `PUT /api/usuarios/{id}` porque el controlador de administración está protegido para ADMIN y su DTO acepta campos privilegiados (rol y username). En su lugar, el parche agrega `GET /api/usuarios/me` y `PUT /api/usuarios/me`.

El backend obtiene el username desde `Authentication.getName()`, derivado del JWT validado. El navegador no envía el ID del usuario. El DTO específico de perfil no contiene username, contraseña, rol, turno, fecha de ingreso ni estado. Así un empleado solo puede modificar sus datos personales, no los de otra persona ni sus privilegios.
