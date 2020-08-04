package com.example.scanealo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import android.content.Intent;

import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class RegistrarUsuario extends AppCompatActivity implements View.OnClickListener{

    EditText usuLogin,usuClave,usuNombre,usuApellido,usuEmail,usuEstado,usuFechaCrea,usuTipo;
    Button btnregistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_usuario);

        usuLogin = findViewById(R.id.editUsuario);
        usuClave = findViewById(R.id.editClave);
        usuNombre = findViewById(R.id.editNombre);
        usuApellido = findViewById(R.id.editApellido);
        usuEmail = findViewById(R.id.editEmail);

    }


    @Override
    public void onClick(View v){
        final String ulogin=usuLogin.getText().toString();
        final String uclave=usuClave.getText().toString();
        final String unombre=usuNombre.getText().toString();
        final String uapellido=usuApellido.getText().toString();
        final String uemail=usuEmail.getText().toString();
        final String ufecha="20200805";


        Response.Listener<String>respoListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonReponse = new JSONObject(response);
                    boolean success = jsonReponse.getBoolean("success");

                    if (success){
                        Intent intent = new Intent(RegistrarUsuario.this,MainActivity.class);
                        RegistrarUsuario.this.startActivity(intent);

                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrarUsuario.this);
                        builder.setMessage("error registro")
                                .setNegativeButton("Retry",null)
                                .create().show();
                    }

                }catch (JSONException e){
                    e.printStackTrace();

                }


            }
        };


        RegisterRequest registerRequest = new RegisterRequest(ulogin,uclave,unombre,uapellido,uemail,"1",ufecha,"U",respoListener);
        RequestQueue queue = Volley.newRequestQueue(RegistrarUsuario.this);
        queue.add(registerRequest);
        Toast.makeText(getApplicationContext(), "REGISTRO EXITOSO", Toast.LENGTH_SHORT).show();

    }



}
