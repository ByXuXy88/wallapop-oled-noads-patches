# Compilar y publicar

## Publicar en GitHub desde el teléfono

1. Crea un repositorio público llamado `wallapop-oled-noads-patches` en tu cuenta. El nombre es opcional: el flujo utiliza automáticamente el repositorio donde se ejecuta.
2. Extrae el ZIP de esta entrega. Sube **el contenido** de la carpeta del proyecto al nivel principal del repositorio, incluyendo `.github/workflows/release.yml`, `scripts`, `src`, `LICENSE` y `toolchain.lock.json`. No subas el ZIP como único archivo ni el APK de Wallapop.
3. En **Actions → Build and release patches → Run workflow**, usa la rama `main`, versión `0.1.0`, y activa **Publish this version as a GitHub release**.
4. Espera a que termine correctamente. El flujo actualizará también `patches-bundle.json` en la rama `main`, que es donde Morphe busca los metadatos al añadir la URL del repositorio. La release `v0.1.0` contendrá `patches-0.1.0.mpp`, `patches-bundle.json`, el listado y las huellas SHA-256.

No necesitas crear ni compartir un token personal. El flujo usa el token automático de GitHub únicamente para publicar en tu propio repositorio. La compilación descarga distribuciones públicas con versiones y SHA-256 fijados; no depende de autenticarte en GitHub Packages.

Subir solo código no basta para que Morphe descargue parches: es necesaria la release con el bundle Android `.mpp` y `patches-bundle.json`.

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
