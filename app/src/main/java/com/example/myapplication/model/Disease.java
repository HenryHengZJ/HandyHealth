package com.example.myapplication.model;

/**
 * Created by zhen on 2/18/2017.
 */

public class Disease {

    private String title;
    private String shortTreatment;
    private String probability;
    private String descrip;

    public Disease() {
    }

    public Disease(String title, String shortTreatment, String probability, String descrip) {
        this.title = title;
        this.shortTreatment = shortTreatment;
        this.probability = probability;
        this.descrip = descrip;
    }

    public String getTitle() {return title;}

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortTreatment() {return shortTreatment;}

    public void setShortTreatment(String shortTreatment) {
        this.shortTreatment = shortTreatment;
    }

    public String getProbability() {return probability;}

    public void setProbability(String probability) {
        this.probability = probability;
    }

    public String getDescrip() {return descrip;}

    public void setDescrip(String descrip) {
        this.descrip = descrip;
    }

}

