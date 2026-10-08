# 🩹 Parches

> **v0.1.0** · 2 parches en 1 aplicación · [Volver al README](README.md)

## Wallapop (`com.wallapop`)

**Versión compatible:** `1.334.0` · código `10141414`  
**Entorno previsto:** Morphe `1.34.0` · patcher `1.15.1`  
**Android mínimo de Wallapop:** Android 12L (API 32).

| Parche | Detalles |
| --- | --- |
| Wallapop OLED dark mode | Usa el modo oscuro de Android. Sustituye las superficies primaria, secundaria y terciaria de la paleta Compose por `#000000`. Cambia el tema clásico a DayNight y añade recursos semánticos de noche para fondos y primeros planos. |
| Wallapop No Ads | Fuerza a falso `ShouldShowAdsCommand.invoke()` y `AdsFeatureFlagsDataSourceImpl.getShouldShowAds()`, dos decisiones centrales de publicidad. |

OLED incluye una dependencia interna de recursos que Morphe aplica junto al parche visible. El modo claro conserva los valores originales de los colores sustituidos por referencias temáticas. El alcance de publicidad no incluye productos promocionados ni servicios de pago.

## Compatibilidad y validación

| Comprobación | Estado |
| --- | --- |
| Compilación Kotlin contra patcher 1.15.1 | Verificada |
| Bundle `.mpp` con clases JVM y DEX Android | Verificado |
| Filtro de compatibilidad y aplicación con Morphe Desktop 1.18.1 | Verificados |
| Reconstrucción completa y firma del APK base suministrado | Verificadas |
| Cambios en DEX, recursos reconstruidos y firma v2 | Verificados independientemente |
| Carga en Morphe Android 1.34.0 | Pendiente de prueba en teléfono |
| Arranque, apariencia y alcance de No Ads en teléfono | Pendientes |

Los [registros de verificación](verification/) documentan estas comprobaciones.

### Paquete dividido

El APK utilizado declara `requiredSplitTypes="base__abi,base__density"` y `com.android.vending.splits.required=true`, y carece de bibliotecas nativas. El APK reconstruido conserva estos requisitos. Necesitas el paquete completo de esa versión, con sus splits, en un formato que Morphe pueda procesar.

### Límites conocidos

Wallapop usa PairIP: compilar y firmar no confirma que una instalación modificada arranque. Las pantallas con colores asignados directamente por código o WebViews pueden requerir ajustes de OLED. La desaparición efectiva de la publicidad y de sus huecos necesita pruebas de uso.
