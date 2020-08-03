package com.example.scanealo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Escanear extends AppCompatActivity {

    Button botonScanear,btnActualizar;
    TextView tvCodigo;
    EditText tvCodigoBarras;
    TextView tvNombre;
    EditText tvPrecio;
    EditText etStockP;
    ImageView imgPrd;
    String host;
    String stockAnt;

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
        btnActualizar=findViewById(R.id.button);

        botonScanear.setOnClickListener(monClickListener);

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarProducto(host+"/actualiza_prd.php");
            }
        });

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

            stockAnt=stock; //En esta variable tomo el valor del stock antes de los cambios

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


    public void GuardarProducto(View v) {
        final ProgressDialog loading = new ProgressDialog(Escanear.this);
        loading.setMessage("Procesando...");
        loading.show();
        loading.setCanceledOnTouchOutside(false);
        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, host + "/post_producto",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            loading.dismiss();
                            Log.d("JSON", response);

                            JSONObject eventObject = new JSONObject(response);
                            String error_status = eventObject.getString("error");

                            if (error_status.equals("true")) {
                                String error_msg = eventObject.getString("msg");
                                ContextThemeWrapper ctw = new ContextThemeWrapper(Escanear.this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
                                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctw);
                                alertDialogBuilder.setTitle("Error en el servidor");
                                alertDialogBuilder.setCancelable(false);
                                alertDialogBuilder.setMessage(error_msg);
                                alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });
                                alertDialogBuilder.show();

                            } else {
                                String error_msg = eventObject.getString("msg");
                                ContextThemeWrapper ctw = new ContextThemeWrapper(Escanear.this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
                                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctw);

                                alertDialogBuilder.setTitle("Registro");
                                alertDialogBuilder.setCancelable(false);
                                alertDialogBuilder.setMessage(error_msg);

                                final String idMascota = eventObject.getString("idMascota");
                                final String nombre = eventObject.getString("nombre");
//                                alertDialogBuilder.setIcon(R.drawable.doubletick);

                                alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent ide = new Intent(Escanear.this, confirmacionGuardado.class);
                                        ide.putExtra("idMascota", idMascota);
                                        ide.putExtra("nombre", nombre);
                                        startActivity(ide);
                                        finish();
                                    }
                                });
                                alertDialogBuilder.show();
                            }
                        } catch (Exception e) {
                            Log.d("Tag", e.getMessage());

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            ContextThemeWrapper ctw = new ContextThemeWrapper(Escanear.this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctw);
                            alertDialogBuilder.setTitle("No connection");
                            alertDialogBuilder.setMessage(" Connection time out error please try again ");
                            alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });
                            alertDialogBuilder.show();
                        } else if (error instanceof AuthFailureError) {
                            ContextThemeWrapper ctw = new ContextThemeWrapper(Escanear.this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctw);
                            alertDialogBuilder.setTitle("Connection Error");
                            alertDialogBuilder.setMessage(" Authentication failure connection error please try again ");
                            alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });
                            alertDialogBuilder.show();
                            //TODO
                        } else if (error instanceof ServerError) {
                            ContextThemeWrapper ctw = new ContextThemeWrapper(Escanear.this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctw);
                            alertDialogBuilder.setTitle("Connection Error");
                            alertDialogBuilder.setMessage("Connection error please try again");
                            alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });
                            alertDialogBuilder.show();
                            //TODO
                        } else if (error instanceof NetworkError) {
                            ContextThemeWrapper ctw = new ContextThemeWrapper(Escanear.this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctw);
                            alertDialogBuilder.setTitle("Connection Error");
                            alertDialogBuilder.setMessage("Network connection error please try again");
                            alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });
                            alertDialogBuilder.show();
                            //TODO
                        } else if (error instanceof ParseError) {
                            ContextThemeWrapper ctw = new ContextThemeWrapper(Escanear.this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctw);
                            alertDialogBuilder.setTitle("Error");
                            alertDialogBuilder.setMessage("Parse error");
                            alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });
                            alertDialogBuilder.show();
                        }
//                        Toast.makeText(Login_Activity.this,error.toString(), Toast.LENGTH_LONG ).show();
                    }
                }) {

 /*
            @Override

     TextView tvCodigo;
    EditText tvCodigoBarras;
    TextView tvNombre;
    EditText tvPrecio;
    EditText etStockP;

            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("id", Integer.toString(idUsuario));
                map.put("nombre", etMasNombre.getText().toString());
                map.put("edad_anios", etMasAnos.getText().toString());
                map.put("edad_meses", etMasMeses.getText().toString());
                map.put("raza", spMasRaza.getSelectedItem().toString());
                map.put("genero", spMasGenero.getSelectedItem().toString());
                map.put("color", etMasColor.getText().toString());
                map.put("alergias", etMasAlergias.getText().toString());
                map.put("peso", spMasPeso.getSelectedItem().toString());
                map.put("descripcion", etMasDescripcion.getText().toString());
                map.put(KEY_User_Document1, Document_img1);
                return map;
            }
 */

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(mRetryPolicy);
        requestQueue.add(stringRequest);
    }


    private void actualizarProducto(String URL) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Producto Actualizado Correctamente", Toast.LENGTH_SHORT).show();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {

            String nombreImg = "";

            @Override

            //Obtiene los parametros que necesitamos del WEB Service
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("prdPrecioVenta", tvPrecio.getText().toString());
                parametros.put("prdExistencia", etStockP.getText().toString());
                parametros.put("prdImagen", nombreImg);
                parametros.put("prdCodigo", tvCodigo.getText().toString());
                return parametros;

                //if(stockAnt==etStockP.getText().toString()){
                //    Toast.makeText(getApplicationContext(), "Se requiere realizar Ajuste", Toast.LENGTH_SHORT).show();
                //}
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void insertarAjuste(String URL) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Producto Actualizado Correctamente", Toast.LENGTH_SHORT).show();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {

            String nombreImg = "";
            Date c = Calendar.getInstance().getTime();
            //System.out.println("Current time => " + c);

            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            String formattedDate = df.format(c);

            String fechaActual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            String tvNombreDispositivo= obtenerNombreDeDispositivo() ;

            @Override

            //Obtiene los parametros que necesitamos del WEB Service
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("ajuFecha", fechaActual);
                parametros.put("ajuTipo", "1");
                parametros.put("ajuEstado", "1");
                parametros.put("usuario", tvCodigo.getText().toString());
                parametros.put("equipo", tvCodigo.getText().toString());
                return parametros;

                //if(stockAnt==etStockP.getText().toString()){
                //    Toast.makeText(getApplicationContext(), "Se requiere realizar Ajuste", Toast.LENGTH_SHORT).show();
                //}
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void insertarAjusteDet(String URL) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Producto Actualizado Correctamente", Toast.LENGTH_SHORT).show();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {

            String nombreImg = "";

            @Override

            //Obtiene los parametros que necesitamos del WEB Service
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("prdPrecioVenta", tvPrecio.getText().toString());
                parametros.put("prdExistencia", etStockP.getText().toString());
                parametros.put("prdImagen", nombreImg);
                parametros.put("prdCodigo", tvCodigo.getText().toString());
                return parametros;

                //if(stockAnt==etStockP.getText().toString()){
                //    Toast.makeText(getApplicationContext(), "Se requiere realizar Ajuste", Toast.LENGTH_SHORT).show();
                //}
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public String obtenerNombreDeDispositivo() {
        String fabricante = Build.MANUFACTURER;
        String modelo = Build.MODEL;
        if (modelo.startsWith(fabricante)) {
            return primeraLetraMayuscula(modelo);
        } else {
            return primeraLetraMayuscula(fabricante) + " " + modelo;
        }
    }


    private String primeraLetraMayuscula(String cadena) {
        if (cadena == null || cadena.length() == 0) {
            return "";
        }
        char primeraLetra = cadena.charAt(0);
        if (Character.isUpperCase(primeraLetra)) {
            return cadena;
        } else {
            return Character.toUpperCase(primeraLetra) + cadena.substring(1);
        }
    }
}