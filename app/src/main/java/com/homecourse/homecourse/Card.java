package com.homecourse.homecourse;

public class Card {
    private String firstname;
    private String lastname;
    private String fullname;
    private String bagslot;
    private String offsite;
    private String oncourse;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getBagslot() {
        return bagslot;
    }

    public void setBagslot(String bagslot) {
        this.bagslot = bagslot;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getFullname() {
        return fullname;
    }

    public void setOncourse(String oncourse) {
        this.oncourse = oncourse;
    }

    public String getOncourse() {
        return oncourse;
    }

    public void setOffsite(String offsite) {
        this.oncourse = offsite;
    }

    public String getOffsite() {
        return offsite;
    }

    public Card(){
        //empty constructor needed
    }

    public Card(String firstName, String lastName, String bagSlot){
        this.firstname = firstname;
        this.lastname = lastname;
        this.fullname = fullname;
        this.bagslot = bagslot;
        this.offsite = offsite;
        this.oncourse = oncourse;
    }


}
