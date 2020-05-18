package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.model.Symptom;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.List;


public class SymptomsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Symptom> list;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_HEADER = 1;

    private static final String TAG = "SymptomsAdapter";
    private boolean isLoading;

    //declare interface
    private OnItemClicked onClick;

    //make interface like this
    public interface OnItemClicked {
        void onItemClick(String action, String text, int position);
    }

    public SymptomsAdapter(List<Symptom> list, Context context){
        this.context = context;
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.symptomsrow, parent, false);
            return new MyHolder(view);
        } else if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.headerrow, parent, false);
            return new MyHeaderHolder(view);
        }
        return null;

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position){

        if (holder instanceof MyHolder) {

            final MyHolder myviewHolder = (MyHolder) holder;
            // Get the current item from the data set
            Symptom mylist = list.get(position);
            final String symptom = mylist.getSymptom();
            final String symptomtitle = mylist.getSymptomtitle();
            final Boolean itemSelected = mylist.getItemSelected();


            //Display expand more button
            if (symptom.contains(" / ")) {
                String firstchar = symptom.split(" / ")[0];
                myviewHolder.mexpandMoreBtn.setVisibility(View.VISIBLE);
                Log.d(TAG, "symptom = " + symptom);
                myviewHolder.msymptomTxt.setText(firstchar);
            }
            else {
                myviewHolder.mexpandMoreBtn.setVisibility(View.GONE);
                myviewHolder.msymptomTxt.setText(symptom);
                Log.d(TAG, "symptom = " + symptom);
            }

            // Display pressed and unpressed color
            if (itemSelected) {
                myviewHolder.msymptomLayout.setBackgroundResource(R.drawable.flexbox_item_pressed_background);
                myviewHolder.msymptomTxt.setTextColor(Color.WHITE);
            }
            else {
                myviewHolder.msymptomLayout.setBackgroundResource(R.drawable.flexbox_item_unpressed_background);
                myviewHolder.msymptomTxt.setTextColor(Color.parseColor("#808080"));
            }

            myviewHolder.msymptomLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String action = null;
                    if (itemSelected) {
                        action = "remove";
                        mylist.setItemSelected(false);
                    }
                    else {
                        action = "add";
                        if (!symptom.contains(" / ")) {
                            mylist.setItemSelected(true);
                        }
                    }
                    onClick.onItemClick(action, symptom, position);
                }
            });

        }
        else if (holder instanceof MyHeaderHolder) {
            MyHeaderHolder headerViewHolder = (MyHeaderHolder) holder;
            Symptom mylist = list.get(position);
            final String header = mylist.getSymptomcategory();
            headerViewHolder.msymptomHeaderTxt.setText(header);
        }

    }

    public void setOnClick(OnItemClicked onClick)
    {
        this.onClick=onClick;
    }

    @Override
    public int getItemViewType(int position) {
        int returnval = 0;

        if (position > 0 && list.get(position - 1).getSymptomcategory().equals(list.get(position).getSymptomcategory())) {
            returnval = VIEW_TYPE_ITEM;
        }
        else {
            returnval = VIEW_TYPE_HEADER;
        }

        return returnval;
      //  return position == 0 ? VIEW_TYPE_HEADER : list.get(position - 1).getSymptomtitle().equals(list.get(position).getSymptomtitle()) ? VIEW_TYPE_ITEM : VIEW_TYPE_HEADER;
    }

    /*@Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_ITEM;
    }*/

    @Override
    public int getItemCount(){
        // Count the items
        return list == null ? 0 : list.size();
    }


    private class MyHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView msymptomTxt;
        ImageView mexpandMoreBtn;
        LinearLayout msymptomLayout;

        public MyHolder(View itemView){
            super(itemView);

            mView = itemView;
            msymptomTxt = (TextView) mView.findViewById(R.id.symptomTxt);
            mexpandMoreBtn = (ImageView) mView.findViewById(R.id.expandMoreBtn);
            msymptomLayout = (LinearLayout) mView.findViewById(R.id.symptomLayout);

        }
    }

    // "Loading item" ViewHolder
    private class MyHeaderHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView msymptomHeaderTxt;

        public MyHeaderHolder(View itemView) {
            super(itemView);

            mView = itemView;
            msymptomHeaderTxt = (TextView) mView.findViewById(R.id.headerTxt);
        }
    }
}
