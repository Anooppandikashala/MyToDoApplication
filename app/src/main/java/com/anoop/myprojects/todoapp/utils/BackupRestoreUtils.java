package com.anoop.myprojects.todoapp.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupRestoreUtils {
    private static final String DB_NAME = "todo_database";
    private static final String BACKUP_DIR = "backups";
    private static final int MAX_BACKUPS = 5;

    public static void backupDatabase(Context context) {
        try {
            File dbFile = context.getDatabasePath(DB_NAME);
            File backupFolder = new File(context.getExternalFilesDir(null), BACKUP_DIR);
            if (!backupFolder.exists()) backupFolder.mkdirs();

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
            File backupFile = new File(backupFolder, "todo_backup_" + timestamp + ".db");

            try (
                FileInputStream fis = new FileInputStream(dbFile);
                FileOutputStream fos = new FileOutputStream(backupFile);
                FileChannel src = fis.getChannel();
                FileChannel dst = fos.getChannel()
            ) {
                dst.transferFrom(src, 0, src.size());
            }

            cleanupOldBackups(backupFolder);
            Toast.makeText(context, "Backup created: " + backupFile.getName(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show();
        }
    }

    public static void restoreDatabase(Context context, File selectedBackup) {
        Log.d("BackupRestoreUtils", "restoreDatabase: ");
        try {
            File dbFile = context.getDatabasePath(DB_NAME);
            try (
                FileInputStream fis = new FileInputStream(selectedBackup);
                FileOutputStream fos = new FileOutputStream(dbFile);
                FileChannel src = fis.getChannel();
                FileChannel dst = fos.getChannel()
            )
            {
                dst.transferFrom(src, 0, src.size());
            }
            Toast.makeText(context, "Restored: " + selectedBackup.getName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Restore failed", Toast.LENGTH_SHORT).show();
        }
    }

    public static List<File> getBackupFiles(Context context) {
        File backupFolder = new File(context.getExternalFilesDir(null), BACKUP_DIR);
        File[] files = backupFolder.listFiles((dir, name) -> name.endsWith(".db"));
        if (files == null) return new ArrayList<>();

        List<File> backups = Arrays.asList(files);
        backups.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified())); // newest first
        return backups;
    }

    private static void cleanupOldBackups(File backupFolder) {
        File[] files = backupFolder.listFiles((dir, name) -> name.endsWith(".db"));
        if (files == null || files.length <= MAX_BACKUPS) return;

        Arrays.sort(files, Comparator.comparingLong(File::lastModified)); // oldest first
        for (int i = 0; i < files.length - MAX_BACKUPS; i++) {
            files[i].delete();
        }
    }
}

















//package com.anoop.myprojects.todoapp.utils;
//
//import android.content.Context;
//import android.os.Environment;
//import android.widget.Toast;
//
//import com.anoop.myprojects.todoapp.DataModels.ToDoItem;
//import com.anoop.myprojects.todoapp.Database.DatabaseHelper;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//
//import java.io.*;
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//
//public class BackupRestoreUtils {
//
//    private static final String BACKUP_FILE_NAME = "todo_backup.json";
//
//    public static void backupToJson(Context context) {
//        DatabaseHelper db = new DatabaseHelper(context);
//        ArrayList<ToDoItem> items = db.getAllToDoItems(true);
//        items.addAll(db.getAllToDoItems(false));
//
//        Gson gson = new Gson();
//        String jsonString = gson.toJson(items);
//
//        try {
//            File file = new File(context.getExternalFilesDir(null), BACKUP_FILE_NAME);
//            FileWriter writer = new FileWriter(file);
//            writer.write(jsonString);
//            writer.close();
//            Toast.makeText(context, "Backup saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public static void restoreFromJson(Context context) {
//        File file = new File(context.getExternalFilesDir(null), BACKUP_FILE_NAME);
//        if (!file.exists()) {
//            Toast.makeText(context, "No backup file found", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(file));
//            StringBuilder jsonBuilder = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                jsonBuilder.append(line);
//            }
//            reader.close();
//
//            String jsonString = jsonBuilder.toString();
//            Gson gson = new Gson();
//            Type listType = new TypeToken<ArrayList<ToDoItem>>(){}.getType();
//            ArrayList<ToDoItem> restoredItems = gson.fromJson(jsonString, listType);
//
//            DatabaseHelper db = new DatabaseHelper(context);
//            for (ToDoItem item : restoredItems) {
//                db.addToDoItem(item);
//            }
//            Toast.makeText(context, "Data restored successfully", Toast.LENGTH_SHORT).show();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(context, "Restore failed", Toast.LENGTH_SHORT).show();
//        }
//    }
//}
