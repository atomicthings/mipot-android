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


## Lector QR

**Paso 1: Leer QR**
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

## Conexión BLE
Para la conexión BLE es importante:
- Tener el barcode que se obtiene al leer QR
- Agregar un ServiceConnection
- Agregar un BroadcastReceiver
- Revisar la conexión BLE

Service Connection

Es una instancia que contiene instrucciones de lo que pasa cuando se
conecta el servicio BLE y también cuando se desconecta

```
// Variable global

BLEService miPot;

ServiceConnection serviceConnection = new ServiceConnection() {
  @Override
  public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
    miPot = ((BLEService.LocalBinder) iBinder).getService();
  }

  @Override
    public void onServiceDisconnected(ComponentName componentName) {
    miPot = null;
  }
};
```

BroadcastReceiver
Es una instancia importante que consiste en el manejo de los mensajes de MiPOT.
Aquí se encontraran los mensajes que envíe MiPOT a la aplicación dependiendo
lo que se solicite desde la aplicación.
```
BroadcastReceiver receiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    HashMap<String, String> map = (HashMap<String, String>) intent.getSerializableExtra(BLEService.EXTRA_DATA);
    switch (action != null ? action : ""){
      case BLEService.ACTION_MIPOT_RECEIVING_VERIFICATION_DATA:
          // Aqui va la respuesta de miPotGetAuthData
      break;
      case BLEService.ACTION_MIPOT_WALLET_RECEIVING_VERIFICATION_DATA_FROM_DEVICE:
          // Aqui va la respuesta de miPotValidation
      break;
      case BLEService.ACTION_MIPOT_WALLET_RESULT_OF_SENDING_AMOUNT_DATA:
          // Aqui va la respuesta de miPotSendData
      break;
      case BLEService.ACTION_GATT_CONNECTED:
          // MiPOT y el smartphone se han conectado via BLE
          // Aun faltan las configuraciones y no es posible mandar mensajes
          // a MiPOT
        break;
      case BLEService.ACTION_GATT_DISCONNECTED:
          // MiPOT y el smartphone se han desconectado via BLE
        break;
      case BLEService.ACTION_GATT_SERVICES_DISCOVERED:
          // Cuando se tenga esta respuesta significa que las configuraciones
          // quedaron listas y ya es posible empezar la comunicación con MiPOT
      break;
    }
  }
};
```

**Ciclo de vida de la actividad y el servicio**

El siguiente bloque de código sirve para establecer la comunicación
con el servicio de BLE
En *gattIntentFilter()* se agregan las respuestas que se esperan en el BroadcastReceiver

```
protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       ...
       Intent miPotService = new Intent(this, BLEService.class);
       bindService(miPotService,serviceConnection,BIND_AUTO_CREATE);
}

@Override
 protected void onResume() {
     super.onResume();
     registerReceiver(receiver,gattIntentFilter());
 }

 @Override
 protected void onPause() {
     super.onPause();
     unregisterReceiver(receiver);
 }

 @Override
 protected void onDestroy() {
     super.onDestroy();
     unbindService(serviceConnection);
     if(miPot != null)
         miPot.disconnectMiPOT();
     miPot = null;
 }

 private static IntentFilter gattIntentFilter() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
    intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
    intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);

    intentFilter.addAction(BLEService.ACTION_MIPOT_RECEIVING_VERIFICATION_DATA);
    intentFilter.addAction(BLEService.ACTION_MIPOT_WALLET_RECEIVING_VERIFICATION_DATA_FROM_DEVICE);
    intentFilter.addAction(BLEService.ACTION_MIPOT_WALLET_RESULT_OF_SENDING_AMOUNT_DATA);
    return intentFilter;
}

```

**Paso 2: Obtener Barcode**

El siguiente bloque de código se agrega en el onCreate y sirve para obtener el
barcode del QR leído y verificar el Bluetooth encendido.
```
// Variable global
String barcode;

Intent dataBarcode = getIntent();
if(dataBarcode.hasExtra(EXTRA_BARCODE_DATA)){
  BluetoothManager bluetoothManager = (BluetoothManager) Objects.requireNonNull(getApplicationContext()).getSystemService(Context.BLUETOOTH_SERVICE);
  BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
  if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BLUETOOTH_REQUEST);
  }else{
    barcode = dataBarcode.getStringExtra("BARCODE");
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
