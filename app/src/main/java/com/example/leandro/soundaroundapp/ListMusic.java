package com.example.leandro.soundaroundapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.view.LayoutInflater;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.leandro.soundaroundapp.controllers.LoginController;
import com.example.leandro.soundaroundapp.helper.SQLiteHandler;
import com.example.leandro.soundaroundapp.helper.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListMusic extends AppCompatActivity {

    public CustomAdapter customAdapter;
    TextView name_music, edtIdMusic, txtAlbumId;
    ListView listMusic;
    Button btNovaMusica;

    LayoutInflater inflater;

    private SessionManager session;

    private ProgressDialog pDialog;
    private SQLiteHandler db;

    ArrayList<String> MUSIC_ID = new ArrayList<String>();
    ArrayList<String> NAMES = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_music);

        getSupportActionBar().hide();

        db = new SQLiteHandler(ListMusic.this);
        session = new SessionManager(ListMusic.this);

        pDialog = new ProgressDialog(ListMusic.this);
        pDialog.setCancelable(false);

        listMusic = (ListView)findViewById(R.id.list_music);
        btNovaMusica = (Button)findViewById(R.id.btnNovaMusica);
        txtAlbumId = (TextView)findViewById(R.id.txtIdAlbum);

        customAdapter = new CustomAdapter();

        listMusic.setAdapter(customAdapter);

        Intent myIntent = getIntent(); // gets the previously created intent
        String album_id = myIntent.getStringExtra("album_id");

        getMusics(album_id);

        txtAlbumId.setText(album_id);

        btNovaMusica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListMusic.this, MusicNova.class);
                intent.putExtra("album_id", txtAlbumId.getText().toString());
                startActivity(intent);
            }
        });
    }

    public void getMusics(final String id) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Carregando músicas ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.GET, AppConfig.URL_MUSICS + id, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONArray jArray = new JSONArray(response);

                    for(int i = 0; i < jArray.length(); i++ ) {

                        JSONObject item = new JSONObject(jArray.get(i).toString());

                        JSONObject album = item.getJSONObject("Sound");


                        MUSIC_ID.add(album.getString("id"));
                        NAMES.add(album.getString("name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                customAdapter = new CustomAdapter();

                listMusic.setAdapter(customAdapter);

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


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private class CustomAdapter extends BaseAdapter {
        public void deleteMusic(final Editable id) {
            // Tag used to cancel the request
            String tag_string_req = "req_login";

            pDialog.setMessage("Deletando album ...");
            showDialog();

            StringRequest strReq = new StringRequest(Request.Method.GET, AppConfig.URL_DELETE_MUSIC + id, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    hideDialog();
                    try {
                        JSONObject item = new JSONObject(response);

                        boolean error = item.getBoolean("error");

                        if (!error) {
                            Toast.makeText(ListMusic.this, "Música deletada!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(ListMusic.this, Login.class);
                            startActivity(intent);

                        } else {
                            Toast.makeText(ListMusic.this, "Existem algo vinculado!", Toast.LENGTH_SHORT).show();
                        }
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

        @Override
        public int getCount() {
            return NAMES.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.activity_custom_list_music, null);

            name_music = (TextView)view.findViewById(R.id.musicName);
            edtIdMusic = (TextView)view.findViewById(R.id.edtIdMusic);
            //Picasso.get().load(IMAGES.get(i)).into(imageView);

            name_music.setText(NAMES.get(i));

            Button btTrash = view.findViewById(R.id.btnDeleteMusic);
            Button btEdit = view.findViewById(R.id.btnEditMusic);

            edtIdMusic.setText(MUSIC_ID.get(i));

            //Abrir detalhe do album
            btEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ListMusic.this, MusicDetail.class);
                    intent.putExtra("music_id", MUSIC_ID.get(i));
                    startActivity(intent);
                }
            });

            //Excluir um album
            btTrash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ListMusic.this);

                    builder.setTitle("Excluir");
                    builder.setMessage("Deseja realmente excluir?");

                    builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing but close the dialog

                            deleteMusic((Editable) edtIdMusic.getText());

                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();

                }
            });

            return view;
        }
    }
}
