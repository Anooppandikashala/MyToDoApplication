package com.anoop.myprojects.todoapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.anoop.myprojects.todoapp.adapters.BackupAdapter;
import com.anoop.myprojects.todoapp.utils.BackupRestoreUtils;

import java.io.File;
import java.util.List;

public class BackupRestoreActivity extends AppCompatActivity {
    private ListView listView;
    private Button backupBtn;
    private List<File> backupFiles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_backup_restore);
        listView = findViewById(R.id.listBackups);
        backupBtn = findViewById(R.id.btnCreateBackup);
        backupBtn.setOnClickListener(v -> {
            BackupRestoreUtils.backupDatabase(this);
            loadBackups();
        });
        loadBackups();
    }

    private void loadBackups() {
        backupFiles = BackupRestoreUtils.getBackupFiles(BackupRestoreActivity.this);

        BackupAdapter adapter = new BackupAdapter(BackupRestoreActivity.this, backupFiles, file -> {
            new AlertDialog.Builder(BackupRestoreActivity.this)
                    .setTitle("Delete Backup")
                    .setMessage("Are you sure you want to delete " + file.getName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        file.delete();
                        Toast.makeText(this, "Deleted: " + file.getName(), Toast.LENGTH_SHORT).show();
                        loadBackups();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            System.out.println("in click");
            File selected = backupFiles.get(position);
            BackupRestoreUtils.restoreDatabase(BackupRestoreActivity.this, selected);
        });
    }

    public void backPressed(View view) {
        getOnBackPressedDispatcher().onBackPressed();
    }
}