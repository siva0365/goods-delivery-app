package com.deliveryapp.model;

import java.math.BigDecimal;

public class Product {
    public int id;
    public String name;
    public BigDecimal price;

    @Override public String toString() {
        return name + " (€" + price + ")";
    }
}
