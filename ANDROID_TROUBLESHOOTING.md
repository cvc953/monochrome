# Solución de Problemas Android

## Error: Unable to launch Android Studio

Si ves el error "Unable to launch Android Studio. Is it installed?":

```bash
# 1. Encuentra Android Studio
which studio || find /opt /usr/share /snap /mnt -name "studio.sh" 2>/dev/null | head -1

# 2. Configura la variable (ajusta la ruta según tu instalación)
export CAPACITOR_ANDROID_STUDIO_PATH="/ruta/a/android-studio/bin/studio"

# 3. Hazlo permanente agregando a ~/.bashrc o ~/.zshrc
echo 'export CAPACITOR_ANDROID_STUDIO_PATH="/ruta/a/studio"' >> ~/.bashrc
```

**Alternativa**: Abre manualmente desde Android Studio:

- Abre Android Studio
- File → Open
- Selecciona la carpeta `android/` del proyecto

## Error: ANDROID_HOME no configurado

```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

Agregar al `~/.bashrc` o `~/.zshrc` para hacerlo permanente.

## Error: Java version incorrecta

Capacitor requiere Java JDK 17:

```bash
sudo apt install openjdk-17-jdk  # Ubuntu/Debian
# o descargar de: https://adoptium.net/
```

## Error: No devices/emulators found

```bash
# Verificar dispositivos conectados
adb devices

# Reiniciar ADB si es necesario
adb kill-server
adb start-server
```

## La app no se actualiza

```bash
# Limpiar y reconstruir
rm -rf dist android/app/build
npm run android:build
```

## Error de permisos en Android

Verificar en `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## Logs de depuración

```bash
# Ver logs de la app en tiempo real
adb logcat | grep Capacitor
```
