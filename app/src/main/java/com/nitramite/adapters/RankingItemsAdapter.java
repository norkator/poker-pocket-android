package com.nitramite.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nitramite.pokerpocket.R;

import java.util.List;

public class RankingItemsAdapter extends RecyclerView.Adapter<RankingItemsAdapter.MyViewHolder> {

    private List<RankingItem> rankingItemsList;


    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView rankMedalIV;
        TextView playerNameTV, playerXPTV;
        TextView winsCountTV, lossesCountTV;


        MyViewHolder(View view) {
            super(view);
            rankMedalIV = (ImageView) view.findViewById(R.id.rankMedalIV);
            playerNameTV = (TextView) view.findViewById(R.id.playerNameTV);
            playerXPTV = (TextView) view.findViewById(R.id.playerXPTV);
            winsCountTV = (TextView) view.findViewById(R.id.winsCountTV);
            lossesCountTV = (TextView) view.findViewById(R.id.lossesCountTV);
        }
    }

    public RankingItemsAdapter(List<RankingItem> rankingItemsList) {
        this.rankingItemsList = rankingItemsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ranking_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        RankingItem rankingItem = rankingItemsList.get(position);
        holder.rankMedalIV.setBackground(ContextCompat.getDrawable(rankingItem.getContext(), rankingItem.getMedalImageResource()));
        holder.playerNameTV.setText(rankingItem.getPlayerName());
        holder.playerXPTV.setText(rankingItem.getPlayerXP());
        holder.winsCountTV.setText(rankingItem.getWinCount());
        holder.lossesCountTV.setText(rankingItem.getLoseCount());
    }

    @Override
    public int getItemCount() {
        return rankingItemsList.size();
    }

} 