# SDK para MiPOT

Este es el repositorio oficial para las versiones de SDK del dispositivo MiPOT


## Guía de inicio

Para instalar solo necesitas descargar el archivo .aar


### Prerequisitos

* Desarrollado en Java

### Installing

Próximamente



### Integración

En esta sección se detallarán los pasos necesarios para que MiPOT funcione con tu
aplicación.

Paso 1: Comprobar permisos del teléfono

```
if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
  // Leer QR
}else{
  ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
}

```


## Deployment


## Built With

* [Android Studio](https://developer.android.com/studio) - IDE de Google para realizar aplicaciones móviles


## Contribuciones
Próximamente

## versión

Usamos [SemVer](http://semver.org/) para determinar una versión.

## Autores

* **RicardoJC**  [Richardo](https://github.com/RicardoJC)


## Licencia

Próximamente

## Agradecimientos

Próximamente
