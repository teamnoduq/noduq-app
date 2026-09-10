# noduq-app

App Kotlin Multiplatform de NODUQ. **Android ahora**; iOS se puede sumar después en este mismo repo (`composeApp` ya es KMP).

Este sprint: identidad + sala de espera vacía. Sin FCM, RevenueCat, Gmail ni SMS.

Dos entradas, una app:

- **Cuenta** — correo y contraseña en Supabase Auth, después el API Spring con ese JWT.
- **Empleado** — sin Supabase. Usuario + código `XXXXX-XXXXX` contra el backend.

## Módulos

```
noduq-app/
  composeApp/          KMP (target Android). UI Compose + cliente HTTP.
    src/commonMain/    Pantallas, tema NODUQ, API, ViewModel
    src/androidMain/   Activity, EncryptedSharedPreferences, OkHttp
  gradle/
```

`applicationId`: `com.noduq.app` · `minSdk` 26.

El emulador Android alcanza el backend del host en `http://10.0.2.2:8080`. Eso está en `local.properties` (`noduq.apiBaseUrl`). Cópialo de `local.properties.example`.

## Abrir en Android Studio

1. Instala JDK 21 (`C:\Program Files\Java\jdk-21`).
2. SDK de Android en `%LOCALAPPDATA%\Android\Sdk` (API 35).
3. **File → Open** y elige `noduq-app` (esta carpeta, no un repo Android aparte).
4. Espera el sync de Gradle. El módulo a ejecutar es **composeApp**.

## Correr en el emulador

1. En Android Studio: **Device Manager** → crea o arranca un AVD (API 26+).
2. Arranca el backend en el host (`noduq-backend` en el puerto 8080).
3. Run **composeApp** en ese AVD. `10.0.2.2` es `localhost` de tu máquina, no hace falta un teléfono.

Si cambias la URL del API, edita `noduq.apiBaseUrl` en `local.properties` y vuelve a sincronizar.

## APK debug

Con `ANDROID_HOME` y JDK 21:

```powershell
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
.\gradlew.bat :composeApp:assembleDebug
```

El APK queda en:

`composeApp/build/outputs/apk/debug/composeApp-debug.apk`

## Qué hay en pantalla

- Puerta: **Empleado** (usuario y código) / **Cuenta** (correo)
- Cuenta: login, registro, organización, Pagos (vacío), Empleados, Cuenta (borrar escribiendo el nombre de la organización)
- Empleado: usuario + código, espera del aviso, salir

El código en claro solo se muestra una vez, al crear o regenerar.
