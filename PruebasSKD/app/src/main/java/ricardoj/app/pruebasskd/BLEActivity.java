package ricardoj.app.pruebasskd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

import atomic.things.mipot.ble.BLEService;

public class BLEActivity extends AppCompatActivity implements View.OnClickListener {

    private Intent dataBarcode;
    private String barcode;
    private TextView lastData;
    private static int BLUETOOTH_REQUEST = 200;
    private BLEService miPot;
    private HashMap<String,String> currentHash;
    private String url = "CONTACTAR A ATOMIC THINGS";
    private String message,token;

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


    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            currentHash = (HashMap<String, String>) intent.getSerializableExtra(BLEService.EXTRA_DATA);

            switch (action != null ? action : ""){
                case BLEService.ACTION_VERIFICATION_DATA:
                    lastData.setText(currentHash.toString().length() !=0 ? currentHash.toString():"Vacio");
                    Toast.makeText(getApplicationContext(),"miPotGetAuthData",Toast.LENGTH_SHORT).show();
                    break;
                case BLEService.ACTION_WALLET_MIPOT_SEND_DATA:
                    lastData.setText(currentHash.toString().length() !=0 ? currentHash.toString():"Vacio");
                    Toast.makeText(getApplicationContext(),"miPotSendData",Toast.LENGTH_SHORT).show();
                    break;
                case BLEService.ACTION_WALLET_DEVICE_VERIFICATION:
                    lastData.setText(currentHash.toString().length() !=0 ? currentHash.toString():"Vacio");
                    Toast.makeText(getApplicationContext(),"miPotValidation",Toast.LENGTH_SHORT).show();
                    break;
                case BLEService.ACTION_GATT_CONNECTED:
                    break;
                case BLEService.ACTION_GATT_DISCONNECTED:
                    Toast.makeText(getApplicationContext(),"No conectado",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case BLEService.ACTION_GATT_SERVICES_DISCOVERED:
                    // En esta parte
                    Toast.makeText(getApplicationContext(),"Listo para interactuar",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);

        lastData = (TextView) findViewById(R.id.dataTextView);

        Button connect,verify,verifyResponse,amount,api;
        verify = (Button) findViewById(R.id.buttonVerifyMiPOT);
        verifyResponse = (Button) findViewById(R.id.buttonVerifyResponseMiPOT);
        amount = (Button) findViewById(R.id.buttonSendAmount);
        connect = (Button) findViewById(R.id.buttonConnect);
        api = (Button) findViewById(R.id.buttonAPI);

        connect.setOnClickListener(this);
        verify.setOnClickListener(this);
        verifyResponse.setOnClickListener(this);
        amount.setOnClickListener(this);
        api.setOnClickListener(this);

        dataBarcode = getIntent();
        if(dataBarcode.hasExtra("BARCODE")){
            BluetoothManager bluetoothManager = (BluetoothManager) Objects.requireNonNull(getApplicationContext()).getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BLUETOOTH_REQUEST);
            }else{
                barcode = dataBarcode.getStringExtra("BARCODE");
                lastData.setText(barcode);
            }

        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == BLUETOOTH_REQUEST && resultCode != 0){
            barcode = dataBarcode.getStringExtra("BARCODE");
            lastData.setText(barcode);
        }else{
            Toast.makeText(getApplicationContext(),"Es necesario activar Blueetooth para continuar",Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonConnect:
                Toast.makeText(getApplicationContext(),"Connect",Toast.LENGTH_SHORT).show();

                if(miPot.miPotInitializeConnection()){
                    Toast.makeText(getApplicationContext(),"Servicio conectado",Toast.LENGTH_SHORT).show();
                    miPot.miPotConnect(barcode);
                }
                break;
            case R.id.buttonVerifyMiPOT:
                Toast.makeText(getApplicationContext(),"Verify MiPOT",Toast.LENGTH_SHORT).show();
                // Comprobar BLE
                if(!miPot.miPotGetAuthData()){
                    Toast.makeText(getApplicationContext(),"No fue posible escribir",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.buttonVerifyResponseMiPOT:
                Toast.makeText(getApplicationContext(),"Response MiPOT",Toast.LENGTH_SHORT).show();

                if(!miPot.miPotValidation(message,token)){
                    Toast.makeText(getApplicationContext(),"No fue posible escribir 2",Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.buttonSendAmount:
                Toast.makeText(getApplicationContext(),"Send Data to MiPOT",Toast.LENGTH_SHORT).show();
                if(!miPot.miPotSendData(true,"100000.99")){
                    Toast.makeText(getApplicationContext(),"No fue posible escribir 3",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.buttonAPI:
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());


                JsonObjectRequest request = null;
                request = new JsonObjectRequest(Request.Method.POST, url,new JSONObject(currentHash), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        lastData.setText(response.toString());

                        if(response.has("message")){
                            try {
                                message = response.getString("message");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                message = "";
                            }
                        }

                        if(response.has("token")){
                            try {
                                token = response.getString("token");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                token = "";
                            }
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error.networkResponse != null && error.networkResponse.data != null){
                            String dataError = new String(error.networkResponse.data);
                            VolleyError volleyError = new VolleyError(dataError);
                            volleyError.getMessage();
                        }

                    }
                });

                queue.add(request);
                break;
        }
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
}
