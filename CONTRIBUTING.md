# 🤝 Contribuir

Gracias por ayudar a mejorar los parches de Wallapop.

## Errores y propuestas

Busca primero en las incidencias existentes y usa la plantilla adecuada. Incluye versiones de Android, Morphe, la app y la fuente; origen y formato del paquete; pasos para reproducir y registros de parcheo. Oculta datos personales en capturas y registros.

## Cambios de código

1. Crea un fork y una rama para tu cambio.
2. Mantén los parches limitados a las versiones de la app verificadas; comprueba las clases, métodos y recursos esperados antes de modificarlos.
3. Sigue [BUILDING.md](BUILDING.md) para compilar y verificar el APK reconstruido.
4. Documenta qué cambia y qué has comprobado. Distingue las comprobaciones estructurales de las pruebas reales en un teléfono.
5. Actualiza `CHANGELOG.md` y, si cambia la compatibilidad o el alcance, `PATCHES.md` y el resumen del README.
6. Abre una pull request con el problema, la solución y el resultado de la validación.

No incluyas APK, splits, claves de firma, tokens, credenciales ni datos de cuentas. Conserva la licencia y las atribuciones del proyecto.

## Publicaciones

Solo una versión con su bundle `.mpp` publicado y sus metadatos actualizados puede ser descargada por Morphe. Los cambios de documentación no necesitan una nueva versión del bundle. El flujo de publicación está descrito en [BUILDING.md](BUILDING.md).
