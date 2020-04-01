package com.example.projectcovid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private PieChart pieChart;
    private CardView cardView_4, cardView_5;
    private TextView indo_positif, indo_sembuh, indo_meninggal;
    private TextView sulsel_positif, sulsel_meninggal, sulsel_sembuh;
    private Thread th;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        indo_positif = findViewById(R.id.indo_positif);
        indo_sembuh = findViewById(R.id.indo_sembuh);
        indo_meninggal = findViewById(R.id.indo_meninggal);

        sulsel_positif = findViewById(R.id.sulsel_positif);
        sulsel_meninggal = findViewById(R.id.sulsel_meninggal);
        sulsel_sembuh = findViewById(R.id.sulsel_sembuh);

        Toolbar tb = findViewById(R.id.toolbar);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, dd-MM-yyyy");
        tb.setTitle(simpleDateFormat.format(new Date()));
        setSupportActionBar(tb);

        indonesia();
        sulawesi_selatan();
        button();
    }

    private void sulawesi_selatan(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.kawalcorona.com/indonesia/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<List<Post_Attributes>> call = jsonPlaceHolderApi.getAttributes();

        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(MainActivity.this);
        progressDoalog.setMax(100);
        progressDoalog.setMessage("Its loading....");
        progressDoalog.setTitle("ProgressDialog bar example");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // show it
        progressDoalog.show();

        call.enqueue(new Callback<List<Post_Attributes>>() {
            @Override
            public void onResponse(Call<List<Post_Attributes>> call, Response<List<Post_Attributes>> response) {
                List<Post_Attributes> posts = response.body();

                String positif = posts.get(5).getAttributes().getKasus_Posi();
                String meninggal = posts.get(5).getAttributes().getKasus_Meni();
                String sembuh = posts.get(5).getAttributes().getKasus_Semb();

                sulsel_positif.setText(positif+"\nPositif");
                sulsel_meninggal.setText(meninggal+"\nMeninggal");
                sulsel_sembuh.setText(sembuh+"\nSembuh");

                progressDoalog.dismiss();
            }

            @Override
            public void onFailure(Call<List<Post_Attributes>> call, Throwable t) {
                progressDoalog.dismiss();
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void indonesia(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.kawalcorona.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<List<Post>> call = jsonPlaceHolderApi.getPost();

        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(MainActivity.this);
        progressDoalog.setMax(100);
        progressDoalog.setMessage("Its loading....");
        progressDoalog.setTitle("ProgressDialog bar example");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // show it
        progressDoalog.show();

        call.enqueue(new Callback<List<Post>>() {
            List<Post> posts;
            String positif = "";
            String sembuh = "";
            String meninggal = "";
            String positif_2 = "";

            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                posts = response.body();

                for(Post post : posts){
                    positif = post.getPositif();
                    sembuh = post.getSembuh();
                    meninggal = post.getMeninggal();
                }

                char [] posi = positif.toCharArray();

                for(int i = 0 ; i < posi.length; i++){
                    if(posi[i] == ','){
                        continue;
                    }else if(posi[i] == '\n'){

                    }else{
                        positif_2 += posi[i];
                    }
                }

                th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        chart(Integer.valueOf(positif_2), Integer.valueOf(sembuh), Integer.valueOf(meninggal));
                    }
                });

                for(Post post : posts){
                    indo_meninggal.setText(post.getMeninggal()+"\nMeninggal");
                    indo_positif.setText(post.getPositif()+"\nPositif");
                    indo_sembuh.setText(post.getSembuh()+"\nSembuh");
                }

                th.start();

                progressDoalog.dismiss();
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                progressDoalog.dismiss();
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chart(int positif, int sembuh, int meninggal){
        pieChart = findViewById(R.id.chart);

        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(10,10,5,5);

        pieChart.setHoleColor(getResources().getColor(R.color.colorPrimaryDark));

        ArrayList<PieEntry> values = new ArrayList<>();

        values.add(new PieEntry(positif));
        values.add(new PieEntry(sembuh));
        values.add(new PieEntry(meninggal));

        PieDataSet dataSet = new PieDataSet(values, "Covid-19");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(getResources().getColor(R.color.positif), getResources().getColor(R.color.sembuh), getResources().getColor(R.color.meninggal));

        PieData data = new PieData(dataSet);
        data.setValueTextSize(10f);
        data.setValueTextColor(getResources().getColor(R.color.colorPrimaryDark));

        pieChart.notifyDataSetChanged();
        pieChart.setData(data);
    }

    private void button(){
        cardView_4 = findViewById(R.id.cardView4);
        cardView_5 = findViewById(R.id.cardView5);

        cardView_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.worldometers.info/coronavirus/");

                Intent i = new Intent(Intent.ACTION_VIEW, uri);

                startActivity(i);
            }
        });

        cardView_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.worldometers.info/coronavirus/country/indonesia/");

                Intent i = new Intent(Intent.ACTION_VIEW, uri);

                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        long a = 1585756496000l;
        Date date = new Date(a);
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("EEEE, dd/MM/yyyy -- HH:mm:ss a");
        Toast.makeText(this, simpleDateFormat1.format(date), Toast.LENGTH_SHORT).show();

        alert.setTitle("Keluar")
                .setMessage("Yakin ingin keluar?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Keluar dibatalkan", Toast.LENGTH_SHORT).show();
                    }
                }).create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.refresh){
            Toast.makeText(this, "Berhasil", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
