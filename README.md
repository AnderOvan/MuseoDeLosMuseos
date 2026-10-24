# Museo de los Museos

Aplicación móvil Android (MVP académico) para el ramo **DSY1105 Desarrollo de Aplicaciones Móviles** (Duoc UC). Centraliza el catálogo de cursos y productos museológicos, el Mapa de Museos Virtuales de Iberoamérica y el cálculo simulado de envíos.

> **Datos ficticios:** por normativa (Leyes N.º 21.719 y N.º 19.628) la app solo usa datos simulados o sintéticos. Nunca ingreses RUT, correos ni direcciones reales.

## Tecnologías

| Componente | Detalle |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| Interfaz | Jetpack Compose + Material 3 |
| Navegación | Navigation Compose |
| Persistencia | Room (SQLite) con KSP |
| Arquitectura | MVVM (pantalla → ViewModel → repositorio → Room) |
| Build | Android Gradle Plugin 9.4.1, minSdk 24 |

## Cómo ejecutar

1. Clona el repositorio y ábrelo con Android Studio.
2. Espera a que termine el **Gradle Sync**.
3. Elige un emulador o un celular con depuración USB y presiona **Run** (▶).

## Estructura del proyecto

Todo el código está en `app/src/main/java/com/example/museodelosmuseos/`.

```
museodelosmuseos/
├── MainActivity.kt
├── modelo/
│   ├── Rol.kt
│   └── Usuario.kt
├── repository/
│   ├── LocalRepository.kt
│   └── local/
│       ├── AppDatabase.kt
│       ├── Cifrado.kt
│       ├── Daos.kt
│       ├── EntidadesLocales.kt
│       └── SessionManager.kt
├── ui/
│   ├── navigation/
│   │   └── Rutas.kt
│   ├── screens/
│   │   ├── BienvenidaScreen.kt
│   │   ├── LoginScreen.kt
│   │   └── RegistroScreen.kt
│   └── theme/
│       ├── MuseoColores.kt
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── util/
│   └── Validaciones.kt
└── viewmodel/
    └── AuthViewModel.kt
```

### Qué contiene cada carpeta

| Carpeta / archivo | Responsabilidad |
|---|---|
| `MainActivity.kt` | Punto de entrada. Crea el repositorio y el ViewModel y define el `NavHost` con todas las rutas |
| `modelo/` | Clases que usan las pantallas: `Rol` (enum de roles) y `Usuario` (datos del formulario) |
| `repository/LocalRepository.kt` | Punto único de acceso a los datos. Aplica las reglas: correo y RUT no repetidos, login, favoritos, historial QR |
| `repository/local/` | Todo lo relacionado con Room y el almacenamiento local |
| `repository/local/EntidadesLocales.kt` | Tablas: `usuarios`, `favoritos`, `lecturas_qr` y `productos_cache` |
| `repository/local/Daos.kt` | Consultas a cada tabla (insertar, buscar, autenticar, observar cambios) |
| `repository/local/AppDatabase.kt` | Base de datos Room, una sola instancia para toda la app |
| `repository/local/Cifrado.kt` | Convierte la contraseña en un hash SHA-256 antes de guardarla |
| `repository/local/SessionManager.kt` | Recuerda qué usuario tiene la sesión iniciada |
| `ui/navigation/Rutas.kt` | Nombres de las rutas de las 15 pantallas |
| `ui/screens/` | Una pantalla Compose por archivo |
| `ui/theme/MuseoColores.kt` | Paleta del museo (azul oscuro, dorado, crema, blanco) |
| `util/Validaciones.kt` | Validación de nombre, correo, contraseña y RUT (módulo 11), y formato del RUT |
| `viewmodel/AuthViewModel.kt` | Une las pantallas de login y registro con el repositorio |

## Flujo de datos

```
Pantalla (ui/screens)
   │  evento: "registrar" / "iniciar sesión"
   ▼
ViewModel (viewmodel)
   │  corrutina
   ▼
LocalRepository (repository)
   │  reglas de negocio
   ▼
DAO + Room (repository/local)  →  SQLite (museo_db)
```

Las pantallas nunca tocan Room directamente: solo muestran datos y avisan eventos. Los mensajes de error de la base de datos vuelven a la pantalla a través del ViewModel.

## Reglas de organización

- **Una sola carpeta de modelos:** `modelo/`. No crear `model/`.
- **Todo lo de Room** (entidades, DAOs, base de datos) vive en `repository/local/`.
- **Las pantallas** van en `ui/screens/`, con un archivo por pantalla y el sufijo `Screen`.
- **Colores:** se usan siempre desde `ui/theme/MuseoColores.kt`, no se escriben códigos de color sueltos.
- **Rutas:** se usan siempre desde `Rutas`, no textos escritos a mano.
- **Nombres en español** para clases, variables y mensajes, salvo los términos técnicos (`ViewModel`, `Repository`, `DAO`).
- **Validaciones** en `util/Validaciones.kt`, no dentro de las pantallas, para poder reutilizarlas.

## Roles de la aplicación

| Rol | Descripción |
|---|---|
| Administrador | Gestiona productos, cursos, solicitudes y notificaciones (perfil de prueba) |
| Investigador | Registra y revisa museos virtuales, ubicaciones y enlaces (perfil de prueba) |
| Cliente | Explora museos y catálogo, guarda favoritos, escanea QR y realiza solicitudes |

## Distribución de pantallas

| Integrante | Pantallas |
|---|---|
| Persona A (Anderson) | 1 Bienvenida, 2 Registro, 3 Login, 4 Dashboard, 13 Historial de solicitudes, 14 Notificaciones, 15 Perfil |
| Persona B | 5 Mapa, 6 Detalle del museo, 7 Escáner QR, 8 Resultado QR, 9 Catálogo, 10 Detalle de producto, 11 Favoritos, 12 Carrito y envío |

## Trabajo con Git

- Cada integrante trabaja en su propia rama y no escribe directamente en `main`.
- **Antes de empezar a trabajar:** `Git → Pull`.
- Commits pequeños, con mensajes que describan el cambio (por ejemplo: `Agrega campo RUT con validación al registro`).
- Si se mueven o renombran archivos, avisar al otro integrante antes de subir los cambios, para evitar conflictos.
- No subir `build/`, `.gradle/` ni `local.properties`.
