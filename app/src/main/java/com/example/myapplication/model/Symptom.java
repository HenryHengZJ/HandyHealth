package com.example.myapplication.model;

/**
 * Created by zhen on 2/18/2017.
 */

public class Symptom {

    private String symptomcategory;
    private String symptom;
    private String symptomtitle;
    private Boolean itemSelected;

    public Symptom() {
    }

    public Symptom(String symptomcategory, String symptomtitle, String symptom, Boolean itemSelected) {
        this.symptomcategory = symptomcategory;
        this.symptomtitle = symptomtitle;
        this.symptom = symptom;
        this.itemSelected = itemSelected;
    }

    public String getSymptomcategory() {return symptomcategory;}

    public void setSymptomcategory(String symptomcategory) {
        this.symptomcategory = symptomcategory;
    }

    public String getSymptomtitle() {return symptomtitle;}

    public void setSymptomtitle(String symptomtitle) {
        this.symptomtitle = symptomtitle;
    }

    public String getSymptom() {return symptom;}

    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }

    public Boolean getItemSelected() {return itemSelected;}

    public void setItemSelected(Boolean itemSelected) {
        this.itemSelected = itemSelected;
    }

}

