package ricardoj.app.pruebasskd;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import atomic.things.mipot.barcode.MiPOTCameraActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = (TextView) findViewById(R.id.main_value_textview);
        Button button = (Button) findViewById(R.id.camera);

        textView.setText("Pruebas SDK");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MiPOTCameraActivity.class);
                startActivityForResult(intent,100);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Toast.makeText(getApplicationContext(),"Request: " + requestCode + " Result: " + resultCode,Toast.LENGTH_SHORT).show();
        if(requestCode == 100 && data != null){
            String barcode = data.getStringExtra(MiPOTCameraActivity.BARCODE_OBJECT);
            Intent intent = new Intent(getApplicationContext(),BLEActivity.class);
            Toast.makeText(getApplicationContext(),"BARCODE: " + barcode,Toast.LENGTH_SHORT).show();
            intent.putExtra("BARCODE",barcode);
            startActivity(intent);
        }
    }
}
