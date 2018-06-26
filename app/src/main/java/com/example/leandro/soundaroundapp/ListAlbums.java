package com.example.leandro.soundaroundapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.leandro.soundaroundapp.controllers.LoginController;
import com.example.leandro.soundaroundapp.helper.SQLiteHandler;
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

    public ImageView imageView;
    public TextView name_album;
    public TextView description_album;

    private ProgressDialog pDialog;
    private SQLiteHandler db;

    ArrayList<String> IMAGES = new ArrayList<String>();
    ArrayList<String> NAMES = new ArrayList<String>();
    ArrayList<String> DESCRIPTIONS = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // SqLite database handler
        db = new SQLiteHandler(getActivity());

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
                        String name = album.getString("name");
                        String description = album.getString("description");
                        String cover = album.getString("cover");

                        if(cover.isEmpty()) {
                            cover = "http://redesinodal.com.br/portalrede/wp-content/themes/linstar/assets/images/default.jpg";
                        }
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
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.custom_list_album, null);

            imageView = view.findViewById(R.id.albumCover);
            name_album = view.findViewById(R.id.albumName);
            description_album = view.findViewById(R.id.albumDescription);

            Picasso.get().load(IMAGES.get(i)).into(imageView);

            name_album.setText(NAMES.get(i));
            description_album.setText(DESCRIPTIONS.get(i));

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
