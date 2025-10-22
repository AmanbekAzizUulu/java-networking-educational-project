package com.dandaev.edu.entity;

import java.io.Serializable;

public class Packet implements Serializable {
    public String id;
    public User user;

    // Конструктор для Gson
    public Packet() {}

    public Packet(String id, User user) {
        this.id = id;
        this.user = user;
    }
}
