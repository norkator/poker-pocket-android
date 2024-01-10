package com.nitramite.adapters;

import android.content.Context;

public class RoomItem {

    private Context context;
    private Integer imageResource;
    private Integer roomId;
    private String roomName, roomDescription;

    public RoomItem(Context context, Integer imageResource, Integer roomId, String roomName, String roomDescription) {
        this.context = context;
        this.imageResource = imageResource;
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomDescription = roomDescription;
    }

    public Context getContext() {
        return this.context;
    }

    public Integer getImageResource() {
        return imageResource;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getRoomDescription() {
        if (roomDescription != null) {
            return "Minimum bet " + roomDescription;
        } else {
            return "";
        }
    }

} 