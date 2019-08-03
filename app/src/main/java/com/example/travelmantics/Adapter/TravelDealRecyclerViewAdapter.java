package com.example.travelmantics.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelmantics.Activities.NewTravelDealActivity;
import com.example.travelmantics.Model.TravelDeal;
import com.example.travelmantics.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TravelDealRecyclerViewAdapter extends RecyclerView.Adapter<TravelDealRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<TravelDeal> travelDealList;

    public TravelDealRecyclerViewAdapter(Context context, List<TravelDeal> travelDealList) {
        this.context = context;
        this.travelDealList = travelDealList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.travel_deal_layout, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TravelDeal travelDeal = travelDealList.get(position);
        String imageUrl = null;

        holder.dealName.setText(travelDeal.getTitle());
        holder.dealDescription.setText(travelDeal.getDescription());
        holder.dealPrice.setText(travelDeal.getPrice());

        Picasso.get()
                .load(travelDeal.getImageUrl())
                .into(holder.dealImage);

    }

    @Override
    public int getItemCount() {
        return travelDealList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView dealImage;
        public TextView dealName;
        public TextView dealDescription;
        public TextView dealPrice;
        private Context context;
        String userID;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            dealImage = (ImageView) itemView.findViewById(R.id.deal_image);
            dealName = (TextView) itemView.findViewById(R.id.deal_name);
            dealDescription = (TextView) itemView.findViewById(R.id.deal_description);
            dealPrice = (TextView) itemView.findViewById(R.id.deal_price);
            this.context = context;
            userID = null;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            TravelDeal travelDeal = travelDealList.get(getAdapterPosition());
            Intent intent = new Intent(context, NewTravelDealActivity.class);
            intent.putExtra("TravelDeal", travelDeal);
            context.startActivity(intent);
        }
    }
}
