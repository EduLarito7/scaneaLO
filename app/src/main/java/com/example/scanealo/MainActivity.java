package com.example.scanealo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    EditText etUs, etClv;
    Button btlogin, tvRegistrar;
    String usuario, password;
    String host;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etUs = findViewById(R.id.etUsuario);
        etClv = findViewById(R.id.etClave);
        btlogin= findViewById(R.id.btnLogin);
        tvRegistrar= findViewById(R.id.btnRegistrar);
        host= getString(R.string.host);

        tvRegistrar = findViewById(R.id.btnRegistrar);
        //recupero los últimos datos ingresados correctamente
        recuperarPreferencias();



        btlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usuario=etUs.getText().toString();
                password=etClv.getText().toString();
                if(!usuario.isEmpty() && !password.isEmpty()){
                    validarUsuario(host+"/validar_usuario.php");
                }else{
                    Toast.makeText(MainActivity.this, "Por favor ingrese los datos de Usuario/Clave",Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void validarUsuario(String URL){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    String tipoUsu = "";

                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        tipoUsu =jsonResponse.getString("usuTipo");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //En esta linea nos guarda los datos de usuario en preferencia
                    guardarPreferencias();

                    //Aqui verificamos que Ventana abrir: Admistrador / Usuario Final
                    if(tipoUsu.equals("A")){
                        Intent intent = new Intent(getApplicationContext(),Escanear.class);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(getApplicationContext(),EscanearUsu.class);
                        startActivity(intent);
//                        Toast.makeText(MainActivity.this, "Pagina de Usuario en construcción", Toast.LENGTH_SHORT).show();
                    }

                    //Intent intent = new Intent(getApplicationContext(),Escanear.class);
                    //startActivity(intent);

                    finish();
                }else{
                    Toast.makeText(MainActivity.this, "Usuario o Contraseña no validos", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override

          //Obtiene los parametros que necesitamos del WEB Service
            protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> parametros = new HashMap<String,String>();
                    //En estas líneas se pueden llenar los datos recuperados de preferencias
                    parametros.put("usuario",usuario);
                    //parametros.put("password",password);
                    //parametros.put("usuario",etUs.getText().toString());
                    parametros.put("password",etClv.getText().toString());
                    return parametros;
            }
        };

        //Ejecuta el web Service
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //Peticiones desde la APP
        requestQueue.add(stringRequest);

    }

    private void guardarPreferencias(){
        //Esto sirve para recuperar los datos guardados de usuario y clave
        SharedPreferences preferences=getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("usuarioApp",usuario);
        editor.putString("passwordApp",password);
        //editor.putString("sesion",true);
        editor.commit(); //Guardar todos los cambios
    }

    private void recuperarPreferencias(){
        SharedPreferences preferences=getSharedPreferences("preferenciasLogin",Context.MODE_PRIVATE);
        etUs.setText(preferences.getString("usuarioApp","Usuario"));
        etClv.setText(preferences.getString("passwordApp","Contraseña"));
    }


    public void registroUsu(View v){
        Intent internetReg = new Intent(MainActivity.this,RegistrarUsuario.class);
        startActivity(internetReg);
    }


}