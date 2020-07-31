package com.example.scanealo;

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

public class Escanear extends AppCompatActivity {

    Button botonScanear;
    TextView tvCodigo;
    EditText tvCodigoBarras;
    TextView tvNombre;
    EditText tvPrecio;
    EditText etStockP;
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
        setContentView(R.layout.activity_escanear);

        botonScanear = findViewById(R.id.btnScanner1);
        tvCodigoBarras = findViewById(R.id.etBarras1);
        tvNombre = findViewById(R.id.tv1Nombre1);
        tvCodigo = findViewById(R.id.tv1Codigo);
        tvPrecio = findViewById(R.id.tvPrecio1);
        imgPrd=findViewById(R.id.imgProducto1);
        etStockP=findViewById(R.id.etStock);

        botonScanear.setOnClickListener(monClickListener);

        host = getString(R.string.host);
    }

    private View.OnClickListener monClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnScanner1:
                    IS_SCANNER = true;
                    new IntentIntegrator(Escanear.this).initiateScan();
                    break;
            }
        }
    };
    static final int REQUEST_TAKE_PHOTO = 1;
    public void selectImage(View v) {
        IS_CAMMERA = true;
        final CharSequence[] options = {"Tomar foto", "Escoger de Galería", "Cancelar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(Escanear.this);
        builder.setTitle("Agregar Foto!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Tomar foto")) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                            photoNameTemp = photoFile.getName();

                        } catch (IOException ex) {
                            // Mensaje de Error mientras se cre el archivo
                            Toast.makeText(getApplicationContext(),"Error: "+ ex.getMessage(),Toast.LENGTH_LONG).show();
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),"com.example.scanealo.fileprovider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                        }
                    }
                } else if (options[item].equals("Escoger de Galería")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                } else if (options[item].equals("Cancelar")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    //Método para crear un nombre único de cada fotografia
    String mCurrentPhotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Backup_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(IS_SCANNER){
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
        if(IS_CAMMERA){
            if (resultCode == RESULT_OK) {
                if (requestCode == 1) {
                    File f = new File(Environment.getExternalStorageDirectory().toString()+"/Android/data/com.example.scanealo/files/Pictures");
                    for (File temp : f.listFiles()) {
                        if (temp.getName().equals(photoNameTemp)) {
                            f = temp;
                            break;
                        }
                    }
                    try {
                        Bitmap bitmap;
                        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                        bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), bitmapOptions);
                        bitmap = getResizedBitmap(bitmap, 400);
                        imgPrd.setImageBitmap(bitmap);
                        BitMapToString(bitmap);
                        String path = android.os.Environment
                                .getExternalStorageDirectory()
                                + File.separator
                                + "Phoenix" + File.separator + "default";
                        f.delete();
                        photoNameTemp = null;
                        OutputStream outFile = null;
                        File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                        try {
                            outFile = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                            outFile.flush();
                            outFile.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (requestCode == 2) {
                    Uri selectedImage = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePath[0]);
                    String picturePath = c.getString(columnIndex);
                    c.close();
                    Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                    thumbnail = getResizedBitmap(thumbnail, 400);
                    Log.w("path of image from gallery......******.........", picturePath + "");
                    imgPrd.setImageBitmap(thumbnail);
                    BitMapToString(thumbnail);
                }
                IS_CAMMERA = false;
            }
        }
    }

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
            String codigo = "";
            String barras = "";
            String precioVenta = "";
            String rutaImg="";
            String stock="";

            try {
                JSONObject jsonResponse = new JSONObject(json);
                //nombre =jsonResponse.getString("UsuTipo");
                nombre =jsonResponse.getString("prdNombre");
                codigo =jsonResponse.getString("prdCodigo");
                barras =jsonResponse.getString("prdCodBarrasQr");
                precioVenta =jsonResponse.getString("prdPrecioVenta");
                rutaImg=jsonResponse.getString("prdImagen");
                stock=jsonResponse.getString("prdExistencia");



            } catch (JSONException e) {
                e.printStackTrace();
            }

            new DownloadImageTask((ImageView) findViewById(R.id.imgProducto1)).execute(host+"/"+rutaImg);

            tvNombre.setText(nombre);
            tvCodigo.setText(codigo);
            tvPrecio.setText(precioVenta);
            etStockP.setText(stock);


        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } /*catch (JSONException e) {
            e.printStackTrace();
        }*/

    }




/*

    public void consumirServicio(View v){
        String cedula= tvCodigogetText().toString();
        String nombre= et2.getText().toString();
        String apellido= et3.getText().toString();
        int edad= Integer.parseInt( et4.getText().toString());
        post servicioTask= new post(this,"http://192.168.1.3/rest/post.php",cedula,nombre,apellido, edad);
        servicioTask.execute();

    }

*/
}