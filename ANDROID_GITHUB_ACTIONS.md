# Compilar Android con GitHub Actions

## Uso del Workflow

El workflow se ejecuta automáticamente cuando:

- Haces `push` a las ramas main/master/develop
- Creas un Pull Request
- Lo ejecutas manualmente

### Ejecutar Manualmente

1. Ve a tu repositorio en GitHub
2. Click en la pestaña **Actions**
3. Selecciona **Build Android App**
4. Click en **Run workflow** → **Run workflow**

## Descargar el APK

1. Ve a **Actions** en GitHub
2. Click en el workflow ejecutado
3. Baja hasta **Artifacts**
4. Descarga:
    - `monochrome-debug.apk` - APK de debug (listo para instalar)
    - `monochrome-release-unsigned.apk` - APK de release sin firmar

## Firmar APK para Producción

El APK de release necesita ser firmado. Puedes configurar GitHub Secrets:

1. Genera un keystore:

```bash
keytool -genkey -v -keystore monochrome.keystore -alias monochrome -keyalg RSA -keysize 2048 -validity 10000
```

2. Convierte a base64:

```bash
base64 monochrome.keystore > keystore.b64
```

3. Agrega estos secrets en GitHub (Settings → Secrets → Actions):
    - `KEYSTORE_FILE` - Contenido del archivo keystore.b64
    - `KEYSTORE_PASSWORD` - Contraseña del keystore
    - `KEY_ALIAS` - Alias de la clave
    - `KEY_PASSWORD` - Contraseña de la clave

4. El workflow automáticamente firmará el APK en releases

## Ventajas del Workflow

✅ No necesitas Android Studio instalado localmente
✅ Compilación limpia en cada build
✅ APK disponible para descargar directamente
✅ Funciona igual para todo el equipo
✅ Historial de builds en GitHub

## Tips

- El APK de **debug** se puede instalar directamente en cualquier dispositivo
- El APK de **release** es más pequeño y optimizado, pero necesita firma
- Los artifacts se guardan por 30 días
