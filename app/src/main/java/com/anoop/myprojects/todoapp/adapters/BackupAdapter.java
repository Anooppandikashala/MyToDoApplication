package com.anoop.myprojects.todoapp.adapters;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import com.anoop.myprojects.todoapp.R;
import java.io.File;
import java.util.List;

public class BackupAdapter extends ArrayAdapter<File> {
    private final Context context;
    private final List<File> backups;
    private final OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDelete(File file);
    }

    public BackupAdapter(Context context, List<File> backups, OnDeleteClickListener listener) {
        super(context, R.layout.item_backup, backups);
        this.context = context;
        this.backups = backups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        File file = backups.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_backup, parent, false);
        }
        TextView nameText = convertView.findViewById(R.id.backupName);
        ImageButton deleteBtn = convertView.findViewById(R.id.deleteBtn);
        nameText.setText(file.getName());
        deleteBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(file);
            }
        });
        return convertView;
    }
}
