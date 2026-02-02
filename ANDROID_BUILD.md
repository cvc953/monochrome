# Compilar la App Android

## Build Rápido

```bash
npm run android:build
```

Esto ejecuta:

1. `npm run build` - Compila el proyecto web
2. `npx cap sync android` - Sincroniza archivos con Android

## Build Manual (Paso a Paso)

```bash
# 1. Compilar proyecto web
npm run build

# 2. Sincronizar con Android
npx cap sync android

# 3. Abrir en Android Studio
npm run android:open
```

## Generar APK/AAB desde Android Studio

1. Abrir proyecto: `npm run android:open`
2. Build → Generate Signed Bundle / APK
3. Seguir el asistente para crear keystore
4. Seleccionar release o debug

📦 El APK estará en `android/app/build/outputs/apk/`
