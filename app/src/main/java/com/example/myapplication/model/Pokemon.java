package com.example.myapplication.model;

/**
 * Created by zhen on 2/18/2017.
 */

public class Pokemon {

    private String name;
    private Float accuracy;

    public Pokemon() {
    }

    public Pokemon(String name, Float accuracy) {
        this.name = name;
        this.accuracy = accuracy;
    }

    public String getName() {return name;}

    public void setName(String name) {
        this.name = name;
    }

    public Float getAccuracy() {return accuracy;}

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

}

