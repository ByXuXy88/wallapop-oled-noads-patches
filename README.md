# Wallapop OLED + No Ads

Fuente de parches para Wallapop 1.334.0 (`com.wallapop`, código 10141414), diseñada para Morphe 1.34.0 con patcher 1.15.1. Wallapop requiere Android 12L o superior.

## Esquema
1. Publicar la fuente en GitHub.
2. Añadirla a Morphe desde Android.
3. Compilar y comprobar cambios.

## Parches

| Parche | Comportamiento |
| --- | --- |
| Wallapop OLED dark mode | Sigue el modo oscuro de Android. Las tres superficies principales de Compose usan negro puro #000000. Los colores originales de textos, iconos y marca se conservan en esa paleta. Las vistas clásicas reciben recursos de noche por función. |
| Wallapop No Ads | Desactiva dos decisiones centrales que permiten mostrar publicidad. No desbloquea pagos ni elimina productos promocionados de vendedores. |

El modo claro mantiene los valores originales de la paleta y los colores XML que se reemplazan por referencias temáticas. El padre del tema pasa a DayNight para habilitar los recursos de noche. No se invierten fotografías ni colores globales compartidos.

## Publicar en GitHub desde el teléfono

1. Crea un repositorio público llamado `wallapop-oled-noads-patches` en tu cuenta. El nombre es opcional: el flujo utiliza automáticamente el repositorio donde se ejecuta.
2. Extrae el ZIP de esta entrega. Sube **el contenido** de la carpeta del proyecto al nivel principal del repositorio, incluyendo `.github/workflows/release.yml`, `scripts`, `src`, `LICENSE` y `toolchain.lock.json`. No subas el ZIP como único archivo ni el APK de Wallapop.
3. En **Actions → Build and release patches → Run workflow**, usa la rama `main`, versión `0.1.0`, y activa **Publish this version as a GitHub release**.
4. Espera a que termine correctamente. El flujo actualizará también `patches-bundle.json` en la rama `main`, que es donde Morphe busca los metadatos al añadir la URL del repositorio. La release `v0.1.0` contendrá `patches-0.1.0.mpp`, `patches-bundle.json`, el listado y las huellas SHA-256.

No necesitas crear ni compartir un token personal. El flujo usa el token automático de GitHub únicamente para publicar en tu propio repositorio. La compilación descarga distribuciones públicas con versiones y SHA-256 fijados; no depende de autenticarte en GitHub Packages.

Subir solo código no basta para que Morphe descargue parches: es necesaria la release con el bundle Android `.mpp` y `patches-bundle.json`.

## Añadir la fuente a Morphe

Cuando la release exista, abre Morphe → **Sources/Fuentes → + → Remote/Remota** y pega la URL de tu repositorio. Para el nombre propuesto:

`https://github.com/ByXuXy88/wallapop-oled-noads-patches`

También puedes abrir:

`https://morphe.software/add-source?github=ByXuXy88/wallapop-oled-noads-patches`

Si eliges otra cuenta o nombre, cambia esas URLs. Después selecciona el APK original de Wallapop 1.334.0, activa los dos parches y genera la aplicación. Activa el modo oscuro de Android para ver el fondo OLED. Una fuente remota permite que Morphe busque versiones nuevas del paquete de parches.

Antes de publicarlo, puedes importar el `.mpp` de esta entrega como fuente **Local** y probarlo en el teléfono. Una fuente local no se actualiza automáticamente.

## Estado comprobado

- Compilación Kotlin real contra patcher 1.15.1: correcta.
- Conversión D8 a un bundle con clases JVM y DEX Android: correcta.
- Carga de los parches y filtro de compatibilidad: correctos en Morphe Desktop 1.18.1, que incluye patcher 1.15.1.
- Aplicación de ambos parches, reconstrucción completa y firma del APK suministrado: correctas.
- Verificación independiente de la firma y comprobaciones de las modificaciones en DEX/recursos reconstruidos.

El APK suministrado es un APK base dividido: declara `requiredSplitTypes="base__abi,base__density"` y `com.android.vending.splits.required=true`, y no incluye bibliotecas nativas. El APK reconstruido conserva esos requisitos. **No es una instalación autónoma:** para instalar desde el teléfono necesitarás el paquete completo de Wallapop 1.334.0 con los splits correspondientes, en un formato que Morphe pueda procesar. El bundle de parches es independiente de estos archivos.

No se ha instalado ni probado visualmente en un teléfono. Los componentes con colores asignados directamente por código, WebViews y otras pantallas pueden necesitar ajustes. Wallapop utiliza PairIP: reconstruir y firmar no demuestra que permita arrancar una versión modificada. El alcance de No Ads y posibles huecos publicitarios requieren pruebas de uso.

La comprobación con el mismo motor no sustituye a probar la carga Android en Morphe 1.34.0. El `.mpp` incluye DEX para esa carga y declara patcher 1.15.1.

## Compilar localmente

JDK 21 y Python 3.12:

```sh
python3 scripts/build.py --version 0.1.0 --repository TU_USUARIO/wallapop-oled-noads-patches
```

Salida en `build/release`. El script comprueba los hashes de sus herramientas, compila únicamente estos parches, crea el `.mpp` Android y genera los metadatos de release para el repositorio indicado.

Para comprobar un APK después de parchearlo:

```sh
python3 -m pip install androguard==4.1.4
python3 tests/verify_patched_apk.py original.apk patched.apk
```

Para una versión nueva: cambia el código y `CHANGELOG.md`, ejecuta Actions con otro número de versión y publica la release. No reutilices números ya publicados salvo para reparar una publicación incompleta.

## Firma e instalación

Morphe firma el APK resultante con su propia clave. No puede actualizar directamente una instalación con otra firma. Comprueba los datos locales antes de desinstalar la aplicación original. No hay claves ni APK de Wallapop en el repositorio.
