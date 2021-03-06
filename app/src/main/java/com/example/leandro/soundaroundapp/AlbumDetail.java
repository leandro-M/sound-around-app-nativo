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

public class AlbumDetail extends AppCompatActivity {
    Button btViewAllMusic, btCancel, btSaveAlbumDetail;
    EditText edNameAlbum, edDescriptionAlbum, edAlbumId;
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
        setContentView(R.layout.activity_album_detail);

        db = new SQLiteHandler(AlbumDetail.this);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        btViewAllMusic = (Button)findViewById(R.id.btnViewAllMusic);
        btCancel = (Button)findViewById(R.id.btnCancel);
        btSaveAlbumDetail = (Button)findViewById(R.id.btnSaveAlbumDetail);
        edNameAlbum = (EditText)findViewById(R.id.edtAlbumName);
        edDescriptionAlbum = (EditText)findViewById(R.id.edtAlbumDescription);
        edAlbumId = (EditText)findViewById(R.id.edtAlbumId);

        Intent myIntent = getIntent(); // gets the previously created intent
        album_id = myIntent.getStringExtra("album_id");


        edAlbumId.setText(album_id);

        chargeElements();
        getAlbumDetail();
    }

    private void chargeElements() {

        btViewAllMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlbumDetail.this, ListMusic.class);
                intent.putExtra("album_id", edAlbumId.getText().toString());
                startActivity(intent);
            }
        });

        btSaveAlbumDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAlbumDetail(edAlbumId.getText());
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getAlbumDetail() {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Buscando informações ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.GET, AppConfig.URL_ALBUMS_VIEW + edAlbumId.getText(), new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                System.out.println("URL PARA O DETALHE DO ALBUM");
                System.out.println(AppConfig.URL_ALBUMS_VIEW + edAlbumId.getText());
                try {
                    JSONObject jObj = new JSONObject(response);
                    edNameAlbum.setText(jObj.getJSONObject("Album").getString("name"));
                    edDescriptionAlbum.setText(jObj.getJSONObject("Album").getString("description"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
            }
        });

        // Adding request to request queue
        LoginController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void saveAlbumDetail(final Editable id) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Atualizando dados ...");
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
                        Toast.makeText(AlbumDetail.this,"Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(AlbumDetail.this, Login.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(AlbumDetail.this, "Erro ao atualizar o album.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(AlbumDetail.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Erro ao atualizar: " + error.getMessage());
                Toast.makeText(AlbumDetail.this,
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

                params.put("data[Album][id]", String.valueOf(id));
                params.put("data[Album][name]", String.valueOf(edNameAlbum.getText()));
                params.put("data[Album][description]", String.valueOf(edDescriptionAlbum.getText()));
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
