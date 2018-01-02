package com.example.user.lectorbarrademo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegistroProd extends AppCompatActivity {
    EditText txtcodigo,txtnombre,txtdesc,txtcant,txtprec;
    Button  btnadd;
    private ProgressBar progressBar2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_prod);
        txtcodigo = (EditText)findViewById(R.id.txtcodp);
        txtnombre = (EditText)findViewById(R.id.txtnombrep);
        txtdesc = (EditText)findViewById(R.id.txtdescp);
        txtcant = (EditText)findViewById(R.id.txtcantp);
        txtprec = (EditText)findViewById(R.id.txtprecp);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        btnadd = (Button) findViewById(R.id.btnadd);

        btnadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agregar_prod();
            }
        });

        Bundle extras = getIntent().getExtras();
        String codigo;

        if (extras != null) {
            codigo = extras.getString("codigo");
            Log.i("COD REG",codigo);
           txtcodigo.setText(codigo);
        }
    }

    private void agregar_prod() {
                btnadd.setVisibility(View.INVISIBLE);
                progressBar2.setVisibility(View.VISIBLE);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("codigo", txtcodigo.getText().toString().trim());
                jsonObject.put("nombre",txtnombre.getText().toString().trim());
                jsonObject.put("desc",txtdesc.getText().toString().trim());
                jsonObject.put("precio",txtprec.getText().toString().trim());
                jsonObject.put("cant",txtcant.getText().toString().trim());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            String insertUrl = "http://joelepping.esy.es/insertProd.php";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    insertUrl, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    try {
                        JSONArray students = response.getJSONArray("mensaje");
                        if (students.isNull(0)) {
                            Toast.makeText(getApplicationContext(),"Error al insertar", Toast.LENGTH_SHORT).show();
                            btnadd.setVisibility(View.VISIBLE);
                            progressBar2.setVisibility(View.INVISIBLE);
                        } else {
                            btnadd.setVisibility(View.VISIBLE);
                            progressBar2.setVisibility(View.INVISIBLE);
                            for (int i = 0; i < students.length(); i++) {
                                JSONObject student = students.getJSONObject(i);
                                Log.i("NOMBRE COD", student.getString("resp"));
                                Toast.makeText(getApplicationContext(),student.getString("resp"),Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(intent);
                            }

                        }
                    }catch(JSONException e){
                        btnadd.setVisibility(View.VISIBLE);
                        progressBar2.setVisibility(View.INVISIBLE);
                        Log.e("VOLLEY ERROR", e.toString());
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    btnadd.setVisibility(View.VISIBLE);
                    progressBar2.setVisibility(View.INVISIBLE);
                    System.out.append(error.getMessage());

                }
            });
            MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);

        }


}
