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

public class MusicNova extends AppCompatActivity {

    Button btCancel, btSaveNovaMusica;
    EditText edNameMusica, edAlbumId, edViews;
    public View view;

    public String album_id;
    public String strReq;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_nova);

        db = new SQLiteHandler(MusicNova.this);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        btCancel = (Button)findViewById(R.id.btnCancelNovaMusicaNova);
        btSaveNovaMusica = (Button)findViewById(R.id.btnSaveNovaMusicaNova);
        edNameMusica = (EditText)findViewById(R.id.edtNomeMusicaNova);
        edAlbumId = (EditText)findViewById(R.id.edtAlbumIdNovaMusica);

        Intent myIntent = getIntent(); // gets the previously created intent
        String album_id = myIntent.getStringExtra("album_id");
        edAlbumId.setText(album_id);

        chargeElements();
    }

    private void chargeElements() {

        btSaveNovaMusica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMusicDetail(edAlbumId.getText());
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void saveMusicDetail(final Editable id) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Cadastrando dados ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_MUSIC_SAVE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);

                    System.out.println(response);

                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        Toast.makeText(MusicNova.this,"Dados cadastrado com sucesso!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MusicNova.this, Login.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(MusicNova.this, "Erro ao cadastrar a música.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(MusicNova.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Erro ao cadastrar: " + error.getMessage());
                Toast.makeText(MusicNova.this,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();

                params.put("data[AlbumsHasSound][0][album_id]", String.valueOf(id));
                params.put("data[Sound][name]", String.valueOf(edNameMusica.getText()));

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
