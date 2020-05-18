package com.example.myapplication.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.model.Disease;
import com.example.myapplication.model.Pokemon;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.List;


public class DiseaseAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<Disease> list;

    private static final String TAG = "DiseaseAdapter";
    private boolean isLoading;

    public DiseaseAdapter(List<Disease> list, Context context){
        this.context = context;
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.diseaserow, parent, false);
        return new MyHolder(view);

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position){

        if (holder instanceof MyHolder) {

            final MyHolder myviewHolder = (MyHolder) holder;
            // Get the current item from the data set
            Disease mylist = list.get(position);
            final String title = mylist.getTitle();
            final String descrip = mylist.getDescrip();
            final String probability = mylist.getProbability();
            final String shortTreatment = mylist.getShortTreatment();

            if(title!=null && descrip!=null && probability!=null && shortTreatment!=null ) {

                myviewHolder.mdiseaseName.setText(title);
                myviewHolder.mprobabilityTxt.setText(probability + "%");
                myviewHolder.mdiseaseShortTreatment.setText(shortTreatment);
                myviewHolder.mdiseaseShortDescrip.setText(descrip);
                myviewHolder.mCircularProgressBar.setProgress(Float.parseFloat(probability));

                //Set short treatment text color
                if (shortTreatment.contains("treated at home")) {
                    myviewHolder.mdiseaseShortTreatment.setTextColor(ContextCompat.getColor(context, R.color.green));
                }
                else if (shortTreatment.contains("pharmacist help")) {
                    myviewHolder.mdiseaseShortTreatment.setTextColor(ContextCompat.getColor(context, R.color.orange));
                }
                else {
                    myviewHolder.mdiseaseShortTreatment.setTextColor(ContextCompat.getColor(context, R.color.red));
                }

                //Set progressbar color
                if (Integer.valueOf(probability) >= 60 && Integer.valueOf(probability) <= 100) {
                    myviewHolder.mprobabilityTxt.setTextColor(ContextCompat.getColor(context, R.color.green));
                    myviewHolder.mCircularProgressBar.setColor(ContextCompat.getColor(context, R.color.green));
                    myviewHolder.mCircularProgressBar.setBackgroundColor(ContextCompat.getColor(context, R.color.lightgreen));
                }
                else if (Integer.valueOf(probability) >= 30 && Integer.valueOf(probability) < 60) {
                    myviewHolder.mprobabilityTxt.setTextColor(ContextCompat.getColor(context, R.color.orange));
                    myviewHolder.mCircularProgressBar.setColor(ContextCompat.getColor(context, R.color.orange));
                    myviewHolder.mCircularProgressBar.setBackgroundColor(ContextCompat.getColor(context, R.color.lightorange));
                }
                else if (Integer.valueOf(probability) >= 0 && Integer.valueOf(probability) < 30) {
                    myviewHolder.mprobabilityTxt.setTextColor(ContextCompat.getColor(context, R.color.red));
                    myviewHolder.mCircularProgressBar.setColor(ContextCompat.getColor(context, R.color.red));
                    myviewHolder.mCircularProgressBar.setBackgroundColor(ContextCompat.getColor(context, R.color.lightred));
                }
            }

        }

    }


    @Override
    public int getItemCount(){
        // Count the items
        return list == null ? 0 : list.size();
    }


    private class MyHolder extends RecyclerView.ViewHolder{

        View mView;
        CircularProgressBar mCircularProgressBar;
        TextView mprobabilityTxt;
        TextView mdiseaseName;
        TextView mdiseaseShortTreatment;
        TextView mdiseaseShortDescrip;

        public MyHolder(View itemView){
            super(itemView);

            mView = itemView;
            mCircularProgressBar = (CircularProgressBar) mView.findViewById(R.id.progressBar);
            mdiseaseName = (TextView) mView.findViewById(R.id.diseaseName);
            mprobabilityTxt = (TextView) mView.findViewById(R.id.probabilityTxt);
            mdiseaseShortTreatment = (TextView) mView.findViewById(R.id.diseaseShortTreatment);
            mdiseaseShortDescrip = (TextView) mView.findViewById(R.id.diseaseShortDescrip);

        }
    }
}
