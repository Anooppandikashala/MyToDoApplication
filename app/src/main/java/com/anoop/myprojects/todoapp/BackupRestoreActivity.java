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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.List;

public class BackupRestoreActivity extends AppCompatActivity {
    private ListView listView;
    private Button backupBtn;
    private List<File> backupFiles;
    private AdView adView;

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

        adView = findViewById(R.id.adView2);
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void loadBackups() {
        backupFiles = BackupRestoreUtils.getBackupFiles(this);

        BackupAdapter adapter = new BackupAdapter(this, backupFiles, file ->
                new AlertDialog.Builder(this)
                        .setTitle("Delete Backup")
                        .setMessage("Are you sure you want to delete " + file.getName() + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            file.delete();
                            Toast.makeText(this, "Deleted: " + file.getName(), Toast.LENGTH_SHORT).show();
                            loadBackups();
                        })
                        .setNegativeButton("Cancel", null)
                        .show());

        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            File selected = backupFiles.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Restore Backup")
                    .setMessage("Restore from " + selected.getName() + "?\nCurrent data will be replaced.")
                    .setPositiveButton("Restore", (dialog, which) ->
                            BackupRestoreUtils.restoreDatabase(this, selected))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    public void backPressed(View view) {
        getOnBackPressedDispatcher().onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) adView.resume();
    }

    @Override
    protected void onPause() {
        if (adView != null) adView.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) adView.destroy();
        super.onDestroy();
    }
}
