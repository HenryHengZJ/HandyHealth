package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by zhen on 5/5/2017.
 */

public class AssessmentTab extends Fragment {


    private CardView mcardview;
    private CardView mretryBtn, meventCardView, mhomeCardView, mlifestyleCardView, msportCardView, mtechCardView, mtutorCardView;
    private NestedScrollView mnestedscroll;
    private ImageView meventimg, mhomeimg, mlifestyleimg, msportimg, mtechimg, mtutorimg ;
    private TextView msymptom1txt, msymptom2txt, msymptom3txt, msymptom4txt, msymptom5txt, msymptom6txt ;
    private String[] urls = {
            "https://firebasestorage.googleapis.com/v0/b/tflow-bbd2b.appspot.com/o/eye.jpg?alt=media&token=2bf577ca-0702-4e1c-a204-f2751d6a8a31",
            "https://firebasestorage.googleapis.com/v0/b/tflow-bbd2b.appspot.com/o/skin.jpg?alt=media&token=7a4551be-b7d6-406f-b7d2-0541b708bd20",
            "https://firebasestorage.googleapis.com/v0/b/tflow-bbd2b.appspot.com/o/head.jpg?alt=media&token=32938222-1180-4943-bee6-e24261d16ade",
            "https://firebasestorage.googleapis.com/v0/b/tflow-bbd2b.appspot.com/o/fever.jpg?alt=media&token=41661a39-fa0b-4c73-b398-477c533b6eed",
            "https://firebasestorage.googleapis.com/v0/b/tflow-bbd2b.appspot.com/o/back.jpg?alt=media&token=2ba63b32-4075-4474-b04e-f24255b1d56e",
            "https://firebasestorage.googleapis.com/v0/b/tflow-bbd2b.appspot.com/o/chest.jpg?alt=media&token=ef235886-d14a-433d-b596-619e092e5142"
    };
    private static final String TAG = "AssessmentTab";

    Activity context;
    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_assessment, container, false);

        context = getActivity();

        mcardview = (CardView) rootView.findViewById(R.id.cardview);

        meventimg = (ImageView) rootView.findViewById(R.id.eventimg);
        msportimg = (ImageView) rootView.findViewById(R.id.sportimg);
        mhomeimg = (ImageView) rootView.findViewById(R.id.homeimg);
        mlifestyleimg = (ImageView) rootView.findViewById(R.id.lifestyleimg);
        mtechimg = (ImageView) rootView.findViewById(R.id.techimg);
        mtutorimg = (ImageView) rootView.findViewById(R.id.tutorimg);

        meventCardView = (CardView) rootView.findViewById(R.id.eventCardView);
        msportCardView = (CardView) rootView.findViewById(R.id.sportCardView);
        mhomeCardView = (CardView) rootView.findViewById(R.id.homeCardView);
        mlifestyleCardView = (CardView) rootView.findViewById(R.id.lifestyleCardView);
        mtechCardView = (CardView) rootView.findViewById(R.id.techCardView);
        mtutorCardView = (CardView) rootView.findViewById(R.id.tutorCardView);

        msymptom1txt = (TextView) rootView.findViewById(R.id.symptom1txt);
        msymptom2txt = (TextView) rootView.findViewById(R.id.symptom2txt);
        msymptom3txt = (TextView) rootView.findViewById(R.id.symptom3txt);
        msymptom4txt = (TextView) rootView.findViewById(R.id.symptom4txt);
        msymptom5txt = (TextView) rootView.findViewById(R.id.symptom5txt);
        msymptom6txt = (TextView) rootView.findViewById(R.id.symptom6txt);

        mcardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent speechintent = new Intent(context, SpeechRecognitionActivity.class);
                startActivity(speechintent);
            }
        });

        registerClick();

        loadurl(urls);

        return rootView;
    }

    private void loadurl(String[] imgurl) {
        for(int i=0; i<imgurl.length; i++) {
            loadimg(i,imgurl[i]);
        }
    }

    private void registerClick() {
        CardView[] cardviews = {meventCardView, msportCardView, mhomeCardView, mlifestyleCardView, mtechCardView, mtutorCardView};
        CardView symptomCardView = null;
        TextView[] textViews = { msymptom1txt, msymptom4txt, msymptom2txt, msymptom3txt, msymptom5txt, msymptom6txt};

        for(int i=0; i<cardviews.length; i++) {
            int x = i;
            symptomCardView = cardviews[i];
            symptomCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent searchintent = new Intent(context, SymptomDetail.class);
                    searchintent.putExtra("imgurl", urls[x]);
                    searchintent.putExtra("mainsymptom", textViews[x].getText());
                    startActivity(searchintent);
                }
            });
        }

    }

    private void loadimg(int categoryImgInt, String imgurl) {
        ImageView categoryImgView = null;

        switch (categoryImgInt) {
            case 0:
                categoryImgView = meventimg;
                break;
            case 1:
                categoryImgView = mhomeimg;
                break;
            case 2:
                categoryImgView = mlifestyleimg;
                break;
            case 3:
                categoryImgView = msportimg;
                break;
            case 4:
                categoryImgView = mtechimg;
                break;
            case 5:
                categoryImgView = mtutorimg;
                break;
        }
        if (categoryImgView != null) {
            Log.d(TAG, "imgurl "+imgurl);
            Glide.with(context)
                    .load(imgurl)
                    .centerCrop()
                    .dontAnimate()
                    .into(categoryImgView);
        }
    }
}

