# LogiTrack

**Sistema web de gestión de inventarios, bodegas, movimientos y auditoría.**

LogiTrack está compuesto por una API REST desarrollada con Spring Boot y un frontend web construido con HTML, CSS y JavaScript moderno (ES Modules). La aplicación permite administrar el catálogo de productos, consultar existencias por bodega, registrar movimientos de inventario, gestionar usuarios y solicitudes de acceso, y consultar la trazabilidad de las operaciones.

Este README documenta el proyecto completo: arquitectura, módulos, tecnologías, configuración, ejecución, autenticación, endpoints, permisos y consideraciones de seguridad.

> **Alcance de esta entrega:** la carpeta contiene el frontend modular y `backend-patch/`, que incluye los cambios de backend preparados para la funcionalidad **Mi perfil**. El backend Spring Boot original se conserva como proyecto independiente; no se debe asumir que está incluido dentro de este directorio del frontend.

---

## 1. Funcionalidades principales

- Inicio de sesión mediante usuario y contraseña.
- Autenticación basada en JWT (JSON Web Token).
- Solicitud de registro de nuevos usuarios y recuperación de contraseña mediante los endpoints existentes del backend.
- Panel principal con indicadores de inventario, bodegas y movimientos recientes.
- Administración de productos y categorías.
- Consulta y administración de stock por bodega y producto.
- Registro y consulta de movimientos de inventario.
- Consulta y administración del detalle de los movimientos.
- Administración de bodegas y sus responsables.
- Gestión administrativa de usuarios.
- Revisión, aprobación y rechazo de solicitudes de acceso.
- Consulta de auditoría por entidad, registro, usuario y rango de fechas.
- Administración de departamentos y ciudades.
- Perfil personal: cada usuario autenticado puede consultar y actualizar sus propios datos personales.
- Interfaz adaptable a escritorio y dispositivos móviles, con tema oscuro.

## 2. Tecnologías utilizadas

### Frontend

- HTML5.
- CSS3.
- JavaScript ES Modules, sin framework obligatorio.
- Fetch API para consumir el backend.
- Lucide para iconografía.
- Google Fonts: DM Sans y Space Grotesk.

### Backend

Según el `pom.xml` del backend original:

- Java 21.
- Spring Boot 4.1.2.
- Spring Web MVC.
- Spring Data JPA / Hibernate.
- Spring Security.
- JWT con JJWT 0.11.5.
- Bean Validation.
- MySQL Connector/J.
- Spring Mail.
- Springdoc OpenAPI / Swagger UI.
- Maven Wrapper.
- Lombok.

### Base de datos

- MySQL.
- El backend está configurado para utilizar un esquema existente. En la configuración revisada, `spring.jpa.hibernate.ddl-auto=none`, por lo que Hibernate no crea automáticamente las tablas.

---

## 3. Estructura general del proyecto

La organización recomendada cuando se mantienen ambos proyectos en un mismo repositorio es:

```text
LogiTrack/
├── backend/                         # Proyecto Spring Boot
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── src/
│       └── main/
│           ├── java/LogiTrack/API/
│           │   ├── auth/            # Login y autenticación
│           │   ├── config/          # Seguridad, Jackson y OpenAPI
│           │   ├── controller/      # Controladores REST
│           │   ├── dto/             # Objetos de solicitud y respuesta
│           │   ├── exception/       # Excepciones y respuestas de error
│           │   ├── mapper/          # Conversión entre entidades y DTO
│           │   ├── Model/           # Entidades JPA
│           │   ├── repository/      # Acceso a datos
│           │   ├── security/        # Filtro y servicios JWT
│           │   ├── service/         # Interfaces y reglas de negocio
│           │   └── audit/           # Registro automático de auditoría
│           └── resources/
│               └── application.properties
│
└── frontend/                        # Aplicación web
    ├── index.html                   # Inicio de sesión
    ├── app.html                     # Estructura principal del dashboard
    ├── styles.css                   # Estilos globales y componentes
    ├── auth.js                      # Login, registro y recuperación
    ├── app.js                       # Navegación y composición de vistas
    ├── package.json                 # Configuración de módulos ES
    ├── ARCHITECTURE.md              # Descripción de la arquitectura frontend
    ├── js/
    │   ├── core/
    │   │   ├── dom.js               # Selectores, escape HTML y formatos
    │   │   └── session.js           # Estado local de sesión
    │   ├── services/
    │   │   ├── http.js               # Cliente HTTP y JWT
    │   │   └── index.js              # Endpoints agrupados por recurso
    │   ├── ui/
    │   │   └── notifications.js     # Notificaciones tipo toast
    │   └── features/
    │       └── profile.view.js      # Vista de perfil personal
    └── backend-patch/                # Archivos que se integran al backend
```

En el ZIP de esta entrega, la carpeta raíz corresponde al **frontend**. La carpeta `backend-patch/` no es un backend completo: contiene solamente los archivos que se deben integrar al backend Spring Boot original.

---

## 4. Arquitectura y responsabilidades

### 4.1 Backend: arquitectura por capas

El backend sigue una separación por responsabilidades:

| Capa | Responsabilidad |
|---|---|
| `Model` | Representa las entidades persistidas en la base de datos. |
| `controller` | Expone los endpoints HTTP y recibe las solicitudes. |
| `dto/request` | Define y valida los datos que recibe la API. |
| `dto/response` | Define la información que devuelve la API. |
| `service` | Contiene las interfaces y reglas de negocio. |
| `service/implement` | Implementa los casos de uso y coordina la lógica. |
| `repository` | Realiza las operaciones de persistencia mediante Spring Data JPA. |
| `mapper` | Convierte entidades a DTO y DTO a entidades. |
| `security` | Valida los tokens JWT y establece la identidad autenticada. |
| `config` | Configura Spring Security, CORS, Jackson y OpenAPI. |
| `audit` | Registra eventos de auditoría asociados a operaciones. |
| `exception` | Centraliza el manejo y formato de errores. |

Flujo habitual de una petición:

```text
Cliente web
   │
   ▼
Controller REST
   │ valida y recibe DTO
   ▼
Service / ServiceImpl
   │ aplica reglas de negocio
   ▼
Repository
   │
   ▼
Base de datos MySQL
```

### 4.2 Frontend: arquitectura modular por responsabilidades

- **`core/`**: utilidades comunes del navegador, sesión, selectores DOM, escape de HTML y formatos.
- **`services/`**: comunicación con la API. Las rutas se agrupan por recurso y las solicitudes HTTP se centralizan.
- **`features/`**: funcionalidades específicas de la aplicación. Actualmente el perfil personal está extraído en su propio módulo.
- **`ui/`**: componentes y utilidades visuales reutilizables, como las notificaciones.
- **`app.js`**: coordina navegación, carga de vistas y composición del dashboard. Los renderizadores de varios módulos existentes todavía permanecen en este archivo; la separación completa de cada módulo es una posible siguiente fase de refactorización.
- **`auth.js`**: controla el flujo de autenticación de la pantalla de acceso.

La URL base de la API se configura en `js/services/http.js`:

```js
const API_BASE_URL = 'http://localhost:8080';
```

Si el backend se ejecuta en otro host o puerto, se debe cambiar ese valor.

---

## 5. Módulos funcionales

| Módulo | Descripción | Acciones principales |
|---|---|---|
| Resumen | Vista general de la operación. | Indicadores, existencias, alertas de stock bajo y movimientos recientes. |
| Productos | Catálogo de artículos. | Crear, consultar, editar, eliminar, filtrar por categoría y consultar stock bajo. |
| Stock por bodega | Existencias por ubicación. | Consultar, crear, editar, eliminar y filtrar por bodega o producto. |
| Movimientos | Operaciones de inventario. | Registrar entradas, salidas o transferencias según las reglas del backend y consultar movimientos. |
| Detalle de movimientos | Líneas asociadas a cada movimiento. | Consultar, crear, editar, eliminar y filtrar por movimiento o producto. |
| Bodegas | Ubicaciones de almacenamiento. | Crear, consultar, editar, eliminar y filtrar por ciudad o encargado. |
| Usuarios | Administración de cuentas. | Listar, consultar, crear, editar y eliminar usuarios, sujeto a permisos del backend. |
| Solicitudes | Solicitudes de acceso. | Crear, consultar, filtrar, aprobar y rechazar solicitudes. |
| Auditoría | Trazabilidad de cambios. | Consultar eventos, entidad, registro, usuario y rango de fechas. |
| Departamentos | División territorial. | Crear, consultar, editar y eliminar. |
| Ciudades | Ciudades asociadas a departamentos. | Crear, consultar, editar, eliminar y filtrar por departamento. |
| Mi perfil | Datos de la cuenta autenticada. | Consultar y actualizar los datos personales propios. |

---

## 6. Requisitos previos

Antes de ejecutar el proyecto, instala o verifica lo siguiente:

1. Java Development Kit (JDK) 21.
2. MySQL Server.
3. Una base de datos LogiTrack creada con el esquema y las tablas que utiliza el backend.
4. Maven Wrapper incluido en el backend (`mvnw` para Linux/macOS o `mvnw.cmd` para Windows). No es necesario instalar Maven globalmente si el wrapper puede descargar la versión requerida.
5. Visual Studio Code u otro editor para el frontend.
6. Una extensión de servidor estático, por ejemplo Live Server, o un servidor HTTP equivalente.

---

## 7. Configuración del backend Spring Boot

### 7.1 Configurar la base de datos

Abre `backend/src/main/resources/application.properties` y configura los datos de conexión de tu entorno. La configuración revisada utiliza MySQL en `localhost`, puerto `3307`, y el esquema `LogiTrack_JKQ`; estos valores pueden cambiar según tu instalación.

Ejemplo de configuración sin credenciales reales:

```properties
spring.application.name=API

spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3307/LogiTrack_JKQ}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.open-in-view=true

jwt.secret=${JWT_SECRET}
jwt.expiration-ms=${JWT_EXPIRATION_MS:1800000}

spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

Configura las variables de entorno `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `MAIL_USERNAME` y `MAIL_PASSWORD` según corresponda. La clave JWT debe ser robusta y cumplir el tamaño mínimo requerido por la implementación JWT del proyecto.

**Importante sobre credenciales:** el `application.properties` del backend original que se revisó contiene credenciales de base de datos y correo escritas directamente en el archivo. No publiques ese archivo ni lo subas a repositorios públicos. Cambia/rota las credenciales que hayan quedado expuestas y utiliza variables de entorno o un gestor de secretos. No copies credenciales reales a este README.

### 7.2 Verificar el esquema de base de datos

El backend tiene `spring.jpa.hibernate.ddl-auto=none`. Esto significa que espera que las tablas y relaciones ya existan. Antes de iniciar la API:

- Verifica que el servicio MySQL esté activo.
- Verifica el nombre del esquema, puerto, usuario y contraseña.
- Confirma que las tablas y relaciones coincidan con las entidades JPA del backend.
- Conserva un respaldo de la base de datos antes de aplicar cambios estructurales.

Este README no incluye un script de creación de base de datos porque no se proporcionó un script SQL completo dentro de los archivos revisados.

### 7.3 Aplicar el parche de perfil personal

Para habilitar **Mi perfil**, copia los archivos de `frontend/backend-patch/src/main/java/LogiTrack/API/` a las rutas equivalentes del backend original, conservando la estructura de paquetes.

El parche contiene:

- `config/SecurityConfig.java`
- `controller/UsuarioController.java`
- `dto/request/PerfilUsuarioDtoRequest.java` (archivo nuevo)
- `service/UsuarioService.java`
- `service/implement/UsuarioServiceImpl.java`

Antes de reemplazar archivos, crea una copia de seguridad o realiza un commit en Git. Si tu backend ha cambiado desde que se preparó el parche, integra los cambios manualmente y conserva los métodos y reglas de negocio que ya tengas.

El repositorio `UsuarioRepository` debe disponer de un método compatible con:

```java
Optional<Usuario> findByUsername(String username);
```

Si no existe, agrégalo a la interfaz del repositorio, verificando el nombre exacto del atributo de usuario en la entidad.

### 7.4 Ejecutar el backend

Desde la carpeta del backend:

**Windows (PowerShell o CMD):**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux/macOS:**

```bash
./mvnw spring-boot:run
```

Si el wrapper no tiene permisos de ejecución en Linux/macOS:

```bash
bash mvnw spring-boot:run
```

La API está configurada para iniciar en el puerto predeterminado de Spring Boot, `8080`, salvo que se cambie la configuración.

### 7.5 Documentación de la API

Con el backend iniciado, abre Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Y la especificación OpenAPI:

```text
http://localhost:8080/v3/api-docs
```

La disponibilidad de estas rutas depende de que el backend haya iniciado correctamente y de la configuración de Springdoc.

---

## 8. Configuración y ejecución del frontend

El frontend no requiere un proceso de compilación ni instalación de dependencias npm para la versión actual.

1. Abre la carpeta del frontend en Visual Studio Code.
2. Comprueba que `API_BASE_URL` en `js/services/http.js` apunte al backend.
3. Inicia el backend en `http://localhost:8080`.
4. Inicia un servidor estático desde la carpeta del frontend. Con Live Server, utiliza preferiblemente `http://127.0.0.1:5500` o `http://localhost:5500`, orígenes contemplados en la configuración CORS revisada.
5. Abre `index.html` desde ese servidor.
6. Inicia sesión con una cuenta válida registrada en la base de datos.

No abras los archivos directamente mediante `file://`: `app.html` utiliza módulos JavaScript y necesita servirse por HTTP/HTTPS.

Si utilizas otro puerto o dominio para el frontend, agrégalo explícitamente a `allowedOrigins` en `SecurityConfig.java` del backend. En producción, configura únicamente los dominios reales autorizados.

---

## 9. Autenticación y sesión

### Inicio de sesión

El frontend envía las credenciales a:

```http
POST /auth/login
Content-Type: application/json
```

Ejemplo de cuerpo:

```json
{
  "username": "usuario",
  "password": "contraseña"
}
```

El frontend espera que la respuesta incluya un token JWT y, según el contrato actual, puede incluir `username`, `rol`, `tipo` y `expiraEnSegundos`.

### Uso del token

Las llamadas protegidas incluyen el encabezado:

```http
Authorization: Bearer <token>
```

El token se guarda en `localStorage` bajo `logitrack_token`. Los datos básicos de sesión se guardan bajo `logitrack_user`.

Cuando el backend devuelve HTTP `401`, el cliente elimina los datos de sesión y redirige al usuario al inicio de sesión. Un error `403` indica que el usuario está autenticado, pero no tiene permisos para la operación solicitada.

### Registro y recuperación

El formulario de acceso utiliza estos endpoints públicos del backend:

- `POST /auth/register`: registra una solicitud de acceso, pendiente del proceso de aprobación definido por el backend.
- `POST /auth/recuperar-password`: solicita la recuperación de contraseña mediante el correo asociado, según la implementación de correo del backend.

---

## 10. Endpoints REST principales

Todos los endpoints de negocio usan el prefijo `/api`, salvo los de autenticación.

| Recurso | Endpoint | Operaciones disponibles en el backend |
|---|---|---|
| Productos | `/api/productos` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `GET /categoria`, `GET /stock-bajo`. |
| Bodegas | `/api/bodegas` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `GET /ciudad/{id}`, `GET /encargado/{id}`. |
| Stock | `/api/stock` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `GET /bodega/{id}`, `GET /producto/{id}`, `GET /bodega/{bodegaId}/producto/{productoId}`, `GET /producto/{id}/total`. |
| Movimientos | `/api/movimientos` | `GET`, `GET /{id}`, `POST`. |
| Detalles | `/api/detalles-movimiento` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `GET /movimiento/{id}`, `GET /producto/{id}`. |
| Usuarios | `/api/usuarios` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`. Además, con el parche, `GET /me` y `PUT /me`. |
| Solicitudes | `/api/solicitudes` | `GET`, `GET /{id}`, `POST`, `GET /estado/{estado}`, `PUT /{id}/aprobar`, `PUT /{id}/rechazar`. |
| Auditoría | `/api/auditorias` | `GET`, `GET /{id}`, `GET /entidad/{entidad}`, `GET /entidad/{entidad}/registro/{id}`, `GET /usuario/{id}`, `GET /rango`. |
| Departamentos | `/api/departamentos` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`. |
| Ciudades | `/api/ciudades` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `GET /departamento/{id}`. |

Los endpoints anteriores corresponden a las rutas encontradas en los controladores del backend original. Para revisar los nombres exactos de parámetros, DTO, tipos de respuesta y validaciones, consulta Swagger UI y los controladores correspondientes.

### Endpoints de perfil propio agregados por el parche

| Método | Ruta | Función |
|---|---|---|
| `GET` | `/api/usuarios/me` | Consulta el perfil del usuario autenticado. |
| `PUT` | `/api/usuarios/me` | Actualiza los datos personales del usuario autenticado. |

El `username` se obtiene desde `Authentication.getName()` después de validar el JWT. El cliente no envía un ID para elegir qué usuario actualizar.

---

## 11. Roles y permisos

La configuración de seguridad revisada establece las siguientes reglas generales:

| Funcionalidad | ADMIN | EMPLEADO |
|---|:---:|:---:|
| Iniciar sesión y solicitar registro | Sí | Sí |
| Consultar endpoints `GET /api/**` | Sí | Sí |
| Registrar movimientos (`POST /api/movimientos`) | Sí | Sí |
| Consultar y actualizar su propio perfil (`/api/usuarios/me`) | Sí, con el parche | Sí, con el parche |
| Administrar usuarios | Sí | No |
| Administrar solicitudes | Sí | No |
| Consultar auditoría | Sí | No |
| Otras operaciones de escritura de `/api/**` | Sí | No, salvo excepciones expresas |

La interfaz oculta algunas opciones administrativas para EMPLEADO, pero eso no sustituye la autorización del backend. Spring Security debe validar cada operación.

---

## 12. Funcionalidad Mi perfil

La pantalla **Mi perfil** permite que cada persona autenticada consulte y actualice sus datos personales:

- Nombre.
- Apellido.
- Documento.
- Correo electrónico.
- Teléfono.
- Dirección.
- Ciudad.

El perfil muestra además información de cuenta como username, rol, estado, turno y fecha de ingreso, pero esos campos no se editan desde esta pantalla.

El DTO `PerfilUsuarioDtoRequest` no incluye username, contraseña, rol, estado, turno ni fecha de ingreso. De esta manera, el endpoint de perfil no debe utilizarse para modificar privilegios o datos administrativos. El cambio no requiere una migración de base de datos si los campos ya existen en la entidad y en el esquema actual.

**Cambio de contraseña:** no está implementado en el módulo Mi perfil. La recuperación de contraseña existente es un flujo separado del backend.

---

## 13. Manejo de errores y validaciones

- El backend centraliza excepciones mediante `GlobalExceptionHandler`.
- Los DTO utilizan Bean Validation cuando corresponde.
- El frontend presenta notificaciones para errores de API y operaciones completadas.
- HTTP `401`: sesión no válida o expirada; el frontend vuelve al inicio de sesión.
- HTTP `403`: usuario autenticado sin autorización suficiente.
- Otros códigos HTTP: el frontend intenta mostrar el mensaje devuelto por la API.

Los mensajes y códigos definitivos dependen de la implementación de cada controlador y del manejador global de excepciones.

---

## 14. Pruebas y verificación

Lista sugerida antes de considerar el sistema listo para uso:

### Backend

- [ ] La aplicación compila con Java 21 y Maven Wrapper.
- [ ] La conexión a MySQL funciona con las credenciales del entorno.
- [ ] Swagger UI carga correctamente.
- [ ] El login devuelve un JWT válido.
- [ ] Las rutas protegidas rechazan solicitudes sin token.
- [ ] ADMIN puede acceder a los módulos administrativos.
- [ ] EMPLEADO no puede ejecutar operaciones administrativas restringidas.
- [ ] `GET /api/usuarios/me` devuelve el usuario correspondiente al JWT.
- [ ] `PUT /api/usuarios/me` actualiza solo los datos personales.
- [ ] Un usuario no puede cambiar el perfil de otro usuario mediante el endpoint `/me`.

### Frontend

- [ ] `index.html` se sirve desde HTTP y permite iniciar sesión.
- [ ] El dashboard carga los datos desde la API.
- [ ] Los módulos muestran los datos y operaciones permitidas.
- [ ] La navegación y el cierre de sesión funcionan.
- [ ] La vista Mi perfil consulta y guarda los datos correctamente.
- [ ] Los errores de conexión y permisos se muestran de forma comprensible.
- [ ] La interfaz se adapta a los tamaños de pantalla requeridos.

### Estado de validación de esta entrega

- Se verificó la sintaxis de los archivos JavaScript con Node.js.
- Se verificó la carga del módulo de servicios y sus recursos exportados.
- Se verificó la integridad del ZIP generado.
- La compilación Java y la integración real contra MySQL no quedaron verificadas en el entorno de preparación; deben ejecutarse en el equipo donde esté disponible el backend y sus dependencias.

---

## 15. Seguridad y recomendaciones para producción

1. No almacenes contraseñas, claves JWT ni credenciales SMTP directamente en el repositorio.
2. Rota las credenciales que hayan sido expuestas en archivos compartidos o subidos.
3. Usa HTTPS en producción.
4. Limita CORS a los dominios reales del frontend.
5. Revisa la duración y renovación de los JWT y la política de cierre de sesión.
6. Valida autorización y propiedad de los recursos siempre en el backend.
7. No confíes en los roles guardados en `localStorage` para proteger endpoints.
8. Considera una política de Content Security Policy y controles adicionales contra XSS.
9. Revisa la exposición de información sensible en los DTO de respuesta y en los registros.
10. Realiza respaldos de la base de datos y pruebas antes de desplegar nuevas versiones.
11. Desactiva o restringe Swagger UI en producción si la política de seguridad del despliegue lo requiere.
12. Revisa la configuración de `spring.jpa.show-sql` y evita registrar datos sensibles.

---

## 16. Mantenimiento y próximas mejoras

- Extraer los renderizadores de productos, bodegas, stock, movimientos, detalles, usuarios, solicitudes, auditoría, departamentos y ciudades de `app.js` a módulos independientes dentro de `js/features/`.
- Incorporar pruebas unitarias y de integración para servicios y controladores Spring Boot.
- Incorporar pruebas de extremo a extremo para los flujos críticos.
- Centralizar la configuración por ambientes para desarrollo, pruebas y producción.
- Añadir paginación y filtros del lado del servidor si el volumen de datos crece.
- Definir un flujo seguro e independiente para cambio de contraseña.
- Documentar el esquema de base de datos y versionar migraciones.

---

## 17. Soporte para desarrollo

Cuando se reporte un problema, recopila:

- Mensaje de error visible en la interfaz.
- Código HTTP y respuesta de la API (sin compartir tokens ni contraseñas).
- Registro de consola del navegador.
- Registro de la aplicación Spring Boot.
- Módulo y operación que produjo el error.

**Nunca compartas públicamente tokens JWT, contraseñas, claves secretas, archivos `.env` ni credenciales de correo o base de datos.**

---

**LogiTrack — Gestión inteligente de inventarios, bodegas y auditoría.**
