package com.example.leandro.soundaroundapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.leandro.soundaroundapp.controllers.LoginController;
import com.example.leandro.soundaroundapp.helper.SQLiteHandler;
import com.example.leandro.soundaroundapp.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AlbumNovo extends AppCompatActivity {

    EditText edNameNewAlbum, edDescriptionNewAlbum;
    Button btnSave, btnCancel;

    public String album_id;
    public String strReq;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_novo);

        db = new SQLiteHandler(AlbumNovo.this);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        edNameNewAlbum = (EditText)findViewById(R.id.edtAlbumNewName);
        edDescriptionNewAlbum = (EditText)findViewById(R.id.edtAlbumNewDescription);

        btnSave = (Button)findViewById(R.id.btnSaveNewAlbum);
        btnCancel = (Button)findViewById(R.id.btnCancelNewAlbum);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAlbum();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void saveAlbum() {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Criando album ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_ALBUMS_SAVE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);

                    System.out.println(response);

                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        Toast.makeText(AlbumNovo.this,"Album criado com sucesso!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(AlbumNovo.this, Login.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(AlbumNovo.this, "Erro ao adicionar o album.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(AlbumNovo.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Erro ao adicionar: " + error.getMessage());
                Toast.makeText(AlbumNovo.this,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();

                // Fetching user details from sqlite
                HashMap<String, String> user = db.getUserDetails();

                String uid = user.get("uid");

                params.put("data[Album][name]", String.valueOf(edNameNewAlbum.getText()));
                params.put("data[Album][description]", String.valueOf(edDescriptionNewAlbum.getText()));
                params.put("data[Album][user_id]", uid);

                return params;
            }

        };

        // Adding request to request queue
        LoginController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
