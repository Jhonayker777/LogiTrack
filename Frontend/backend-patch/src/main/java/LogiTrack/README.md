# Parche backend: perfil propio LogiTrack

Copiar los archivos de `src/main/java/LogiTrack/API/` sobre la misma ruta del backend Spring Boot. Se agregan los endpoints:

- `GET /api/usuarios/me` — devuelve el perfil asociado al `username` del JWT.
- `PUT /api/usuarios/me` — actualiza nombre, apellido, documento, email, teléfono, dirección y ciudad.

Ambos endpoints admiten ADMIN y EMPLEADO autenticados. Las rutas administrativas `/api/usuarios/**` continúan restringidas a ADMIN, excepto las rutas `/me` declaradas antes en SecurityConfig.

La solicitud de perfil no acepta username, password, rol, estado, turno ni fecha de ingreso; estos campos no pueden alterarse mediante el endpoint propio. No se requiere migración de base de datos.
