package com.anoop.myprojects.todoapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.anoop.myprojects.todoapp.DataModels.Category;
import com.anoop.myprojects.todoapp.DataModels.ToDoItem;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "todo_database";
    private static final int DATABASE_VERSION = 4;
    private static final String TABLE_TODO = "todo_items";
    private static final String TABLE_CATEGORIES = "categories";

    private static final String CREATE_TABLE_TODO =
            "CREATE TABLE " + TABLE_TODO + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "title TEXT NOT NULL," +
            "date TEXT NOT NULL," +
            "time TEXT NOT NULL," +
            "complete INTEGER NOT NULL," +
            "notes TEXT," +
            "category_id INTEGER NOT NULL DEFAULT 0," +
            "priority INTEGER NOT NULL DEFAULT 0);";

    private static final String CREATE_TABLE_CATEGORIES =
            "CREATE TABLE " + TABLE_CATEGORIES + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "color TEXT NOT NULL);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TODO);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        insertDefaultCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_TODO + " ADD COLUMN notes TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_TODO + " ADD COLUMN category_id INTEGER NOT NULL DEFAULT 0");
            db.execSQL(CREATE_TABLE_CATEGORIES);
            insertDefaultCategories(db);
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_TODO + " ADD COLUMN priority INTEGER NOT NULL DEFAULT 1");
        }
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        String[][] defaults = {
                {"Personal", "#4CAF50"},
                {"Work",     "#2196F3"},
                {"Shopping", "#FF9800"},
                {"Health",   "#E91E63"}
        };
        for (String[] cat : defaults) {
            ContentValues cv = new ContentValues();
            cv.put("name",  cat[0]);
            cv.put("color", cat[1]);
            db.insert(TABLE_CATEGORIES, null, cv);
        }
    }

    // ── Categories ─────────────────────────────────────────────────────────────

    public ArrayList<Category> getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Category> list = new ArrayList<>();
        Cursor c = db.query(TABLE_CATEGORIES, null, null, null, null, null, "id ASC");
        if (c.moveToFirst()) {
            do {
                list.add(new Category(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("color"))
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    // ── Tasks ──────────────────────────────────────────────────────────────────

    public long addToDoItem(ToDoItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title",       item.getTitle());
        values.put("time",        item.getTime());
        values.put("date",        item.getDate());
        values.put("complete",    item.getCompleted());
        values.put("notes",       item.getNotes());
        values.put("category_id", item.getCategoryId());
        values.put("priority",    item.getPriority());
        return db.insertWithOnConflict(TABLE_TODO, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public ToDoItem getToDoItemById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ToDoItem item = null;
        Cursor c = db.query(TABLE_TODO, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (c.moveToFirst()) item = cursorToItem(c);
        c.close();
        return item;
    }

    public boolean editToDoItem(int id, String title, String time, String date,
                                String notes, int categoryId, int priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title",       title);
        values.put("time",        time);
        values.put("date",        date);
        values.put("notes",       notes);
        values.put("category_id", categoryId);
        values.put("priority",    priority);
        long rows = db.update(TABLE_TODO, values, "id = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public void deleteToDoItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TODO, "id = ?", new String[]{String.valueOf(id)});
    }

    public boolean updateToDoItem(int id, boolean isComplete) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("complete", isComplete ? 1 : 0);
        long rows = db.update(TABLE_TODO, values, "id = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public ArrayList<ToDoItem> getAllToDoItems(boolean isCompleted) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<ToDoItem> toDoItems = new ArrayList<>();
        Cursor c = db.query(TABLE_TODO, null, "complete = ?",
                new String[]{isCompleted ? "1" : "0"}, null, null, "id DESC");
        if (c.moveToFirst()) {
            do { toDoItems.add(cursorToItem(c)); } while (c.moveToNext());
        }
        c.close();
        return toDoItems;
    }

    private ToDoItem cursorToItem(Cursor c) {
        ToDoItem item = new ToDoItem();
        item.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        item.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
        item.setTime(c.getString(c.getColumnIndexOrThrow("time")));
        item.setDate(c.getString(c.getColumnIndexOrThrow("date")));
        item.setCompleted(c.getInt(c.getColumnIndexOrThrow("complete")));
        int notesIdx = c.getColumnIndex("notes");
        if (notesIdx >= 0) item.setNotes(c.getString(notesIdx));
        int catIdx = c.getColumnIndex("category_id");
        if (catIdx >= 0) item.setCategoryId(c.getInt(catIdx));
        int priorityIdx = c.getColumnIndex("priority");
        if (priorityIdx >= 0) item.setPriority(c.getInt(priorityIdx));
        return item;
    }
}
