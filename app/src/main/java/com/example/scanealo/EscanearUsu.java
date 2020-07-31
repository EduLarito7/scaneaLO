package com.example.scanealo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EscanearUsu extends AppCompatActivity {

    Button botonScanear;
    EditText tvCodigoBarras;
    TextView tvNombre;
    TextView tvPrecio;
    ImageView imgPrd;
    String host;

    public static final String KEY_User_Document1 = "image";
    private String Document_img1 = "";
    private String photoNameTemp;

    private Boolean IS_SCANNER = false;
    private Boolean IS_CAMMERA = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escanear_usu);

        botonScanear = findViewById(R.id.btnScanner1);
        tvCodigoBarras = findViewById(R.id.etBarras1);
        tvNombre = findViewById(R.id.tv1Nombre1);
        tvPrecio = findViewById(R.id.tvPrecio1);
        imgPrd=findViewById(R.id.imgProducto1);

        botonScanear.setOnClickListener(monClickListener);

        host = getString(R.string.host);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents() != null)
                //tvCodigoBarras.setText("El codigo de barras es:\n"+ result.getContents());
                tvCodigoBarras.setText(result.getContents());
                Button btConsultar = findViewById(R.id.btConsultar1);
                getData(btConsultar.getRootView());


        }else {
            tvCodigoBarras.setText("ERROR AL SCANEAR");
        }
    }



    private View.OnClickListener monClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnScanner1:
                    new IntentIntegrator(EscanearUsu.this).initiateScan();
                    break;
            }
        }
    };


    public String BitMapToString(Bitmap userImage1) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        userImage1.compress(Bitmap.CompressFormat.PNG, 60, baos);
        byte[] b = baos.toByteArray();
        Document_img1 = Base64.encodeToString(b, Base64.DEFAULT);
        return Document_img1;
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }



    public void getData(View v){
        //Dirección del servidor donde esta la base de datos, en este caso de nuestra máquina local
        String ws = host + "/post_producto.php?prdCodBarrasQr="+tvCodigoBarras.getText();

        //Habilitar permisos de la app para el acceso a nuestro Web service
        StrictMode.ThreadPolicy politica = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(politica);
        URL url = null;
        HttpURLConnection conn;

        //Captura de Excepciones
        try {
            //Llamada a nuestro WS,
            url = new URL(ws);
            //Realizamos un cast de tipo HttpUrlConnection y realizamos la coneccion a la base de datos
            conn = (HttpURLConnection) url.openConnection();
            //Definimos el método que vamos a utilizar del WS
            conn.setRequestMethod("GET");
            conn.connect();

            //Utilizamos un Buffer para la lectura de datos, lo que llegue de GET, se almacena en la variable: in
            BufferedReader in = new BufferedReader((new InputStreamReader(conn.getInputStream())));

            //variables para llenar los datos del GET, el Buffer que llega se lo convierte a String y el String a Json
            String inputLine;
            StringBuffer response = new StringBuffer();
            String json="";

            //Este ciclo hace la conversión antes mencionada: Buffer---> String ----> Json
            while ((inputLine = in.readLine())!=null){
                response.append(inputLine);
            }

            json = response.toString(); //Aqui obtenemos los datos de tipo String
            JSONArray jsonArr=null;

            //Declaramos las variables donde vamos almacenar los datos obtenidos
            String nombre = "";
            String barras = "";
            String precioVenta = "";
            String rutaImg="";

            try {
                JSONObject jsonResponse = new JSONObject(json);
                //nombre =jsonResponse.getString("UsuTipo");
                nombre =jsonResponse.getString("prdNombre");
                barras =jsonResponse.getString("prdCodBarrasQr");
                precioVenta =jsonResponse.getString("prdPrecioVenta");
                rutaImg=jsonResponse.getString("prdImagen");


            } catch (JSONException e) {
                e.printStackTrace();
            }

            new DownloadImageTask((ImageView) findViewById(R.id.imgProducto1)).execute(host+"/"+rutaImg);

            tvNombre.setText(nombre);
            tvPrecio.setText(precioVenta);

        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}
