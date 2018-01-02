package com.example.user.lectorbarrademo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView zXingScannerView;
    Button btn_consultar,btn_scan;
    TextView txt_prod;
    private ProgressBar progressBar;
    private AlertDialog enableNotificationListenerAlertDialog;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private String codigo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_consultar = (Button) findViewById(R.id.btnprod);
        btn_scan = (Button) findViewById(R.id.btn_scan);
        txt_prod = (TextView) findViewById(R.id.txtproductos);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btn_consultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listar_producto();
            }
        });
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
               scan();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void scan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_CAMERA_REQUEST_CODE);
            }
        }
        zXingScannerView = new ZXingScannerView(getApplicationContext());
        setContentView(zXingScannerView);
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (zXingScannerView != null)
            zXingScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        setContentView(R.layout.activity_main);
        Toast.makeText(getApplicationContext(), result.getText(), Toast.LENGTH_SHORT).show();
         codigo = result.getText();
        Log.i("CODIGO HANDLE", codigo);
        consulta_producto(codigo);
        zXingScannerView.resumeCameraPreview(this);

    }


    private void consulta_producto(String codigo) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("codigo", codigo);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String insertUrl = "http://joelepping.esy.es/exist_barra.php";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                insertUrl, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray students = response.getJSONArray("usuario");
                    if (students.isNull(0)) {
                        enableNotificationListenerAlertDialog = alerta_producto_vacio();
                        enableNotificationListenerAlertDialog.show();
                    } else {
                        for (int i = 0; i < students.length(); i++) {
                            JSONObject student = students.getJSONObject(i);
                            Log.i("NOMBRE COD", student.getString("nombre"));
                            String prod = "Descipcion del Producto \ncodigo: " + student.getString("codigo") + "\n nombre: " + student.getString("nombre") + "\n descr: " + student.getString("descripcion") + " \nPrecio:" + student.getString("precio") + " \nCantidad:" + student.getString("cantidad") + "\n-------------------------------------------------\n";
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                            builder1.setMessage(prod);
                            builder1.setCancelable(false);

                            builder1.setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            Intent i = new Intent(getApplicationContext(),MainActivity.class);
                                            startActivity(i);
                                        }
                                    });

                            AlertDialog alert11 = builder1.create();
                            alert11.show();

                        }

                    }
                }catch(JSONException e){
                    Log.e("VOLLEY ERROR", e.toString());
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.append(error.getMessage());

            }
        });
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);

    }

    private void listar_producto() {
        txt_prod.setText("");
        btn_consultar.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("codigo", "");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String insertUrl = "http://joelepping.esy.es/list_productos.php";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                insertUrl, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    btn_consultar.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    JSONArray students = response.getJSONArray("usuario");
                    for (int i = 0; i < students.length(); i++) {
                        JSONObject student = students.getJSONObject(i);
                        //Log.i("LISTA PRODUCTOS,", student.getString("codigo") + "\t" + student.getString("nombre") + "\t" + student.getString("descripcion") + "\t" + student.getString("precio") + "\n");
                        String prod = "codigo: " + student.getString("codigo") + "\n nombre: " + student.getString("nombre") + "\n descr: " + student.getString("descripcion") + " \nPrecio:" + student.getString("precio") + " \nCantidad:" + student.getString("cantidad") + "\n-------------------------------------------------\n";
                        txt_prod.append(prod);


                    }


                } catch (JSONException e) {
                    btn_consultar.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    Log.e("VOLLEY ERROR", e.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                btn_consultar.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                System.out.append(error.getMessage());

            }
        });
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);

    }


    private AlertDialog alerta_producto_vacio() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Atención");
        alertDialogBuilder.setMessage("El producto no se encuentra en stock ");
        alertDialogBuilder.setPositiveButton("Agregar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(getApplicationContext(),RegistroProd.class);
                        i.putExtra("codigo",codigo);

                        startActivity(i);
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getApplicationContext(), "opcion cancelar", Toast.LENGTH_SHORT).show();
                    }
                });
        return (alertDialogBuilder.create());

    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                scan();

            } else {

                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();

            }

        }
    }
}