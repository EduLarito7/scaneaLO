package com.example.scanealo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class confirmacionGuardado extends AppCompatActivity {

    ImageView img;
    String host;
    Bundle dato;
    String idProducto;
    TextView etNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmacion_guardado);
        dato = getIntent().getExtras();
        idProducto = dato.getString("idProducto");
        String nombre = dato.getString("nombre");
        etNombre = findViewById(R.id.etNombre);
        etNombre.setText(nombre);

        img = findViewById(R.id.QrCode);
        host = getString(R.string.host);
        new DownloadImageTask((ImageView) findViewById(R.id.QrCode)).execute(host.concat("/img/producto_"+idProducto+".png"));
    }

    public void imprimirQr(View v){
        PrintHelper photoPrinter = new PrintHelper(confirmacionGuardado.this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),img);
        BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        photoPrinter.printBitmap("QrCode.jpg - imprimir", bitmap);
    }

    public void inicio(View v){
        Intent intentEnviar = new Intent(confirmacionGuardado.this, Escanear.class);
        startActivity(intentEnviar);
    }
}
