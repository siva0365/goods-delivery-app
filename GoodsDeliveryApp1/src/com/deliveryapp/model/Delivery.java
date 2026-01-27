package com.deliveryapp.model;

import java.time.LocalDate;

public class Delivery {
    public int id;
    public int customerId;
    public String customerName;
    public int productId;
    public String productName;
    public int qtyKg;
    public String address;
    public LocalDate deliveryDate;
    public String status;
}
