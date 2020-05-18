package com.example.myapplication;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Adapter.DiseaseAdapter;
import com.example.myapplication.Adapter.SymptomsAdapter;
import com.example.myapplication.model.Disease;
import com.example.myapplication.model.Symptom;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SymptomDetail extends AppCompatActivity implements SymptomsAdapter.OnItemClicked {

    private RecyclerView msymptomsRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private SymptomsAdapter recyclerAdapter;
    private List<Symptom> symptomlist;
    private ImageView mpostImage;
    private TextView msymptomTitle;
    private LinearLayout mproceedLay;
    private BottomSheetBehavior sheetBehavior;

    private Map<String, Object> docData = new HashMap<>();

    private static final String TAG = "SymptomDetail";

    @Override
    public void onItemClick(String action, String text, int position) {
        Log.d(TAG, "action : " + action);
        Log.d(TAG, "Selected : " + text);
        //Add data
        if (action.equals("add")) {
            //Add multiple selections
            if (text.contains(" / ")) {
                String otheroptions[] = text.split(" / ");
                configureBottomSheetBehavior(otheroptions, position);
            }
            else {
                docData.put(symptomlist.get(position).getSymptomtitle(), 1);
            }
        }
        //Remove data
        else if (action.equals("remove")) {
            //Remove multiple selections
            if(symptomlist.get(position).getSymptomtitle().contains(" / ")) {
                symptomlist.get(position).setSymptom(symptomlist.get(position).getSymptomtitle());
                docData.remove(text.split("\\(")[1].replace(")", ""));
            }
            else {
                docData.remove(symptomlist.get(position).getSymptomtitle());
            }
        }
        recyclerAdapter.notifyDataSetChanged();
        checkDocDataSize();

    }

    private void  checkDocDataSize() {
        if (docData.size() >= 5) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        else {
            sheetBehavior.setHideable(true);
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void configureBottomSheetBehavior(String[] otheroptions, int position) {

        View view = getLayoutInflater().inflate(R.layout.bottomsheet, null);

        BottomSheetDialog dialog = new BottomSheetDialog(this);

        LinearLayout mbottomLayout = (LinearLayout) view.findViewById(R.id.bottomLayout);

        String firstchar = otheroptions[0];

        for (int i = 0; i < otheroptions.length; ++ i) {
            if (i > 0) {
                addButtons(firstchar, otheroptions[i], mbottomLayout, dialog, position);
            }
        }
        dialog.setContentView(view);
        dialog.show();
    }

    private void addButtons(String firstchar, String btnName, LinearLayout mbottomLayout, BottomSheetDialog dialog, int position) {

        Log.d(TAG, "btnName = " + btnName);

        Button b = new Button(this);
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        rl.addRule(RelativeLayout.ALIGN_TOP);
        b.setLayoutParams(rl);
        b.setPadding(20, 20, 20, 20);
        b.setText(btnName);
        b.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float[] outerRadii = new float[8];
            Arrays.fill(outerRadii, 10 / 2);

            RoundRectShape shape = new RoundRectShape(outerRadii, null, null);
            ShapeDrawable mask = new ShapeDrawable(shape);

            ColorStateList stateList = ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));

            b.setBackground(new RippleDrawable(stateList, getResources().getDrawable(R.drawable.circle_transparent_btn), mask));

        } else {
            b.setBackgroundColor(getResources().getColor(R.color.white));
        }

        (mbottomLayout).addView(b);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "b = " + b.getText());
                String btnText = b.getText().toString();
                dialog.dismiss();
                docData.put(btnText, 1);
                symptomlist.get(position).setItemSelected(true);
                symptomlist.get(position).setSymptomtitle(symptomlist.get(position).getSymptom());
                symptomlist.get(position).setSymptom(firstchar + " (" + btnText.substring(0,1).toUpperCase() + btnText.substring(1) + ")");
                recyclerAdapter.notifyDataSetChanged();

                checkDocDataSize();
            }
        });

    }

    private void proceedClicked() {
        Log.d(TAG, "docData = " + docData);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_detail);

        Intent i = getIntent();
        String imgurl = i.getStringExtra("imgurl");
        String mainsymptom = i.getStringExtra("mainsymptom");

        Toolbar mToolbar = (Toolbar)findViewById(R.id.toolbar_other);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setTitle(" ");
        mToolbar.setSubtitle(" ");

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_other);
        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar_toolbar_other);
        collapsingToolbarLayout.setTitle(" ");

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(mainsymptom + " Symptoms");
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });


        mproceedLay = (LinearLayout) findViewById(R.id.proceedLay);
        mproceedLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                proceedClicked();
            }
        });
        // init the bottom sheet behavior
        sheetBehavior = BottomSheetBehavior.from(mproceedLay);
        sheetBehavior.setHideable(true);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // Get the RecyclerView object.
        msymptomsRecyclerView = (RecyclerView)findViewById(R.id.symptomsRecyclerView);
        msymptomsRecyclerView.setHasFixedSize(false);
        msymptomsRecyclerView.setNestedScrollingEnabled(false);

        // Create the FlexboxLayoutMananger, only flexbox library version 0.3.0 or higher support.
        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(getApplicationContext());
        // Set flex direction.
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
        // Set JustifyContent.
        flexboxLayoutManager.setJustifyContent(JustifyContent.FLEX_START);

        symptomlist = new ArrayList<Symptom>();
        recyclerAdapter = new SymptomsAdapter(symptomlist,SymptomDetail.this);

        msymptomsRecyclerView.setLayoutManager(flexboxLayoutManager);
        msymptomsRecyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.setOnClick(SymptomDetail.this);


        mpostImage = (ImageView)findViewById(R.id.postImage);
        Glide.with(SymptomDetail.this)
                .load(imgurl)
                .centerCrop()
                .dontAnimate()
                .into(mpostImage);

        msymptomTitle = (TextView)findViewById(R.id.symptomTitle);
        msymptomTitle.setText(mainsymptom + " Symptoms");


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("symptoms").document("eye_symptoms");

        /*docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        JSONObject response = new JSONObject(document.getData());
                        JSONArray keys = response.names ();

                        for (int i = 0; i < keys.length (); ++i) {

                            String key = null; // Here's your key
                            try {
                                key = keys.getString(i);
                                Log.d(TAG, "DocumentSnapshot key: " + key);
                                if (!key.equals("Question")) {
                                    Symptom symptoms = new Symptom();
                                    symptoms.setSymptomtitle(key);
                                    symptoms.setSymptom(null);
                                    symptoms.setItemSelected(false);
                                    symptomlist.add(symptoms);

                                    for (int n = 0; n < response.getJSONArray(key).length(); ++n) {
                                        Log.d(TAG, "DocumentSnapshot value: " + response.getJSONArray(key).get(n));
                                        Symptom symptoms1 = new Symptom();
                                        symptoms1.setSymptomtitle(key);
                                        symptoms1.setSymptom(response.getJSONArray(key).get(n).toString());
                                        symptoms1.setItemSelected(false);
                                        symptomlist.add(symptoms1);
                                    }
                                    Log.d(TAG, "symptomlist : " + symptomlist);
                                    recyclerAdapter.notifyDataSetChanged();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });*/


        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        JSONObject response = new JSONObject(document.getData());
                        JSONArray keys = response.names ();

                        for (int i = 0; i < keys.length (); ++i) {

                            String key = null; // Here's your key
                            try {
                                key = keys.getString(i);
                                Log.d(TAG, "DocumentSnapshot key: " + key);
                                if (!key.equals("Question")) {
                                    Symptom symptoms = new Symptom();
                                    symptoms.setSymptomcategory(key);
                                    symptoms.setSymptomtitle(null);
                                    symptoms.setSymptom(null);
                                    symptoms.setItemSelected(false);
                                    symptomlist.add(symptoms);

                                    JSONObject response2 = new JSONObject(response.get(key).toString());
                                    JSONArray keys2 = response2.names ();

                                    for (int n = 0; n < keys2.length(); ++n) {
                                        Log.d(TAG, "DocumentSnapshot value: " + response2.getString(keys2.getString(n)));
                                        Symptom symptoms1 = new Symptom();
                                        symptoms1.setSymptomcategory(key);
                                        symptoms1.setSymptomtitle(keys2.getString(n));
                                        symptoms1.setSymptom(response2.getString(keys2.getString(n)));
                                        symptoms1.setItemSelected(false);
                                        symptomlist.add(symptoms1);
                                    }
                                    Log.d(TAG, "symptomlist : " + symptomlist);

                                }
                                recyclerAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
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
