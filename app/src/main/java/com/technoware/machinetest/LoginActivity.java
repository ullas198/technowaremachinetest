package com.technoware.machinetest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    EditText Emailed, Passworded;
    Button Signin;
    OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.primarycolor));

        client = new OkHttpClient();

        Emailed = findViewById(R.id.emailed);
        Passworded = findViewById(R.id.passworded);
        Signin = findViewById(R.id.signinbt);

        Signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailvalue = Emailed.getText().toString();
                String passwordvalue = Passworded.getText().toString();

                if (TextUtils.isEmpty(emailvalue)) {
                    Emailed.setError("Value needed");
                    Emailed.requestFocus();
                } else if (TextUtils.isEmpty(passwordvalue)) {
                    Passworded.setError("Value needed");
                    Passworded.requestFocus();
                } else {
                    checknetworkthencontinue(emailvalue, passwordvalue);
                }


            }
        });


    }


    private void checknetworkthencontinue(String emailvalue, String passwordvalue) {
        if(isNetworkAvailable()){
            new loginapicall().execute(emailvalue,passwordvalue);
        }else{
            final Dialog dialog = new Dialog(LoginActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.nonetworkdialog);

            TextView retry = (TextView) dialog.findViewById(R.id.refresh);
            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    checknetworkthencontinue(emailvalue,passwordvalue);
                }
            });

            dialog.show();
        }

    }


    public class loginapicall extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {
            String result;
            String emailv = arg0[0];
            String passv = arg0[1];

            try {
                result = loginapicalldata(emailv, passv);
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(final String result) {
           // Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Boolean status = null;
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        status = Boolean.valueOf(jsonObject.optString("Status"));

                        if (status) {
                            String username = jsonObject.optString("UserName");
                            Intent in = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(in);
                            finish();
                            Toast.makeText(getApplicationContext(), "Login Success , Logined as " + username, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Invalid Credentials", Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });
            super.onPostExecute(result);
        }
    }

    public String loginapicalldata(String email, String pass) {


        String passs = "MTIzNDU=" ;
        byte[] data = new byte[0];
        try {
            data = passs.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String Passwordbase64 = Base64.encodeToString(data, Base64.DEFAULT);
        Log.i("qwerty" , Passwordbase64);

        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("UserID", email)
                    .add("Password", pass)
                    .build();
            Request request = new Request.Builder()
                    .url("http://54.251.37.65/TESTAPI/api/Users/UserAuthentication")
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}