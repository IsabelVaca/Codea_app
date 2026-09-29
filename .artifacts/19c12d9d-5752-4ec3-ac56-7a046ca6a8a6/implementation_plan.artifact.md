# Plan de Corrección de Errores

Se han identificado dos tipos de errores principales: un error de configuración de build debido a versiones de dependencias y errores de sintaxis en el archivo `infanteScreenAdmin.kt`.

## Análisis del Error

1.  **Error de Build (SDK Version):** El proyecto está configurado para compilar con `android-36.1`, pero las dependencias actuales (como `core-ktx:1.19.0`) requieren al menos el SDK 37.
2.  **Error en `infanteScreenAdmin.kt`:**
    *   `Scaffold`: Falta el parámetro obligatorio `content`. En Material 3, `Scaffold` requiere una lambda para el contenido.
    *   `TopAppBar`: Es una API experimental en Material 3 y requiere la anotación `@OptIn(ExperimentalMaterial3Api::class)`.

## Cambios Propuestos

### Configuración del Proyecto

#### [MODIFY] [app/build.gradle.kts](file:///Users/isabelvaca/AndroidStudioProjects/Codea_app/app/build.gradle.kts)
*   Actualizar `compileSdk` a 37.
*   Actualizar `targetSdk` a 37 para mantener consistencia.

### Interfaz de Usuario

#### [MODIFY] [infanteScreenAdmin.kt](file:///Users/isabelvaca/AndroidStudioProjects/Codea_app/app/src/main/java/mx/tec/codea/ui/screens/infanteScreenAdmin.kt)
*   Añadir la anotación `@OptIn(ExperimentalMaterial3Api::class)` a la función `InfanteScreen`.
*   Importar `androidx.compose.material3.ExperimentalMaterial3Api`.
*   Añadir el bloque `content` a `Scaffold` recibiendo `paddingValues`.
*   Añadir importaciones necesarias para `Modifier` y `padding` si se decide usar un contenedor en el contenido.

## Plan de Verificación

### Pruebas Automatizadas
*   Ejecutar `./gradlew app:assembleDebug` para asegurar que el build sea exitoso.

### Verificación Manual
*   Confirmar que el archivo `infanteScreenAdmin.kt` ya no muestra errores en el editor.
