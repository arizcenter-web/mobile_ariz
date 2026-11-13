# Arquitectura de app_ariz (Android nativo)

App móvil de Ariz. Consume el **mismo backend NestJS** que `Ariz_Web_frontend` (sin prefijo `/api`;
ese prefijo es solo del proxy de Next.js) y usa **el mismo diseño y paleta** (`tokens.css`).

**Stack:** Kotlin · Jetpack Compose + Material3 · MVVM (UiState + StateFlow) · Hilt · Retrofit/OkHttp +
kotlinx.serialization · Room · DataStore · Navigation Compose (rutas tipadas) · Coil · Paging 3.

## 1. Mapa de módulos

```
app/                  Ensambla: Application, MainActivity, NavHost, barra inferior
core/
  common/             AppResult, AppError, dispatchers, utilidades Kotlin puras
  model/              Modelos de dominio compartidos (Kotlin puro, sin Android)
  network/            Retrofit, OkHttp, SessionCookieJar, safeApiCall, DTOs/interceptores compartidos
  session/            SessionManager: usuario autenticado, restaurar (/auth/me), cerrar sesión, expiración
  catalog/            Catálogo público compartido (propiedades y proyectos con filtros): Inicio, Buscar, listings, projects
  favorites/          Favoritos del usuario: ids en vivo, guardar/quitar, FavoritesViewModel y rememberFavorites()
  leads/              Formulario "Contacta al anunciante" (hoja inferior) de propiedades y proyectos
  database/           Room: @Database, DAOs y entidades compartidas (caché offline)
  datastore/          Preferencias no sensibles
  designsystem/       Tema (colores, tipografía, formas) y componentes base Ariz
  ui/                 Componentes de UI compartidos entre features (estados, imágenes, listas)
feature/<nombre>/     Una feature = un módulo (ver §2)
build-logic/          Convention plugins (toda la config de Gradle compartida)
```

### Features y su módulo del backend

| Feature      | Módulos del backend (`backend/src/modules`) |
|--------------|---------------------------------------------|
| `auth`       | `auth`                                      |
| `home`       | `properties`, `projects` (vía `core:catalog`) |
| `search`     | `properties` (vía `core:catalog`)           |
| `listings`   | `properties`, `alerts` (detalle y aviso de precio) |
| `projects`   | `projects` (directorio y detalle)           |
| `favorites`  | `favorites` (lista; el estado vive en `core:favorites`) |
| `agents`     | `agents` (perfil público del anunciante)    |
| `account`    | sesión (`core:session`): perfil y accesos   |
| `rewards`    | `referrals`, `redemptions` (Ariz Club)      |
| `investments`| `investment-opportunities`                  |
| `info`       | `contact-reports` + páginas estáticas del web (nosotros, servicios, construye, ayuda, términos) |

No se implementó (paneles de gestión pensados para escritorio, fuera del alcance de la app de clientes):
`admin/*`, `owner/*`, `claims` (libro de reclamaciones), `campaigns`, `media`. Los contactos recibidos por
un profesional (`GET /leads/mine`) tampoco están: la app cubre a clientes e invitados.

### Qué va en `core` y qué en una feature

Una capacidad que usan **varias features** (favoritos, formulario de consulta, catálogo público) vive en
`core:*` para que las features no se dependan entre sí. La feature que la necesita recibe lo que ofrece
`core` y, para navegar a otra pantalla, pide el destino por un callback que `:app` resuelve.

## 2. Estructura interna de una feature

```
feature/<nombre>/src/main/kotlin/com/tecsup/app_ariz/feature/<nombre>/
├─ data/
│  ├─ remote/api/        Interfaz Retrofit (@GET/@POST...)
│  ├─ remote/dto/        DTOs @Serializable (espejo del JSON del backend)
│  ├─ local/dao/         DAOs Room de la feature (si hay caché)
│  ├─ local/entity/      Entidades Room
│  ├─ mapper/            DTO/Entity -> modelo de dominio
│  └─ repository/        Implementación de los repositorios de domain/
├─ domain/
│  ├─ model/             Modelos propios de la feature
│  ├─ repository/        Interfaces de repositorio
│  └─ usecase/           Un caso de uso por acción (operator fun invoke)
├─ ui/
│  ├─ component/         Composables propios de la feature
│  └─ <pantalla>/        XxxContract.kt (UiState + Action + Event) · XxxViewModel.kt · XxxScreen.kt
│                        (una carpeta por pantalla). Referencia: feature/auth/ui/login
├─ navigation/           XxxRoute (@Serializable) + NavGraphBuilder.xxxScreen()
└─ di/                   Módulo Hilt: @Binds repositorio, @Provides api
```

Tests en `src/test/kotlin/.../feature/<nombre>/{data,domain,ui}`.

## 3. Reglas de dependencia (no negociables)

```
app ──> feature:* ──> core:*
```

- `feature` **nunca** depende de otra `feature`. Si dos features necesitan lo mismo, sube a `core`.
- `core` **nunca** depende de `feature` ni de `app`.
- `domain/` es Kotlin puro: sin Android, sin Retrofit, sin Room, sin DTOs.
- Los DTOs y entidades **no salen de `data/`**: se mapean a modelos de dominio.
- La UI solo habla con el ViewModel; el ViewModel solo con casos de uso; estos con interfaces de repositorio.
- `app` solo conoce de cada feature su función pública `xxxScreen()` y su `XxxRoute`.
- Navegar entre features: desde `app` con rutas (`navController.navigate(XxxRoute)`), o recibiendo
  lambdas (`onOpenProject: (id) -> Unit`) en el grafo de la feature. Nunca importando su UI.

Flujo: `Screen → ViewModel → UseCase → Repository(interfaz) → RepositoryImpl → Api / Dao`.

## 4. Convenciones

- **Estado de pantalla:** un `XxxUiState` inmutable expuesto como `StateFlow`; eventos de una sola vez
  (navegar, snackbar) por `Channel`. Cada pantalla se divide en `XxxScreen` (con ViewModel: recoge el
  estado y los eventos) y `XxxContent(state, onAction)` sin ViewModel, que es la UI real y lleva `@Preview`.
  `XxxRoute` queda reservado para el destino `@Serializable` de navegación.
- **Validación de formularios:** reglas puras en `domain/validation` que devuelven un enum `FieldError`;
  la UI lo traduce a texto con `strings.xml`. El ViewModel valida antes de llamar al caso de uso.
- **Sesión compartida:** vive en `core:session` (`SessionManager`). Cualquier feature puede leer
  `sessionManager.state` (`Restoring | SignedOut | SignedIn(user)`) para saber quién está autenticado;
  solo `feature:auth` llama a `onSignedIn` (desde sus casos de uso) y solo `feature:account` cierra sesión.
  Para proteger una pantalla: si `state` es `SignedOut`, navegar a `AuthRoute` (la feature recibe una
  lambda, no importa `feature:auth`).
- **Errores:** los repositorios devuelven `AppResult<T>`; nunca lanzan. Usa `safeApiCall { api.xxx() }`.
  `AppError` ya mapea 401, 403, 404, 400/422, 429 (throttler) y 503 (BD caída del backend).
- **Sesión:** el backend usa cookie httpOnly `ariz_session`. `SessionCookieJar` (cifrada) la guarda y
  reenvía sola. Un 401 en ruta protegida emite `SessionEvents.expired` y la app abre `AuthRoute`.
  Las rutas `/auth/*` no disparan ese evento (un 401 en login es "credenciales inválidas").
- **Diseño:** nunca hardcodear colores ni radios. Usa `MaterialTheme.colorScheme` / `.shapes` /
  `.typography` y `ArizTheme.extraColors` (éxito, aviso, info, WhatsApp). Los valores vienen de
  `Ariz_Web_frontend/src/shared/styles/tokens.css`; si cambian allá, se actualizan en
  `core/designsystem/.../theme/Color.kt`.
- **Nombres:** `XxxDto`, `XxxEntity`, `XxxRepository` (interfaz) / `XxxRepositoryImpl`, `GetXxxUseCase`,
  `XxxViewModel`, `XxxUiState`, `XxxScreen`, `XxxRoute`.
- **Idioma:** código y nombres en inglés; textos visibles al usuario en español (en `strings.xml` de
  cada módulo cuando sean más de un par).
- **Contratos del backend:** no hay Swagger. Los DTOs se escriben a mano leyendo el controller y los
  `dto/` del módulo en `backend/src/modules/<modulo>`. `Json` ya ignora claves desconocidas.

## 5. Cómo agregar una feature nueva

1. Crear `feature/<nombre>/` copiando la estructura de §2 y un `build.gradle.kts`:
   ```kotlin
   plugins { id("ariz.android.feature") }
   android { namespace = "com.tecsup.app_ariz.feature.<nombre>" }
   ```
2. Registrar `include(":feature:<nombre>")` en `settings.gradle.kts`.
3. Agregar `implementation(project(":feature:<nombre>"))` en `app/build.gradle.kts`.
4. Llamar su `xxxScreen()` dentro del `NavHost` de `app/.../navigation/ArizApp.kt`.

## 6. Configuración y entornos

| Qué | Dónde |
|---|---|
| Versiones de librerías | `gradle/libs.versions.toml` (única fuente) |
| Config común de módulos | `build-logic/convention` (plugins `ariz.android.*`) |
| URL del backend | `core/network/build.gradle.kts` → `buildConfigField("API_BASE_URL")` |
| Debug | `http://10.0.2.2:4000/` (localhost del PC desde el emulador); HTTP permitido solo en debug |
| Release | **Pendiente:** reemplazar el placeholder `https://api.example.com/` por la URL real |
| ProGuard (release, minify activo) | `app/proguard-rules.pro` |
| Estilo de código | `.editorconfig` |

Plugins disponibles: `ariz.android.application`, `ariz.android.library`, `ariz.jvm.library`,
`ariz.android.compose`, `ariz.android.hilt`, `ariz.android.room`, `ariz.android.feature`
(= library + compose + hilt + serialization + dependencias a core, navegación, Retrofit y Coil).

Compilar: `./gradlew assembleDebug` · Tests unitarios: `./gradlew test`.

## 7. Pendiente de definir por el equipo

- URL pública del backend para release.
- **Dominio web** (`arizWebHost` en `gradle.properties`, hoy `ariz.example.com`): lo usan el enlace de
  "restablecer contraseña" y el `intent-filter` del manifest. Para que el enlace abra la app directo (App
  Links verificados), el sitio web debe publicar `/.well-known/assetlinks.json` con el SHA-256 de la firma.
  Sin eso el enlace abre el navegador (la página web de reset sigue funcionando).
- **Login con Google:** el código está listo, pero el botón solo aparece si existe `app/google-services.json`
  (Firebase Console → app Android `com.tecsup.app_ariz` con el SHA-1 de debug/release, y proveedor Google
  activado). El proyecto Firebase debe ser el mismo del backend (`FIREBASE_PROJECT_ID`).
- Mapas (lotes/planos): el web usa Google Maps; decidir `maps-compose` y la API key de Android.
- Firma de release (keystore) y CI.
