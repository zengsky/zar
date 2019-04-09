package com.zeng.zar.model;

import com.zeng.zar.core.Model;

public class Fruit extends Model{

    private static final long serialVersionUID = -2778755331348314485L;
    
    private String name;
    
    private Float price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }
    
}
