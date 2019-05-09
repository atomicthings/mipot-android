# SDK para MiPOT (Actualización 06/05/19)
# Versión actual: 0.3

Este es el repositorio oficial para las versiones de SDK del dispositivo MiPOT

![alt text](https://img.icons8.com/color/48/000000/error.png "Alerta")![alt text](https://img.icons8.com/color/48/000000/error.png "Alerta")![alt text](https://img.icons8.com/color/48/000000/error.png "Alerta")
## Actualiaciones !!!



Desde la primera versión de SDK (0.1) solo se ha realizado una actualización que
 corresponde a la 0.3.
Estos son los puntos correspondientes:

* Se renombraron variables para facilitar su uso  **Requiere cambios**
* Se solucionó el problema de las variables cruzadas en la versión 0.1  **Requiere cambios**
* El objeto obtenido en el QR pasó a tipo String (antes barcode) **Requiere cambios**
* Mejoras en seguridad **Requiere cambios**


Estos cambios aplican para la versión del firmware de MiPOT (v0.96L)

### Guía de inicio

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


![alt text](https://img.icons8.com/color/48/000000/error.png "Alerta") **Cambio en la nueva versión  1/4 (Parte 1)**
* El objeto obtenido en el QR pasó a tipo String (antes barcode)


La respuesta se obtiene en onActivityResult
```
if(requestCode == ESCANER_REQUEST_MIPOT && data != null){
  String barcode = data.getStringExtra(MiPOTCameraActivity.BARCODE_OBJECT);
  Intent intent = new Intent(getApplicationContext(),BLEActivity.class);
  intent.putExtra(EXTRA_BARCODE_DATA,barcode);
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

![alt text](https://img.icons8.com/color/48/000000/error.png "Alerta") **Cambio en la nueva versión 2/4**
* Se renombraron variables para facilitar su uso
* Se solucionó el problema de las variables cruzadas en la versión 0.1  

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
      case BLEService.ACTION_VERIFICATION_DATA:
          // Aqui va la respuesta de miPotGetAuthData
      break;
      case BLEService.ACTION_WALLET_DEVICE_VERIFICATION:
          // Aqui va la respuesta de miPotValidation
      break;
      case BLEService.ACTION_WALLET_MIPOT_SEND_DATA:
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

![alt text](https://img.icons8.com/color/48/000000/error.png "Alerta") **Cambio en la nueva versión 3/4**
* Se renombraron variables para facilitar su uso
* Se solucionó el problema de las variables cruzadas en la versión 0.1  


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

    intentFilter.addAction(BLEService.ACTION_VERIFICATION_DATA);
    intentFilter.addAction(BLEService.ACTION_WALLET_MIPOT_SEND_DATA);
    intentFilter.addAction(BLEService.ACTION_WALLET_DEVICE_VERIFICATION);
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
    barcode = dataBarcode.getStringExtra(EXTRA_BARCODE_DATA);
  }
}
```
**Paso 3 y 4: Establecer conexión**
Si se puede inicializar una conexión (paso 3) se realiza la conexión (paso 4)
```
if(miPot.miPotInitializeConnection()){
  miPot.miPotConnect(barcode);
}
```

**Paso 5: Solicitar id MiPOT para validación**
Se debe solicitar el id de MiPOT para verificar que el dispositivo
sea autentico
```
if(!miPot.miPotGetAuthData()){
  // Si entra aquí se muestra un mensaje de error
}
```

**Paso 6: Recibiendo la respuesta del id**
En el switch del BroadcastReceiver caerá la respuesta de MiPOT
al haber solicitado el id. Se tiene un tipo de dato HashMap<String,String>
```
case BLEService.ACTION_VERIFICATION_DATA:
```

**Paso 7: Verificando cadena con id en el servidor**
Una vez que se tiene la cadena, se debe enviar una solicitud al servidor
de AtomicThings para verificar MiPOT
#POST
- header ->  Content-Type:application/x-www-form-urlencoded
- body   ->  message: Hashmap en formato JSON (new JSONObject(map))



![alt text](https://img.icons8.com/color/48/000000/error.png "Alerta") **Cambio en la nueva versión 4/4**
*  Mejoras en seguridad


**Paso 8: El resultado de la petición POST**
Este paso consiste en solo tener control del resultado de la petición.
La respuesta tiene el siguiente formato:
*{message:cadenaCifrada, token:tokenDeCifrado}*

Se debe de obtener la cadenaCifrada y el token para agregar como parámetros al
método miPotValidation(cadenaCifrada,tokenDeCifrado)


**Paso 9: Regresando respuesta a MiPOT**
Para seguir con el proceso se requiere mandar la información a MiPOT
Esto se hace con miPotValidation
```
if(!miPot.miPotValidation(cadenaCifrada,tokenDeCifrado)){
  // Si entra aquí se muestra un mensaje de error
}
```

**Paso 10: Validando MiPOT**
MiPOT se puede desconectar. Algunas razones son:
- El dispositivo MiPOT fue alterado o es un clon del original
- Hemos detectado comportamiento extraño y se tomó la decisión de bloquearlo
Se manda un código de error a
```
case BLEService.ACTION_WALLET_DEVICE_VERIFICATION:
```
para conocer el estado del sistema.


**Paso 11 y 12**
Estos pasos corresponden a la petición de tu Wallet para hacer un intento de cobro
El paso 12 corresponde a la obtención del resultado listo para mandarlo a MiPOT


**Paso 13: MiPOT muestra el resultado**
Para envíar resultado a MiPOT es necesario usar
```
if(!miPot.miPotSendData(true,monto)){
  // Si entra aquí se muestra un mensaje de error (No fue posible mandar mensaje)
}
```
donde el primer parámetro consiste en el estado de la transacción y el segundo en
el monto. El primer parámetro es un boolean y el segundo un String

**MiPOT enciende en este paso**


**Paso 14: Fin de la comunicación**
Se puede obtener un último mensaje en
```
case BLEService.ACTION_WALLET_MIPOT_SEND_DATA
```
que sirve para conocer, si es que hubo algún error, la razón por la cual
MiPOT no lograra encender.


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
