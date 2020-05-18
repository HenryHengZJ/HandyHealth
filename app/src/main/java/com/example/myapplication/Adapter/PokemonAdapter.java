package com.example.myapplication.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.model.Pokemon;
import com.example.myapplication.R;

import java.util.List;


public class PokemonAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<Pokemon> list;

    private static final String TAG = "PokemonAdapter";
    private boolean isLoading;

    public PokemonAdapter(List<Pokemon> list, Context context){
        this.context = context;
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_row, parent, false);
        return new MyJobHolder(view);

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position){

        if (holder instanceof MyJobHolder) {

            final MyJobHolder jobViewHolder = (MyJobHolder) holder;
            // Get the current item from the data set
            Pokemon mylist = list.get(position);
            final String name = mylist.getName();
            final Float accuracy = mylist.getAccuracy();

            if(name!=null && accuracy!=null ) {

                jobViewHolder.mitemName.setText(name);
                jobViewHolder.mitemAccuracy.setText("Probability :" + Math.round(accuracy * 100) + "%");
            }

        }

    }


    @Override
    public int getItemCount(){
        // Count the items
        return list == null ? 0 : list.size();
    }


    private class MyJobHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView mitemName;
        TextView mitemAccuracy;

        public MyJobHolder(View itemView){
            super(itemView);

            mView = itemView;
            mitemName = (TextView) mView.findViewById(R.id.itemName);
            mitemAccuracy = (TextView) mView.findViewById(R.id.itemAccuracy);

        }
    }
}
