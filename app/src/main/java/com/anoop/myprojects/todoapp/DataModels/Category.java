package com.anoop.myprojects.todoapp.DataModels;

public class Category {
    public final int id;
    public final String name;
    public final String color; // hex e.g. "#4CAF50"

    public Category(int id, String name, String color) {
        this.id    = id;
        this.name  = name;
        this.color = color;
    }
}
