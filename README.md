# SDK para MiPOT

Este es el repositorio oficial para las versiones de SDK del dispositivo MiPOT


## Guía de inicio

Para instalar solo necesitas descargar el archivo .aar

### Prerequisitos

* Desarrollado en Java
* Tener, como mínimo, Android API 21 en el proyecto a integrar

### Installing
En la carpeta de tu proyecto /ejemplo/app/libs/ añade el archivo mipot.aar
para agregar las funciones de MiPOT

En **Gradle Scripts**, en el archivo app, agregar o confirmar que se tiene la siguiente
línea de configuración:

build.gradle
```
android{
    compileSdkVersion xx
    defaultConfig {
        ...
    }
    buildTypes {
      ...
    }
}

dependencies{
  implementation fileTree(dir: 'libs', include: ['*.aar']) // Linea para agregar .aar  
  ...
}

```
### Integración

En esta sección se detallarán los pasos necesarios para que MiPOT funcione con tu
aplicación.

Se pueden dividir las tareas en 2 partes:
- Integración del lector QR
- Integración de la comunicación BLE

Lo primero es agregar los permisos necesarios al archivo manifest

```
 <uses-permission android:name="android.permission.INTERNET"/>
```
Para hacer peticiones al API de MiPOT


**Lector QR**
Para abrir el escaner con la acción de un botón se tiene lo siguiente:

```
button.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(getApplicationContext(), MiPOTCameraActivity.class);
               startActivityForResult(intent,ESCANER_REQUEST_MIPOT);
           }
       });
```
El SDK se hace cargo de los permisos de cámara.
El código anterior abrirá la cámara y buscará un QR de MiPOT.

La respuesta se obtiene en onActivityResult
```
if(requestCode == ESCANER_REQUEST_MIPOT && data != null){
  Barcode barcode = data.getParcelableExtra(MiPOTCameraActivity.BARCODE_OBJECT);
  Intent intent = new Intent(getApplicationContext(),BLEActivity.class);
  intent.putExtra(EXTRA_BARCODE_DATA,barcode.displayValue);
  startActivity(intent);
}
```
Donde BLEActivity es una actividad que manipulará los servicios de Bluetooth
y mostrará información relevante de MiPOT

**Conexión BLE**
Para la conexión BLE es importante:
- Tener el barcode que se obtiene al leer QR
- Agregar un ServiceConnection
- Agregar un BroadcastReceiver
- Revisar la conexión BLE


*Paso 1: Obtener Barcode*

El siguiente bloque de código se agrega en el onCreate y sirve para obtener el
barcode del QR leído y
```
Intent dataBarcode = getIntent();
if(dataBarcode.hasExtra(EXTRA_BARCODE_DATA)){
  BluetoothManager bluetoothManager = (BluetoothManager) Objects.requireNonNull(getApplicationContext()).getSystemService(Context.BLUETOOTH_SERVICE);
  BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
  if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BLUETOOTH_REQUEST);
  }else{
    barcode = dataBarcode.getStringExtra("BARCODE");
    lastData.setText(barcode);
  }

}
```









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
