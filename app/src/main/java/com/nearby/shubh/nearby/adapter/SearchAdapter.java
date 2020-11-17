package com.nearby.shubh.nearby.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nearby.shubh.nearby.R;

import java.util.ArrayList;

/**
 * Created by sh on 4/18/2016.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder>{

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<String> mList;
    private ArrayList<String> filteredList;
    private OnMySuggestionListener mOnSuggestionClick;

    public SearchAdapter(Context context, ArrayList<String> list,OnMySuggestionListener suggestionClickListener){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.mList = list;
        this.filteredList = list;
        this.mOnSuggestionClick = suggestionClickListener;
    }

    @Override
    public SearchAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view  = inflater.inflate(R.layout.single_suggestion_search, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    public interface OnMySuggestionListener{
        void onSuggestionClick(String itemText);
    }
    public void filter(String charText){
        charText = charText.toLowerCase();
        filteredList = new ArrayList<>();
        if (charText.length() == 0 ||charText.trim().equals("")) {
            filteredList.addAll(mList);
        }
        else
        {
            for (String s : mList)
            {
                if (s.toLowerCase().contains(charText))
                {

                    filteredList.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final SearchAdapter.MyViewHolder holder, int position) {
        holder.suggestion.setText(filteredList.get(position));
        holder.cardHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnSuggestionClick.onSuggestionClick(holder.suggestion.getText().toString());
            }
        });
    }
    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        LinearLayout cardHolder;
        TextView suggestion;
        public MyViewHolder(View itemView) {
            super(itemView);
            suggestion = (TextView) itemView.findViewById(R.id.suggestion_tv);
            cardHolder = (LinearLayout) itemView.findViewById(R.id.single_search_suggestion);
        }
    }
}
