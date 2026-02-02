# Configuración Inicial para Android

## Requisitos Previos

1. **Node.js** (versión 16 o superior)
2. **Java JDK** (versión 17 recomendada)
3. **Android Studio** con Android SDK

## Instalar Android Studio

1. Descargar de: https://developer.android.com/studio
2. Instalar Android SDK (API 33 o superior recomendado)
3. Configurar variables de entorno:

```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

## Verificar Instalación

```bash
java --version
android --version  # desde Android Studio SDK tools
```

¡Listo para construir! 🚀
