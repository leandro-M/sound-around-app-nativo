package com.example.leandro.soundaroundapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class Profile extends Fragment {
    EditText edName, edEmail, edPassword, edBirthdate, edCEP, edStreet, edNeighborhood, edUf, edCity;
    Button btnLogout, btnGetCEP, btnSave, btnCancel;
    View view;

    public String strReq;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private String TAG;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_profile, container, false);

        session = new SessionManager(getContext());
        db = new SQLiteHandler(getContext());

        chargeElements();
        logoutUser();

        getCityByCEP();
        saveRegister();

        return view;
    }

    public void chargeElements() {
        // Progress dialog
        pDialog = new ProgressDialog(getContext());
        pDialog.setCancelable(false);
        // <------------

        // Personal data
        edName = view.findViewById(R.id.edtName);
        edEmail = view.findViewById(R.id.edtEmail);
        edPassword = view.findViewById(R.id.edtPassword);
        edBirthdate = view.findViewById(R.id.edtBirthdate);

        edBirthdate.addTextChangedListener(MaskEditUtil.mask(edBirthdate, MaskEditUtil.FORMAT_DATE));
        // <------------

        // Forget city

        edCEP = view.findViewById(R.id.edtPostalCode);
        edCity = view.findViewById(R.id.edtCity);
        edNeighborhood = view.findViewById(R.id.edtNeighborhood);
        edStreet = view.findViewById(R.id.edtStreet);
        edUf = view.findViewById(R.id.edtState);
        // <-----------

        btnLogout = view.findViewById(R.id.btnLogout);
        btnGetCEP = view.findViewById(R.id.btnSearchCep);
        btnSave = view.findViewById(R.id.btnSaveRegister);
    }

    public void saveRegister() {
       btnSave.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               registerUser();
           }
       });
    }

    public void getCityByCEP() {
        btnGetCEP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectGetByCEP(String.valueOf(edCEP.getText()));
            }
        });
    }

    public void connectGetByCEP(final String cep) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Buscando CEP ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.GET, "https://viacep.com.br/ws/" + cep + "/json/", new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObject = new JSONObject(response);

                    String neighborhood = jObject.getString("bairro");
                    String uf = jObject.getString("uf");
                    String street = jObject.getString("logradouro");
                    String city = jObject.getString("localidade");

                    edCity.setText(city);
                    edNeighborhood.setText(neighborhood);
                    edStreet.setText(street);
                    edUf.setText(uf);

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

    private void registerUser() {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Atualizando dados ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        Toast.makeText(getContext(),"Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getContext(), "Erro ao atualizar o usuário.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Erro ao atualizar: " + error.getMessage());
                Toast.makeText(getContext(),
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

                params.put("data[User][id]", uid);
                params.put("data[User][name]", String.valueOf(edName.getText()));
                params.put("data[User][email]", String.valueOf(edEmail.getText()));

                if(!edPassword.getText().equals("") || !edPassword.getText().toString().isEmpty()) {
                    params.put("data[User][password]", String.valueOf(edPassword.getText()));
                }

                if(!edBirthdate.getText().equals("") || !edBirthdate.getText().toString().isEmpty()) {
                    params.put("data[User][birthdate]", String.valueOf(edBirthdate.getText()));
                }
                params.put("data[Address][street]", String.valueOf(edStreet.getText()));
                params.put("data[Address][city]", String.valueOf(edCity.getText()));
                params.put("data[Address][neighborhood]", String.valueOf(edNeighborhood.getText()));
                params.put("data[Address][state]", String.valueOf(edUf.getText()));

                return params;
            }

        };

        // Adding request to request queue
        LoginController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void logoutUser() {

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setTitle("Sair");
                builder.setMessage("Deseja realmente sair?");

                builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog

                        session.setLogin( false );

                        db.deleteUsers();

                        // Launching the login activity
                        Intent intent = new Intent(getContext(), Login.class);
                        startActivity(intent);
                        getActivity().finish();

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
