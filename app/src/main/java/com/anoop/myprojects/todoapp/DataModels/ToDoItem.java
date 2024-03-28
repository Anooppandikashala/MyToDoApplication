package com.anoop.myprojects.todoapp.DataModels;

public class ToDoItem {
    int id;
    String title,time,date;
    int completed;
    public ToDoItem() {
        this.completed = 0;
    }
    public ToDoItem(int id, String title, String time, String date, int completed) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.date = date;
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }
}
