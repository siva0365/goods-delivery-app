package com.deliveryapp.model;

public class User {
    public int id;
    public String email;
    public String passwordHash;
    public String phone;
    public Role role;
    public String firstName;
    public String lastName;
    public String truckNumber;     // nullable
    public Integer truckCapacityKg; // nullable
}
