package jadelab2;

import java.io.Serializable;

public class Product implements Serializable{
    private String name;
    private int cost;
    private int shippingCost;

    public Product(String name, int cost, int shippingCost) {
        this.name = name;
        this.cost = cost;
        this.shippingCost = shippingCost;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(int shippingCost) {
        this.shippingCost = shippingCost;
    }
}
