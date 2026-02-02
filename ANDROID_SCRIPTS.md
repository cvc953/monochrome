# Scripts NPM Disponibles

## Scripts Android

| Comando | Descripción |
|---------|-------------|
| `npm run android:build` | Compila web + sincroniza con Android |
| `npm run android:open` | Abre el proyecto en Android Studio |
| `npm run android:run` | Compila y ejecuta en dispositivo/emulador |

## Scripts Originales

| Comando | Descripción |
|---------|-------------|
| `npm run dev` | Servidor de desarrollo (web) |
| `npm run build` | Compila proyecto web |
| `npm run preview` | Preview del build |
| `npm run lint` | Ejecuta todos los linters |

## Flujo de Trabajo Típico

```bash
# 1. Desarrollo web (prueba rápida)
npm run dev

# 2. Cuando esté listo, compilar para Android
npm run android:build

# 3. Abrir en Android Studio
npm run android:open

# 4. Ejecutar en dispositivo desde Android Studio
# O directamente con:
npm run android:run
```

💡 **Tip**: Durante desarrollo, usa `npm run dev` para cambios rápidos en web, y sincroniza a Android solo cuando necesites probar funcionalidad nativa.
