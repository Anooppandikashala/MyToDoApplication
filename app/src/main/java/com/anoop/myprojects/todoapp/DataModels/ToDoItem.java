package com.anoop.myprojects.todoapp.DataModels;

public class ToDoItem {
    int id;
    String title, time, date;
    int completed;
    String notes;
    int categoryId;
    int priority; // 0=Low, 1=Medium, 2=High

    public ToDoItem() {
        this.completed  = 0;
        this.id         = 0;
        this.categoryId = 0;
        this.priority   = 0;
    }

    public ToDoItem(String title, String time, String date, int completed) {
        this.title      = title;
        this.time       = time;
        this.date       = date;
        this.completed  = completed;
        this.categoryId = 0;
        this.priority   = 0;
    }

    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }

    public String getTitle()            { return title; }
    public void setTitle(String title)  { this.title = title; }

    public String getTime()             { return time; }
    public void setTime(String time)    { this.time = time; }

    public String getDate()             { return date; }
    public void setDate(String date)    { this.date = date; }

    public int getCompleted()               { return completed; }
    public void setCompleted(int completed) { this.completed = completed; }

    public String getNotes()            { return notes; }
    public void setNotes(String notes)  { this.notes = notes; }

    public int getCategoryId()                  { return categoryId; }
    public void setCategoryId(int categoryId)   { this.categoryId = categoryId; }

    public int getPriority()                { return priority; }
    public void setPriority(int priority)   { this.priority = priority; }
}
