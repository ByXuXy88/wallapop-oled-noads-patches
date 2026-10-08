# 👋🧩 ByXuXy88's Morphe Patches

[![Release](https://img.shields.io/github/v/release/ByXuXy88/wallapop-oled-noads-patches?label=versión)](https://github.com/ByXuXy88/wallapop-oled-noads-patches/releases/latest)
[![Build](https://github.com/ByXuXy88/wallapop-oled-noads-patches/actions/workflows/release.yml/badge.svg)](https://github.com/ByXuXy88/wallapop-oled-noads-patches/actions/workflows/release.yml)
[![License](https://img.shields.io/badge/licencia-GPL--3.0-blue)](LICENSE)

Parches para usar Wallapop con fondos negros OLED en modo oscuro y desactivar sus decisiones centrales de publicidad.

> [!NOTE]
> Proyecto independiente desarrollado con ayuda de IA. La compilación y la reconstrucción del APK están verificadas; el arranque y el aspecto en un teléfono todavía requieren pruebas.

## Contenido

- [Cómo usar los parches](#-cómo-usar-los-parches)
- [Lista de parches](#-lista-de-parches)
- [Problemas y solicitudes](#-problemas-y-solicitudes)
- [Reportar errores](#-reportar-errores)
- [Preguntas frecuentes](#-preguntas-frecuentes)
- [Desarrollo](#-desarrollo)
- [Acerca del proyecto](#-acerca-del-proyecto)

## 📲 Cómo usar los parches

**[➕ Añadir esta fuente a Morphe](https://morphe.software/add-source?github=ByXuXy88/wallapop-oled-noads-patches)**

También puedes añadir manualmente esta URL en **Morphe → Fuentes → + → Remota**:

```text
https://github.com/ByXuXy88/wallapop-oled-noads-patches
```

1. Usa Morphe **1.34.0** y el paquete original completo de Wallapop **1.334.0**.
2. Selecciona **Wallapop OLED dark mode** y **Wallapop No Ads**.
3. Genera la aplicación y activa el **modo oscuro de Android** para ver los fondos OLED.

El archivo `.mpp` también está disponible en [Releases](https://github.com/ByXuXy88/wallapop-oled-noads-patches/releases/latest) para importarlo como fuente local. Las fuentes remotas permiten buscar nuevas versiones de los parches.

> [!IMPORTANT]
> El APK utilizado para las comprobaciones es un APK base dividido y necesita sus splits de arquitectura y densidad. Por sí solo no permite una instalación completa. [Compatibilidad y resultados](PATCHES.md#compatibilidad-y-validación).

## 🩹 Lista de parches

> [v0.1.0](https://github.com/ByXuXy88/wallapop-oled-noads-patches/releases/tag/v0.1.0) · **2 parches en 1 aplicación** · [Detalles completos](PATCHES.md)

| Aplicación | Parches | Versión compatible | Paquete |
| --- | --- | --- | --- |
| [Wallapop](PATCHES.md#wallapop-comwallapop) | 2 | `1.334.0` | `com.wallapop` |

| Parche | Qué cambia |
| --- | --- |
| Wallapop OLED dark mode | Sigue el modo oscuro del sistema y cambia las superficies principales a negro puro `#000000`. |
| Wallapop No Ads | Desactiva dos decisiones centrales que permiten mostrar publicidad. |

## 📬 Problemas y solicitudes

¿Algo no funciona o tienes una propuesta? Consulta primero las [incidencias existentes](https://github.com/ByXuXy88/wallapop-oled-noads-patches/issues).

- [Reportar un fallo](https://github.com/ByXuXy88/wallapop-oled-noads-patches/issues/new?template=bug_report.yml).
- [El parche falla tras actualizar Wallapop](https://github.com/ByXuXy88/wallapop-oled-noads-patches/issues/new?template=app_update.yml).
- [Proponer una mejora](https://github.com/ByXuXy88/wallapop-oled-noads-patches/issues/new?template=feature_request.yml).

La compatibilidad está limitada a las versiones indicadas. Una versión nueva de Wallapop puede requerir cambios en el código.

## 🐛 Reportar errores

Incluye la versión de Android, Morphe y Wallapop; la versión de la fuente de parches; el origen y formato del paquete (APK, APKM o XAPK); los pasos para reproducir el problema y el registro de parcheo de Morphe.

Para problemas de OLED, indica si el modo oscuro de Android estaba activo y adjunta una captura de la pantalla afectada. Oculta conversaciones, nombres, ubicaciones y otros datos personales antes de publicar capturas o registros.

## ❓ Preguntas frecuentes

### ¿OLED significa que todo pasa a negro?

Las tres superficies principales de Compose usan negro puro en modo oscuro. Las vistas clásicas reciben recursos de noche por función. Se conservan fotografías y los colores de marca, textos e iconos de la paleta. Algunas pantallas con colores fijados por código o WebViews pueden necesitar ajustes.

### ¿Qué anuncios elimina?

El parche desactiva dos decisiones centrales de publicidad. Su alcance necesita pruebas de uso; no elimina productos promocionados por vendedores ni desbloquea servicios de pago.

### ¿Está probado en Android?

Se ha compilado el bundle Android y se han aplicado ambos parches con el motor patcher **1.15.1**, reconstruido el APK y verificado su firma y sus cambios. La carga en Morphe Android **1.34.0**, el arranque y las pantallas en un teléfono siguen pendientes. Wallapop utiliza PairIP, que puede afectar el arranque de un APK modificado.

### ¿Puedo instalarlo sobre la app original?

Morphe firma la aplicación resultante con su propia clave. Una firma diferente impide actualizar directamente la instalación original. Comprueba los datos locales antes de desinstalarla.

## 🛠️ Desarrollo

- [Compilar y publicar](BUILDING.md).
- [Cómo contribuir](CONTRIBUTING.md).
- [Historial de cambios](CHANGELOG.md).
- [Resultados de verificación](verification/verified-apk.json).

GitHub Actions compila el bundle `.mpp` y publica las versiones y los metadatos que consume Morphe. No hace falta un token personal para ejecutar el flujo de publicación.

## ℹ️ Acerca del proyecto

Fuente independiente de parches, sin afiliación con Wallapop o Morphe. Código bajo [GPL-3.0](LICENSE); atribuciones en [NOTICE.md](NOTICE.md). El repositorio distribuye los parches, sin APK de Wallapop ni claves de firma.

La organización de esta documentación toma como referencia [rushiranpise/morphe-patches](https://github.com/rushiranpise/morphe-patches), con contenido propio para este proyecto.
