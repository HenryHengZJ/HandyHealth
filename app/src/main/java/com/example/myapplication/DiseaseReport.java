package com.example.myapplication;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.myapplication.Adapter.DiseaseAdapter;
import com.example.myapplication.Adapter.PokemonAdapter;
import com.example.myapplication.model.Disease;
import com.example.myapplication.model.Pokemon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class DiseaseReport extends AppCompatActivity {

    private RecyclerView mdiseaseRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private DiseaseAdapter recyclerAdapter;
    private List<Disease> diseaselist;

    private static final String TAG = "DiseaseReport";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_report);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setTitle("Diseases Report");

        mdiseaseRecyclerView = (RecyclerView) findViewById(R.id.diseaseRecyclerView);
        mdiseaseRecyclerView.setHasFixedSize(false);

        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setReverseLayout(false);
        mLayoutManager.setStackFromEnd(false);

        diseaselist = new ArrayList<Disease>();
        recyclerAdapter = new DiseaseAdapter(diseaselist,DiseaseReport.this);

        mdiseaseRecyclerView.setLayoutManager(mLayoutManager);
        mdiseaseRecyclerView.setAdapter(recyclerAdapter);

        Intent i = getIntent();
        String response = i.getStringExtra("response");
        String lines[] = response.split("[\r\n]+");

        for(int n = 0; n < lines.length; n++) {
            Log.e(TAG, "lines = "+ lines[n]);

            try {
                JSONObject mainObject = new JSONObject(lines[n]);

                Disease diseases = new Disease();
                diseases.setShortTreatment(mainObject.get("ShortTreatment").toString());
                diseases.setTitle(mainObject.get("Disease").toString());
                diseases.setDescrip(mainObject.get("Descrip").toString());
                diseases.setShortTreatment(mainObject.get("ShortTreatment").toString());
                double probability = (double)mainObject.get("Probability") * 100;
                int intprobability = (int) probability;
                diseases.setProbability(Integer.toString(intprobability));
                if (intprobability > 0) {
                    diseaselist.add(diseases);
                }

                Collections.sort(diseaselist, new Comparator< Disease >() {
                    @Override public int compare(Disease p1, Disease p2) {
                        return Integer.valueOf(p2.getProbability())- Integer.valueOf(p1.getProbability()); // Ascending
                    }
                });

                recyclerAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
