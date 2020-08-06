package com.example.scanealo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import android.content.Intent;

import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class RegistrarUsuario extends AppCompatActivity implements View.OnClickListener{

    EditText usuLogin,usuClave,usuNombre,usuApellido,usuEmail,usuEstado,usuFechaCrea,usuTipo;
    Button btnregistro;
    String usuarioApp;
    String host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_usuario);

        usuLogin = findViewById(R.id.editUsuario);
        usuClave = findViewById(R.id.editClave);
        usuNombre = findViewById(R.id.editNombre);
        usuApellido = findViewById(R.id.editApellido);
        usuEmail = findViewById(R.id.editEmail);

        host = getString(R.string.host);

    }



    @Override
    public void onClick(View v){

        final String ulogin=usuLogin.getText().toString();
        final String uclave=usuClave.getText().toString();
        final String unombre=usuNombre.getText().toString();
        final String uapellido=usuApellido.getText().toString();
        final String uemail=usuEmail.getText().toString();
//        final String ufecha="20200806";
        final String ufecha=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if(!ulogin.isEmpty() && !uclave.isEmpty() && !unombre.isEmpty() && !uapellido.isEmpty() && !uemail.isEmpty()) {
            validarUsuarioApp(host+"/validar_usuarioApp.php",ulogin);

//            if(usuarioApp.equals("") || usuarioApp.equals(null)) {

                Response.Listener<String> respoListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonReponse = new JSONObject(response);
                            boolean success = jsonReponse.getBoolean("success");

                            if (success) {
                                Intent intent = new Intent(RegistrarUsuario.this, MainActivity.class);
                                RegistrarUsuario.this.startActivity(intent);

                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegistrarUsuario.this);
                                builder.setMessage("Error al Registrar usuario")
                                        .setNegativeButton("Retry", null)
                                        .create().show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();

                        }

                    }
                };

                RegisterRequest registerRequest = new RegisterRequest(host+"/Register.php", ulogin, uclave, unombre, uapellido, uemail, "1", ufecha, "U", respoListener);
                RequestQueue queue = Volley.newRequestQueue(RegistrarUsuario.this);
                queue.add(registerRequest);
                Toast.makeText(getApplicationContext(), "Registro de Usuario Exitoso", Toast.LENGTH_SHORT).show();
//            }else {
//                Toast.makeText(getApplicationContext(), "Nombre de usuario ya Existe", Toast.LENGTH_SHORT).show();
//            }
        }else{
            Toast.makeText(getApplicationContext(), "Por favor llene todos los datos", Toast.LENGTH_SHORT).show();
        }

    }

    private void validarUsuarioApp(String URL, final String user){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    String tipoUsu = "";
                    usuarioApp="";

                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        tipoUsu =jsonResponse.getString("usuTipo");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    usuarioApp=tipoUsu;

                }else{
                    usuarioApp="";
                    Toast.makeText(getApplicationContext(), "Usuario o Contraseña no validos", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override

            //Obtiene los parametros que necesitamos del WEB Service
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<String,String>();
                //En estas líneas se pueden llenar los datos recuperados de preferencias
                parametros.put("usuario",user);
                return parametros;
            }
        };

        //Ejecuta el web Service
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //Peticiones desde la APP
        requestQueue.add(stringRequest);

    }


}
