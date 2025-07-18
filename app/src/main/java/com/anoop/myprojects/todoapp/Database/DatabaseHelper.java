package com.anoop.myprojects.todoapp.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.anoop.myprojects.todoapp.DataModels.ToDoItem;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static String DATABASE_NAME = "todo_database";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_TODO = "todo_items";
    private static final String CREATE_TABLE_TODO = "CREATE TABLE "
            + TABLE_TODO +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "title TEXT NOT NULL,"+
            "date TEXT NOT NULL,"+
            "time TEXT NOT NULL," +
            "complete INTEGER NOT NULL);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TODO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_TODO + "'");
        onCreate(db);
    }

    public long addToDoItem(ToDoItem toDoItem)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title",toDoItem.getTitle());
        values.put("time",toDoItem.getTime());
        values.put("date",toDoItem.getDate());
        values.put("complete",toDoItem.getCompleted());
        long id = db.insertWithOnConflict(TABLE_TODO,null,values,SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return id;
    }

    public  void deleteToDoItem(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TODO," id= ? ",new String[]{String.valueOf(id)});
        db.close();
    }

    public boolean updateToDoItem(int id, boolean isComplete)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("complete",(isComplete ? 1 : 0));
        long i = db.update(TABLE_TODO,values, " id= ? ",new String[]{String.valueOf(id)});
        db.close();
        return i > 0;
    }

    @SuppressLint("Range")
    public ArrayList<ToDoItem> getAllToDoItems(boolean isCompleted)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<ToDoItem> toDoItems = new ArrayList<>();
        String completed = isCompleted ? " 1 " : " 0 ";
        String selectQuery = "SELECT * FROM "+ TABLE_TODO + " WHERE complete =" + completed +" ORDER BY id DESC" ;
        Cursor c = db.rawQuery(selectQuery,null);
        if(c.moveToFirst())
        {
            do {
                ToDoItem toDoItem = new ToDoItem();
                toDoItem.setId(c.getInt(c.getColumnIndex("id")));
                toDoItem.setTitle(c.getString(c.getColumnIndex("title")));
                toDoItem.setTime(c.getString(c.getColumnIndex("time")));
                toDoItem.setDate(c.getString(c.getColumnIndex("date")));
                toDoItem.setCompleted(c.getInt(c.getColumnIndex("complete")));
                toDoItems.add(toDoItem);

            }
            while (c.moveToNext());
        }
        c.close();
        db.close();
        return toDoItems;
    }

}
