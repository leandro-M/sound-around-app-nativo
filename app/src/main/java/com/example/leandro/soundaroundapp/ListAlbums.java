package com.example.leandro.soundaroundapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.leandro.soundaroundapp.controllers.LoginController;
import com.example.leandro.soundaroundapp.helper.SQLiteHandler;
import com.example.leandro.soundaroundapp.helper.SessionManager;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;


public class ListAlbums extends Fragment {
    public View view;
    public CustomAdapter customAdapter;
    public ListView listAlbum;

    private SessionManager session;

    public ImageView imageView;
    public TextView name_album;
    public TextView description_album;

    private ProgressDialog pDialog;
    private SQLiteHandler db;

    ArrayList<String> ALBUM_ID = new ArrayList<String>();
    ArrayList<String> IMAGES = new ArrayList<String>();
    ArrayList<String> NAMES = new ArrayList<String>();
    ArrayList<String> DESCRIPTIONS = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // SqLite database handler
        db = new SQLiteHandler(getActivity());
        session = new SessionManager(getContext());

        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        view = inflater.inflate(R.layout.fragment_list_albums, container, false);

        listAlbum = view.findViewById(R.id.list_album);

        customAdapter = new CustomAdapter();

        listAlbum.setAdapter(customAdapter);

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String uid = user.get("uid");

        getAlbums(uid);

        return view;
    }

    public void getAlbums(final String id) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Carregando álbums ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.GET, AppConfig.URL_ALBUMS + id, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONArray jArray = new JSONArray(response);

                    for(int i = 0; i < jArray.length(); i++ ) {

                        JSONObject item = new JSONObject(jArray.get(i).toString());

                        JSONObject album = item.getJSONObject("Album");
                        String album_id = album.getString("id");
                        String name = album.getString("name");
                        String description = album.getString("description");
                        String cover = album.getString("cover");

                        if(cover.isEmpty()) {
                            cover = "http://redesinodal.com.br/portalrede/wp-content/themes/linstar/assets/images/default.jpg";
                        }

                        ALBUM_ID.add(album_id);
                        IMAGES.add(cover);
                        NAMES.add(name);
                        DESCRIPTIONS.add(description);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                customAdapter = new CustomAdapter();

                listAlbum.setAdapter(customAdapter);

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

    class CustomAdapter extends BaseAdapter {

        public void deleteAlbum(final Editable id) {
            // Tag used to cancel the request
            String tag_string_req = "req_login";

            pDialog.setMessage("Deletando album ...");
            showDialog();

            StringRequest strReq = new StringRequest(Request.Method.GET, AppConfig.URL_DELETE_ALBUM + id, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    hideDialog();
                    try {
                        JSONObject item = new JSONObject(response);

                        boolean error = item.getBoolean("error");

                        if (!error) {
                            Toast.makeText(getContext(), "Album deletado!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getContext(), Login.class);
                            startActivity(intent);

                        } else {
                            Toast.makeText(getContext(), "Existe músicas vinculadas!", Toast.LENGTH_SHORT).show();
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
            return IMAGES.size();
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
            view = getLayoutInflater().inflate(R.layout.custom_list_album, null);

            imageView = view.findViewById(R.id.albumCover);
            name_album = view.findViewById(R.id.albumName);
            description_album = view.findViewById(R.id.albumDescription);

            //Picasso.get().load(IMAGES.get(i)).into(imageView);

            name_album.setText(NAMES.get(i));
            description_album.setText(DESCRIPTIONS.get(i));

            Button btTrash = view.findViewById(R.id.btnTrash);
            Button btEdit = view.findViewById(R.id.btnEdit);
            final EditText edIdAlbum = view.findViewById(R.id.edtIdAlbum);

            edIdAlbum.setText(ALBUM_ID.get(i));

            //Abrir detalhe do album
            btEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), AlbumDetail.class);
                    intent.putExtra("album_id", ALBUM_ID.get(i));
                    startActivity(intent);
                }
            });

            //Excluir um album
            btTrash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setTitle("Excluir");
                    builder.setMessage("Deseja realmente excluir?");

                    builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing but close the dialog

                            deleteAlbum(edIdAlbum.getText());

                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Toast.makeText(getContext(), "não vai excluir!", Toast.LENGTH_SHORT).show();
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}