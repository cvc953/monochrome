# Ejecutar la App Android

## Opción 1: Ejecutar en Dispositivo/Emulador

```bash
npm run android:run
```

Esto compila y ejecuta directamente en el dispositivo conectado.

## Opción 2: Usar Android Studio

```bash
npm run android:open
```

Luego presiona el botón ▶️ Run en Android Studio.

## Dispositivo Físico

1. Activar "Opciones de desarrollador" en el dispositivo
2. Activar "Depuración USB"
3. Conectar vía USB
4. Aceptar autorización en el dispositivo
5. Ejecutar `npm run android:run`

## Emulador Android

1. Abrir Android Studio → Device Manager
2. Crear/iniciar un AVD (Android Virtual Device)
3. Ejecutar `npm run android:run`

🎯 La app se instalará y ejecutará automáticamente
