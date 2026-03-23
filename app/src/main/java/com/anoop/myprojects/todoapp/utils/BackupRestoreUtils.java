package com.anoop.myprojects.todoapp.utils;

import android.content.Context;
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
    private static final String SQLITE_HEADER = "SQLite format 3";

    public static void backupDatabase(Context context) {
        try {
            File dbFile = context.getDatabasePath(DB_NAME);
            if (dbFile == null || !dbFile.exists()) {
                Toast.makeText(context, "Database not found", Toast.LENGTH_SHORT).show();
                return;
            }

            File backupFolder = new File(context.getExternalFilesDir(null), BACKUP_DIR);
            if (!backupFolder.exists()) backupFolder.mkdirs();

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                    .format(new Date());
            File backupFile = new File(backupFolder, "todo_backup_" + timestamp + ".db");

            try (FileInputStream fis = new FileInputStream(dbFile);
                 FileOutputStream fos = new FileOutputStream(backupFile);
                 FileChannel src = fis.getChannel();
                 FileChannel dst = fos.getChannel()) {
                dst.transferFrom(src, 0, src.size());
            }

            cleanupOldBackups(backupFolder);
            Toast.makeText(context, "Backup created: " + backupFile.getName(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void restoreDatabase(Context context, File selectedBackup) {
        if (!isValidSQLiteFile(selectedBackup)) {
            Toast.makeText(context, "Invalid backup file", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File dbFile = context.getDatabasePath(DB_NAME);
            if (dbFile == null) {
                Toast.makeText(context, "Cannot locate database path", Toast.LENGTH_SHORT).show();
                return;
            }

            try (FileInputStream fis = new FileInputStream(selectedBackup);
                 FileOutputStream fos = new FileOutputStream(dbFile);
                 FileChannel src = fis.getChannel();
                 FileChannel dst = fos.getChannel()) {
                dst.transferFrom(src, 0, src.size());
            }
            Toast.makeText(context, "Restored: " + selectedBackup.getName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Restore failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static List<File> getBackupFiles(Context context) {
        File backupFolder = new File(context.getExternalFilesDir(null), BACKUP_DIR);
        File[] files = backupFolder.listFiles((dir, name) -> name.endsWith(".db"));
        if (files == null) return new ArrayList<>();
        List<File> backups = Arrays.asList(files);
        backups.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        return backups;
    }

    private static void cleanupOldBackups(File backupFolder) {
        File[] files = backupFolder.listFiles((dir, name) -> name.endsWith(".db"));
        if (files == null || files.length <= MAX_BACKUPS) return;
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        for (int i = 0; i < files.length - MAX_BACKUPS; i++) {
            files[i].delete();
        }
    }

    private static boolean isValidSQLiteFile(File file) {
        if (file == null || !file.exists() || file.length() < 16) return false;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] header = new byte[16];
            if (fis.read(header) < 16) return false;
            return new String(header, "UTF-8").startsWith(SQLITE_HEADER);
        } catch (Exception e) {
            return false;
        }
    }
}
