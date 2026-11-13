# mobile_ariz

App móvil nativa (Android) de **Ariz Proyectos**. Usa el mismo backend NestJS y el mismo diseño
(colores, logo, tipografía) que el sitio web.

**Stack:** Kotlin · Jetpack Compose · MVVM · Hilt · Retrofit/OkHttp · Room · Navigation Compose.

## Empezar

1. Abrir la carpeta en Android Studio (JDK 17+) y sincronizar Gradle.
2. Levantar el backend en `http://localhost:4000` (el emulador lo ve como `10.0.2.2:4000`).
3. Ejecutar la configuración `app`, o por terminal:

```bash
./gradlew assembleDebug   # compilar
./gradlew test            # tests unitarios
```

## APK de prueba para otros dispositivos

El emulador ve el backend como `10.0.2.2`, pero un celular real necesita la IP de tu PC en la misma red Wi-Fi:

```bash
./gradlew :app:assembleRelease -ParizApiBaseUrl=http://<IP-de-tu-PC>:4000/ -ParizSignWithDebug=true
# APK: app/build/outputs/apk/release/app-release.apk (~4 MB)
```

- La URL debe terminar en `/`. Con `http://` la app permite tráfico sin HTTPS; con `https://` no.
- `-ParizSignWithDebug=true` lo firma con la llave de debug para poder instalarlo. **No sirve para publicar.**
- Con el backend encendido y el firewall de Windows permitiendo el puerto 4000 en red privada.
- Para el APK final, definir la URL pública con `-ParizApiBaseUrl=https://...` y firmar con la llave de release.

## Qué incluye

Autenticación completa, Inicio, Buscar con filtros, detalle de propiedad (aviso de precio y consulta),
proyectos (directorio y detalle), favoritos, perfil del agente, Ariz Club (puntos y canjes), inversiones con
calculadora, cuenta, contacto y páginas informativas. Todo con el diseño y los textos de la web.

## Documentación

La estructura de módulos, las reglas de dependencia, las convenciones y los pendientes
(URL de release, dominio web, Google Sign-In, firma) están en [ARCHITECTURE.md](ARCHITECTURE.md).
