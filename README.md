# noduq-app

App Kotlin Multiplatform de NODUQ. **Android ahora**; iOS se puede sumar después en este mismo repo (`composeApp` ya es KMP).

Dos entradas, una app:

- **Cuenta** — correo y contraseña (o Google) en Supabase Auth, después el API Spring con ese JWT.
- **Empleado** — sin Supabase. Usuario + código `XXXXX-XXXXX` contra el backend.

El dueño no entra al mostrador sin plan. Si cierra la app antes de pagar, al volver cae otra vez en la pantalla de plan. Después pide SMS y notificaciones.

El cobro de prueba usa RevenueCat **Test Store**. RevenueCat no es pasarela: en producción el dinero lo cobra Play (Android) o, en web, RevenueCat Billing / Stripe. iOS (`noduq_email`) más adelante.

## Módulos

```
noduq-app/
  composeApp/          KMP (target Android). UI Compose + cliente HTTP.
    src/commonMain/    Pantallas, tema NODUQ, API, ViewModel
    src/androidMain/   Activity, SMS, FCM, RevenueCat, EncryptedSharedPreferences
  gradle/
```

`applicationId`: `com.noduq.app` · `minSdk` 26.

El emulador Android alcanza el backend del host en `http://10.0.2.2:8080`. En dispositivo o contra prod usa `noduq.apiBaseUrl` en `local.properties`. Cópialo de `local.properties.example`. Ahí también va `noduq.revenueCatApiKey` (clave Test Store).

## Abrir en Android Studio

1. Instala JDK 21 (`C:\Program Files\Java\jdk-21`).
2. SDK de Android en `%LOCALAPPDATA%\Android\Sdk` (API 35).
3. **File → Open** y elige `noduq-app`.
4. El módulo a ejecutar es **composeApp**.

## APK debug

Con `ANDROID_HOME` y JDK 21:

```powershell
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
.\gradlew.bat :composeApp:assembleDebug
```

El APK queda en `composeApp/build/outputs/apk/debug/composeApp-debug.apk`.

## Qué hay en pantalla

- Puerta: **Empleado** / **Cuenta**
- Cuenta: login, registro, olvido de clave, comercio, **plan**, permisos, Pagos, Empleados (lookback Hoy/3/7), Cuenta (Gmail, borrar)
- Empleado: usuario + código, avisos del recorte que le puso el dueño
