# Backend – Museo de los Museos

Microservicio REST desarrollado en **Spring Boot** (Java 21, Maven) con base de datos **MySQL**. Es el único microservicio del proyecto y da servicio a la app móvil Android (Kotlin + Jetpack Compose) de **DSY1105 Desarrollo de Aplicaciones Móviles**.

> **Datos ficticios:** por normativa (Leyes N.º 21.719 y N.º 19.628) el sistema solo usa datos simulados. No ingreses RUT, correos ni direcciones reales.

## Integrantes

- Anderson Ovando: backend (microservicio) y pantallas de la app
- Cristobal Pizarro: código base de la app móvil

## Funcionalidades

- Catálogo de **museos** (mapa de museos virtuales) con filtro por categoría.
- Catálogo de **productos** (cursos, libros y materiales) con filtro por categoría.
- Registro, consulta y edición de **usuarios**, con contraseña guardada con hash BCrypt.
- **Login** (`/api/auth/login`).
- **Pedidos** con número automático (`MDM-00001`), estado y filtro por usuario.
- **Notificaciones** por usuario, con marcado como leída.
- Datos iniciales cargados automáticamente al arrancar (12 museos, 10 productos y un administrador).

## Tecnologías

| Componente | Detalle |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.2.0 (Web MVC, Data JPA, Validation) |
| Base de datos | MySQL (XAMPP) |
| Seguridad | `spring-security-crypto` (BCrypt) |
| Pruebas | JUnit 5 y Mockito |
| Build | Maven (`mvnw` incluido) |

## Requisitos

- JDK 21
- MySQL en ejecución (con XAMPP: botón **Start** en MySQL)
- No hace falta instalar Maven: se usa `mvnw`

## Cómo ejecutar

1. Inicia MySQL. La base `museo_db` se crea sola la primera vez.
2. Si tu usuario `root` tiene contraseña, defínela antes de arrancar (PowerShell):
   ```powershell
   $env:DB_PASSWORD="tu_clave"
   ```
   Para usar otro usuario de MySQL: `$env:DB_USER="otro_usuario"`.
3. En la carpeta `backend/`:
   ```powershell
   .\mvnw spring-boot:run
   ```
4. Comprueba en el navegador: http://localhost:8080/api/museos

Desde el **emulador de Android**, el backend se ve como `http://10.0.2.2:8080/`. Desde un celular físico, usa la IP de tu PC en la red local.

### Datos iniciales

Al arrancar se agregan los que falten (no duplica si se reinicia):

- 12 museos (`MU01` a `MU12`)
- 10 productos (`C001`–`C004` cursos, `L001`–`L003` libros, `M001`–`M003` materiales)
- Un usuario administrador de prueba:

| Correo | Contraseña | Rol |
|---|---|---|
| `admin@museo.cl` | `Admin123` | ADMINISTRADOR |

## Endpoints

| Recurso | Métodos |
|---|---|
| `/api/museos` (`?categoria=`) | GET, POST |
| `/api/museos/{id}` | GET, PUT, DELETE |
| `/api/productos` (`?categoria=`) | GET, POST |
| `/api/productos/{id}` | GET, PUT, DELETE |
| `/api/usuarios` | GET, POST |
| `/api/usuarios/{id}` | GET, PUT, DELETE |
| `/api/auth/login` | POST |
| `/api/pedidos` (`?usuarioId=`) | GET, POST |
| `/api/pedidos/{id}` | GET, PUT, DELETE |
| `/api/pedidos/{id}/estado` | PATCH |
| `/api/notificaciones` (`?usuarioId=`) | GET, POST |
| `/api/notificaciones/{id}` | GET, PUT, DELETE |
| `/api/notificaciones/{id}/leida` | PATCH |

### Ejemplos de cuerpo

**Registrar usuario** (`POST /api/usuarios`):
```json
{
  "nombre": "Ana Prueba",
  "rut": "11111111-1",
  "correo": "ana@mail.cl",
  "contrasena": "secreto1",
  "despacho": "Santiago, Chile"
}
```

**Login** (`POST /api/auth/login`):
```json
{ "correo": "ana@mail.cl", "contrasena": "secreto1" }
```

**Crear pedido** (`POST /api/pedidos`):
```json
{
  "usuarioId": 1,
  "clienteNombre": "Ana Prueba",
  "resumen": "1 x Curso de Curaduría Virtual",
  "total": 49.0,
  "destino": "Santiago, Chile",
  "tipoEnvio": "Estándar"
}
```

**Cambiar estado de un pedido** (`PATCH /api/pedidos/{id}/estado`):
```json
{ "estado": "ENVIADO" }
```
Estados: `PENDIENTE`, `CONFIRMADO`, `ENVIADO`, `ENTREGADO`.

**Crear notificación** (`POST /api/notificaciones`):
```json
{
  "usuarioId": 1,
  "tipo": "PEDIDO",
  "titulo": "Pedido confirmado",
  "mensaje": "Tu pedido MDM-00001 fue confirmado."
}
```
Tipos: `PEDIDO`, `CURSO`, `MUSEO`, `AVISO`.

### Reglas de negocio

- La contraseña **nunca se devuelve** en las respuestas y se guarda con BCrypt.
- Correo y RUT de usuario son únicos.
- Todo pedido nace en estado `PENDIENTE` y toda notificación nace sin leer, aunque el cliente envíe otro valor.
- Pedidos y notificaciones solo se crean para un `usuarioId` que exista.
- El login responde el mismo mensaje si falla el correo o la contraseña.

## Errores

Todas las respuestas de error tienen la misma forma:

```json
{ "status": 404, "mensaje": "Usuario 999 no encontrado" }
```

| Código | Cuándo ocurre |
|---|---|
| **400** | Datos inválidos (con `detalles` por campo), JSON mal formado o valor de enum inexistente |
| **401** | Correo o contraseña incorrectos en el login |
| **404** | El recurso no existe (museo, producto, usuario, pedido o notificación) |
| **409** | Dato duplicado (id de museo o producto, correo o RUT de usuario) |

## Pruebas

```powershell
.\mvnw test
```

Las pruebas unitarias usan **Mockito**: simulan el repositorio y el encriptador, así que **no necesitan MySQL**. `UsuarioServiceTest` cubre correo y RUT duplicados, contraseña guardada con hash, usuario inexistente y login correcto o fallido.

Las pruebas de los endpoints se hicieron con Postman (colección en `backend/postman/`).

## Estructura

```
src/main/java/com/museodelosmuseos/museo_backend/
├── MuseoBackendApplication.java
├── config/        SeguridadConfig (BCrypt), DataLoader (datos iniciales)
├── controller/    Museo, Producto, Usuario, Auth, Pedido, Notificacion
├── dto/           UsuarioRequest, UsuarioResponse, LoginRequest, EstadoPedidoRequest
├── exception/     excepciones propias y GlobalExceptionHandler
├── model/         entidades JPA y enums (Rol, EstadoPedido, TipoNotificacion)
├── repository/    interfaces JpaRepository
└── service/       lógica de negocio
```

Flujo: `Controller → Service → Repository → MySQL`. Los controllers solo reciben y responden, las reglas viven en los services y los errores se convierten en JSON en `GlobalExceptionHandler`.
