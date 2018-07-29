package com.alkowskey.scashp;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private List<String> names = new ArrayList<>();
    private List<String> brands  = new ArrayList<>();

    public RecyclerAdapter(List<String> names, List<String> brands)
    {
        this.names = names;
        this.brands = brands;
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        public int currentItem;
        public TextView itemTitle;
        public TextView itemDetail;


        public ViewHolder(View itemView) {
            super(itemView);
            itemTitle = (TextView)itemView.findViewById(R.id.item_title);
            itemDetail = (TextView)itemView.findViewById(R.id.item_detail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    int position = getAdapterPosition();
                    //v.setBackgroundColor(Color.parseColor("#FF0000"));
                }
            });
        }

    }
    public void clean()
    {
        final int sizeNames = names.size(), sizeBrands = brands.size();
        names.clear();
        brands.clear();
        notifyItemRangeChanged(0, sizeNames);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.itemTitle.setText(names.get(i));
        viewHolder.itemDetail.setText(brands.get(i));
    }

    @Override
    public int getItemCount() {
        return names.toArray().length;
    }
}
