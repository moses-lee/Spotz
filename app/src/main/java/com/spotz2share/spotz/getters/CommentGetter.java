package com.spotz2share.spotz.getters;

/**
 * Created by spotzdevelopment on 11/9/2017.
 */

public class CommentGetter {

    public CommentGetter(){

    }

    public CommentGetter(String userUID, String name, String comment) {
        this.userUID = userUID;
        this.name = name;
        this.comment = comment;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private String userUID, name, comment;

}