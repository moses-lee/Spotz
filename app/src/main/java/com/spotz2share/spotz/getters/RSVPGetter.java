package com.spotz2share.spotz.getters;

/**
 * Created by spotzdevelopment on 11/17/2017.
 */

public class RSVPGetter {

    public RSVPGetter(){

    }

    public long getAttending() {
        return attending;
    }

    public void setAttending(long attending) {
        this.attending = attending;
    }

    public RSVPGetter(long attending, String userUID) {
        this.attending = attending;
        this.userUID = userUID;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    private long attending;
    private String userUID;

}