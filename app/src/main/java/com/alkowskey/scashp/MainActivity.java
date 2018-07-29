package com.alkowskey.scashp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.CalendarView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    DatabaseHelper myDb;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;

    List<String> listNames = new ArrayList<>();
    List<String> listBrands = new ArrayList<>();

    private CalendarView calendarView;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        checkCamera();

        myDb = new DatabaseHelper(this);

        calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                i1++;
                String year;
                if(i1>=10)year = i1+"";
                else year = "0"+i1;
                date = i+"-"+year+"-"+i2;
                date = "'"+date+"'";

                getData(date);

            }
        });



        recyclerView = findViewById(R.id.my_recycler_view);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerAdapter(listNames, listBrands);
        recyclerView.setAdapter(adapter);

        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        date = "'"+date+"'";
        getData(date);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            int position = viewHolder.getAdapterPosition();

            String name = listNames.get(position);
            name = "\""+name+"\"";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String date = sdf.format(new Date(calendarView.getDate()));
            date = "'"+date+"'";

            if(myDb.deleteData(name, date))toast("delete successfully");
            else toast("delete wasn't successfully");

            listNames.remove(position);
            listBrands.remove(position);
            adapter.notifyDataSetChanged();

        }
    };


    public void getData(String date) {
        Cursor res = myDb.getAllData(date);

        final int size = listNames.size();

        listBrands.clear();
        listNames.clear();

        adapter.notifyItemRangeChanged(0, size);
        recyclerView.invalidate();

        if(res.getCount()==0) return;

        while(res.moveToNext()) {
            listNames.add(res.getString(1));
            listBrands.add(res.getString(2));

            adapter.notifyDataSetChanged();
            recyclerView.invalidate();

        }

    }

    public void btn_clicked(View view) {

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        Intent intent = integrator.createScanIntent();
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                try {
                    findByBarcode(contents);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == RESULT_CANCELED) {
                toast("scanning canceled");
            }
        }else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    private void findByBarcode(String contents) throws MalformedURLException {
        OkHttpClient okHttpClient = new OkHttpClient();

        String url = "https://mignify.p.mashape.com/gtins/v1.0/productsToGtin?gtin="+contents;

        Request request = new Request.Builder().url(url).addHeader("X-Mashape-Key", "NK9EVcnARjmshbLPdA65pmZAuLQnp1M4KEKjsnawsbYbW7qcxc").addHeader("Accept", "application/json").build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    final String myResponse = response.body().string();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String name = null;
                            String brand = null;
                            try {
                                JSONObject jsonObject = new JSONObject(myResponse);
                                JSONArray jsonArray = jsonObject.getJSONObject("payload").getJSONArray("results");
                                name = jsonArray.getJSONObject(0).getString("productName");
                                brand = jsonArray.getJSONObject(0).getString("brand");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String _date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                            if(name==null||name.equals("null"))
                            {
                                toast("Couldn't find");
                                return;
                            }

                            if(brand==null)brand="null";

                            name = name.replace('"', '\"');
                            if(myDb.insertData(name, brand, _date))
                            {

                                if(date.replace("'","").equals(_date)) {
                                    listNames.add(name);
                                    listBrands.add(brand);


                                    adapter.notifyDataSetChanged();
                                    recyclerView.invalidate();
                                }
                            }
                            else toast("Couldn't insert to database");
                        }
                    });
                }
            }
        });

    }

    private void toast(String string) {
        Toast.makeText(this,string,Toast.LENGTH_LONG).show();
    }

    void checkCamera()
    {
        if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) == PackageManager.PERMISSION_GRANTED
                &&(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED)Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT ).show();
            }
        }
    }
}
