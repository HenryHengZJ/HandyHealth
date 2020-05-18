package com.example.myapplication.Adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.myapplication.R;
import com.example.myapplication.model.SearchWord;


import java.util.List;

/**
 * Created by zhen on 7/10/2017.
 */

public class SearchWordRecyclerAdapter extends RecyclerView.Adapter {

    private List<SearchWord> searchlist;

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;


    private static final String TAG = "SearchWordRecyclerAdapter";

    public SearchWordRecyclerAdapter(List<SearchWord> searchlist, Context context){
        this.mInflater = LayoutInflater.from(context);
        this.searchlist = searchlist;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.searchlist, parent, false);
        return new NormalViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position){

        if (holder instanceof NormalViewHolder) {

            final NormalViewHolder normalViewHolder = (NormalViewHolder) holder;
            // Get the current item from the data set
            SearchWord mylist = searchlist.get(position);

            final String lowertitle = mylist.getTitle();

            normalViewHolder.msearchResult.setText(lowertitle);
        }

    }


    @Override
    public int getItemCount(){
        // Count the items
        return searchlist == null ? 0 : searchlist.size();
    }


    private class NormalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView msearchResult;
        CardView mcardview;

        public NormalViewHolder(View view) {
            super(view);

            msearchResult = (TextView) view.findViewById(R.id.searchResult);
            mcardview = (CardView)view.findViewById(R.id.cardview);
            mcardview.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }


    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }


}
