package com.technoware.machinetest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    private OkHttpClient client;
    SwipeRefreshLayout swipeRefreshLayout;
    List<productmodel> productlist;
    RecyclerView recyclerView;
    ProductAdapter adapter;
    ProgressDialog pb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle(getResources().getString(R.string.productlisttitle));

        pb = new ProgressDialog(this);
        pb.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pb.setMessage("Loading...");

        client = new OkHttpClient();
        productlist = new ArrayList<>();

        recyclerView = findViewById(R.id.wvv);
        adapter = new ProductAdapter(MainActivity.this, productlist);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(MainActivity.this, 2);
        recyclerView.setHasFixedSize(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(true);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checknetworkthencontinue() ;
            }
        });


        checknetworkthencontinue() ;


    }

    private void checknetworkthencontinue() {
        if(isNetworkAvailable()){
            new fetchallproducts().execute();
        }else{
            final Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.nonetworkdialog);

            TextView retry = (TextView) dialog.findViewById(R.id.refresh);
            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    checknetworkthencontinue();
                }
            });

            dialog.show();
        }

    }


    public class fetchallproducts extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {
            pb.show();

        }

        protected String doInBackground(String... arg0) {
            String result;
            try {
                result = fetchallproductsdata();
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(final String result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //  Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                    String productdata = null;
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        productdata = jsonObject.getString("products");


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    List<productmodel> cc = new Gson().fromJson(productdata.toString(), new TypeToken<List<productmodel>>() {
                    }.getType());
                    productlist.clear();
                    productlist.addAll(cc);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);

                    pb.dismiss();


                }
            });
            super.onPostExecute(result);

        }
    }

    public String fetchallproductsdata() {

        try {
            Request request = new Request.Builder()
                    .url("https://run.mocky.io/v3/bd26945d-228e-45b4-94a3-04c74f085e40")
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            int code = response.code();
            String result = response.body().string();
            response.close();
            return result;
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }


    public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyViewHolder> {
        private Context context;
        private List<productmodel> plists;
        OkHttpClient client;
        ProgressDialog pb;


        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView Title, Price;
            ImageView img;


            public MyViewHolder(View view) {
                super(view);

                Title = view.findViewById(R.id.title);
                Price = view.findViewById(R.id.price);
                img = view.findViewById(R.id.imageView);

            }
        }

        public ProductAdapter(Context context, List<productmodel> movieList) {
            this.context = context;
            this.plists = movieList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.productitem, parent, false);


            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final productmodel mm = plists.get(position);


            holder.Title.setText(mm.getTitle());
            holder.Price.setText("Rs . " + mm.getPrice());
            Glide.with(context)
                    .load(mm.getImageUrl())
                    .into(holder.img);


        }

        @Override
        public int getItemCount() {
            return plists.size();
        }


    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this activity?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {

        super.onRestart();
    }

    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}