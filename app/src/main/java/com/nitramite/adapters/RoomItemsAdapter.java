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

public class RoomItemsAdapter extends RecyclerView.Adapter<RoomItemsAdapter.MyViewHolder> {

    private List<RoomItem> roomItemList;


    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView roomName, roomDescription;


        MyViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            roomName = (TextView) view.findViewById(R.id.roomName);
            roomDescription = (TextView) view.findViewById(R.id.roomDescription);
        }
    }

    public RoomItemsAdapter(List<RoomItem> substanceItems) {
        this.roomItemList = substanceItems;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.room_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        RoomItem roomItem = roomItemList.get(position);
        holder.image.setBackground(ContextCompat.getDrawable(roomItem.getContext(), roomItem.getImageResource()));
        holder.roomName.setText(roomItem.getRoomName());
        holder.roomDescription.setText(roomItem.getRoomDescription());
    }

    @Override
    public int getItemCount() {
        return roomItemList.size();
    }

} 
