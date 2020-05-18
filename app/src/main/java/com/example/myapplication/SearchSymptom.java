package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.example.myapplication.Adapter.SearchWordRecyclerAdapter;
import com.example.myapplication.model.Disease;
import com.example.myapplication.model.SearchWord;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchSymptom extends AppCompatActivity {

    private RecyclerView msearchRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private SearchWordRecyclerAdapter recyclerAdapter;
    private List<SearchWord> searchlist;
    private ImageButton mbackBtn;

    private static final String TAG = "SearchSymptom";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_bar);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Intent i = getIntent();
        String keyword = i.getStringExtra("keyword");

        msearchRecyclerView = (RecyclerView) findViewById(R.id.searchlist);
        mbackBtn = (ImageButton) findViewById(R.id.backBtn);

        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setReverseLayout(false);
        mLayoutManager.setStackFromEnd(false);

        searchlist = new ArrayList<SearchWord>();
        recyclerAdapter = new SearchWordRecyclerAdapter(searchlist,SearchSymptom.this);

        msearchRecyclerView.setLayoutManager(mLayoutManager);
        msearchRecyclerView.setAdapter(recyclerAdapter);

        mbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        db.collection("SearchSymptom").document(keyword)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                for(int n = 0; n < document.getData().size(); n++) {
                                    SearchWord searchtitles = new SearchWord();
                                    String title = document.getString(String.valueOf(n+1));
                                    Log.d(TAG, title);
                                    searchtitles.setTitle(title);
                                    searchlist.add(searchtitles);
                                }
                                recyclerAdapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
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
