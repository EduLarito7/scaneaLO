package com.example.scanealo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;




public class RegisterRequest extends StringRequest {



    public static final String REGISTER_REQUEST_URL = "http://192.168.1.3/scaneaLo/Register.php";
    private Map<String,String> params;


    public RegisterRequest(String usuLogin, String usuClave, String usuNombre, String usuApellido,
                           String usuEmail, String usuEstado, String usuFechaCrea , String usuTipo, Response.Listener<String> listener) {
        super(Method.POST,REGISTER_REQUEST_URL,listener,null);



        params = new HashMap<>();
        params.put("usuLogin",usuLogin);
        params.put("usuClave",usuClave);
        params.put("usuNombre",usuNombre);
        params.put("usuApellido",usuApellido);
        params.put("usuEmail",usuEmail);
        params.put("usuEstado",usuEstado);
        params.put("usuFechaCrea",usuFechaCrea);
        params.put("usuTipo",usuTipo);



    }

    @Override
    public Map<String, String> getParams(){
        return params;
    }
}
