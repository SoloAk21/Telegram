package com.soloak.telegramclone.models;


import java.io.Serializable;

public class Contact implements Serializable {
    private String name;
    private String phoneNumber;

    public Contact() {
        // Required default constructor
    }

    public Contact(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
