package com.anoop.myprojects.todoapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anoop.myprojects.todoapp.DataModels.Category;
import com.anoop.myprojects.todoapp.DataModels.ToDoItem;
import com.anoop.myprojects.todoapp.Database.DatabaseHelper;
import com.anoop.myprojects.todoapp.databinding.ActivityMainBinding;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int UPDATE_REQUEST_CODE = 1001;
    private static final int SPLASH_DELAY_MS = 2000;

    private ActivityMainBinding binding;
    private RecyclerView recyclerView;
    private RecyclerView completeRecyclerView;
    private TextView errorCompleted;
    private boolean keepSplash = true;
    private AdView adView;
    private AppUpdateManager appUpdateManager;
    private Handler splashHandler;
    private AlertDialog currentDialog;
    private CustomAdapter mainAdapter;
    private String currentSearchQuery = "";
    private int selectedCategoryId = -1; // -1 = All
    private HashMap<Integer, Category> categoryMap = new HashMap<>();

    private final InstallStateUpdatedListener listener = state -> {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showUpdateCompleteDialog();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> keepSplash);
        super.onCreate(savedInstanceState);

        splashHandler = new Handler(Looper.getMainLooper());
        splashHandler.postDelayed(() -> keepSplash = false, SPLASH_DELAY_MS);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));
        changeActionBarIconColor(android.R.color.white);

        checkForUpdate();

        recyclerView = findViewById(R.id.recycler_view);
        View error = findViewById(R.id.error);
        error.setVisibility(View.VISIBLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        loadCategories();
        setupFilterChips();
        refreshPage();
        binding.fab.setOnClickListener(v -> showAddToDoDialog());

        resetAdsIfUpdated();
        adView = findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void loadCategories() {
        categoryMap.clear();
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            for (Category cat : db.getAllCategories()) {
                categoryMap.put(cat.id, cat);
            }
        }
    }

    private void setupFilterChips() {
        ChipGroup chipGroup = findViewById(R.id.filterChipGroup);
        chipGroup.removeAllViews();

        boolean isNight = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        int primaryColor = ContextCompat.getColor(this, R.color.colorPrimary);
        Chip allChip = new Chip(this);
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(selectedCategoryId == -1);
        allChip.setTag(-1);
        allChip.setChipBackgroundColor(chipBgColors(primaryColor, isNight));
        allChip.setTextColor(chipTextColors(primaryColor, isNight));
        allChip.setChipStrokeColor(chipStrokeColors(primaryColor));
        allChip.setChipStrokeWidth(2 * getResources().getDisplayMetrics().density);
        chipGroup.addView(allChip);

        for (Category cat : categoryMap.values()) {
            Chip chip = new Chip(this);
            chip.setText(cat.name);
            chip.setCheckable(true);
            chip.setChecked(selectedCategoryId == cat.id);
            chip.setTag(cat.id);
            try {
                int color = Color.parseColor(cat.color);
                chip.setChipBackgroundColor(chipBgColors(color, isNight));
                chip.setTextColor(chipTextColors(color, isNight));
                chip.setChipStrokeColor(chipStrokeColors(color));
                chip.setChipStrokeWidth(2 * getResources().getDisplayMetrics().density);
            } catch (IllegalArgumentException ignored) {}
            chipGroup.addView(chip);
        }

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            View checked = group.findViewById(checkedIds.get(0));
            if (checked != null && checked.getTag() instanceof Integer) {
                int newCat = (int) checked.getTag();
                if (newCat != selectedCategoryId) {
                    selectedCategoryId = newCat;
                    refreshPage();
                }
            }
        });
    }

    /** Chip background: opaque when checked, semi-transparent when not. */
    private ColorStateList chipBgColors(int color, boolean isNight) {
        int unchecked = Color.argb(isNight ? 70 : 35,
                Color.red(color), Color.green(color), Color.blue(color));
        return new ColorStateList(
                new int[][]{ new int[]{ android.R.attr.state_checked },
                             new int[]{ -android.R.attr.state_checked } },
                new int[]{ color, unchecked });
    }

    /** Chip stroke: darkened color when checked, same color when not. */
    private ColorStateList chipStrokeColors(int color) {
        float f = 0.6f;
        int dark = Color.rgb(
                (int)(Color.red(color)   * f),
                (int)(Color.green(color) * f),
                (int)(Color.blue(color)  * f));
        return new ColorStateList(
                new int[][]{ new int[]{ android.R.attr.state_checked },
                             new int[]{ -android.R.attr.state_checked } },
                new int[]{ dark, Color.TRANSPARENT });
    }

    /** Chip text: white when checked (on solid bg), category color when not. */
    private ColorStateList chipTextColors(int color, boolean isNight) {
        int uncheckedText = isNight
                ? Color.argb(220, Color.red(color), Color.green(color), Color.blue(color))
                : color;
        return new ColorStateList(
                new int[][]{ new int[]{ android.R.attr.state_checked },
                             new int[]{ -android.R.attr.state_checked } },
                new int[]{ Color.WHITE, uncheckedText });
    }

    private void populateCategoryChips(ChipGroup chipGroup, int preselectedCategoryId) {
        chipGroup.removeAllViews();

        boolean isNight = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        int primaryColor = ContextCompat.getColor(this, R.color.colorPrimary);
        Chip noneChip = new Chip(this);
        noneChip.setText("None");
        noneChip.setCheckable(true);
        noneChip.setChecked(preselectedCategoryId == 0);
        noneChip.setTag(0);
        noneChip.setChipBackgroundColor(chipBgColors(primaryColor, isNight));
        noneChip.setTextColor(chipTextColors(primaryColor, isNight));
        noneChip.setChipStrokeColor(chipStrokeColors(primaryColor));
        noneChip.setChipStrokeWidth(2 * getResources().getDisplayMetrics().density);
        chipGroup.addView(noneChip);

        for (Category cat : categoryMap.values()) {
            Chip chip = new Chip(this);
            chip.setText(cat.name);
            chip.setCheckable(true);
            chip.setChecked(preselectedCategoryId == cat.id);
            chip.setTag(cat.id);
            try {
                int color = Color.parseColor(cat.color);
                chip.setChipBackgroundColor(chipBgColors(color, isNight));
                chip.setTextColor(chipTextColors(color, isNight));
                chip.setChipStrokeColor(chipStrokeColors(color));
                chip.setChipStrokeWidth(2 * getResources().getDisplayMetrics().density);
            } catch (IllegalArgumentException ignored) {}
            chipGroup.addView(chip);
        }
    }

    private int getSelectedCategoryFromChipGroup(ChipGroup chipGroup) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked() && chip.getTag() instanceof Integer) {
                return (int) chip.getTag();
            }
        }
        return 0;
    }

    private void populatePriorityChips(ChipGroup cg, int preselectedPriority) {
        cg.removeAllViews();
        boolean isNight = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        String[] labels   = {"Low", "Medium", "High"};
        int[]    colorRes = {R.color.priorityLow, R.color.priorityMedium, R.color.priorityHigh};
        for (int i = 0; i < labels.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(labels[i]);
            chip.setCheckable(true);
            chip.setChecked(i == preselectedPriority);
            chip.setTag(i);
            int color = ContextCompat.getColor(this, colorRes[i]);
            chip.setChipBackgroundColor(chipBgColors(color, isNight));
            chip.setTextColor(chipTextColors(color, isNight));
            chip.setChipStrokeColor(chipStrokeColors(color));
            chip.setChipStrokeWidth(2 * getResources().getDisplayMetrics().density);
            cg.addView(chip);
        }
    }

    private int getSelectedPriorityFromChipGroup(ChipGroup cg) {
        for (int i = 0; i < cg.getChildCount(); i++) {
            Chip chip = (Chip) cg.getChildAt(i);
            if (chip.isChecked() && chip.getTag() instanceof Integer) {
                return (int) chip.getTag();
            }
        }
        return 0; // Default Low
    }

    private void showAddToDoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog, viewGroup, false);
        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        alertDialog.setCancelable(true);
        currentDialog = alertDialog;

        EditText title = dialogView.findViewById(R.id.title);
        TextView date  = dialogView.findViewById(R.id.date);
        TextView time  = dialogView.findViewById(R.id.time);
        EditText notes = dialogView.findViewById(R.id.notes);
        ChipGroup priorityChipGroup  = dialogView.findViewById(R.id.dialogPriorityChipGroup);
        ChipGroup categoryChipGroup  = dialogView.findViewById(R.id.dialogCategoryChipGroup);
        populatePriorityChips(priorityChipGroup, 0);
        populateCategoryChips(categoryChipGroup, 0);

        ImageButton closeButton = dialogView.findViewById(R.id.delete);
        closeButton.setOnClickListener(v -> alertDialog.dismiss());

        View addTodoButton = dialogView.findViewById(R.id.fab_add_todo);
        addTodoButton.setOnClickListener(v -> {
            String titleStr = title.getText().toString().trim();
            String dateStr  = date.getText().toString().trim();
            String timeStr  = time.getText().toString().trim();
            String notesStr = notes.getText().toString().trim();
            int    priority = getSelectedPriorityFromChipGroup(priorityChipGroup);
            int    catId    = getSelectedCategoryFromChipGroup(categoryChipGroup);

            if (titleStr.isEmpty()) { title.setError("Please enter a title"); return; }
            if (dateStr.isEmpty())  { Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show(); return; }
            if (timeStr.isEmpty())  { Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show(); return; }

            ToDoItem toDoItem = new ToDoItem(titleStr, timeStr, dateStr, 0);
            toDoItem.setNotes(notesStr.isEmpty() ? null : notesStr);
            toDoItem.setPriority(priority);
            toDoItem.setCategoryId(catId);
            long id;
            try (DatabaseHelper db = new DatabaseHelper(this)) {
                id = db.addToDoItem(toDoItem);
            }
            if (id > 0) {
                Toast.makeText(this, "ToDo item Added", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
                refreshPage();
            } else {
                Toast.makeText(this, "Failed to add ToDo item", Toast.LENGTH_SHORT).show();
            }
        });

        date.setOnClickListener(v -> showDatePicker(date));
        time.setOnClickListener(v -> showTimePicker(time));

        alertDialog.show();
    }

    private void showViewToDoDialog(ToDoItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog, viewGroup, false);
        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        alertDialog.setCancelable(true);
        currentDialog = alertDialog;

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        if (dialogTitle != null) dialogTitle.setText("Task Details");

        EditText title     = dialogView.findViewById(R.id.title);
        TextView date      = dialogView.findViewById(R.id.date);
        TextView time      = dialogView.findViewById(R.id.time);
        EditText notes     = dialogView.findViewById(R.id.notes);
        ChipGroup priorityCg  = dialogView.findViewById(R.id.dialogPriorityChipGroup);
        ChipGroup categoryCg  = dialogView.findViewById(R.id.dialogCategoryChipGroup);

        title.setText(item.getTitle());
        date.setText(item.getDate());
        time.setText(item.getTime());
        if (item.getNotes() != null) notes.setText(item.getNotes());
        populatePriorityChips(priorityCg, item.getPriority());
        populateCategoryChips(categoryCg, item.getCategoryId());
        priorityCg.setEnabled(false);
        categoryCg.setEnabled(false);

        // Make all fields read-only
        title.setEnabled(false);
        title.setFocusable(false);
        notes.setEnabled(false);
        notes.setFocusable(false);

        ImageButton closeButton = dialogView.findViewById(R.id.delete);
        closeButton.setOnClickListener(v -> alertDialog.dismiss());

        // Repurpose submit button as "Edit"
        MaterialButton submitBtn = dialogView.findViewById(R.id.fab_add_todo);
        submitBtn.setText("Edit Task");
        submitBtn.setOnClickListener(v -> {
            alertDialog.dismiss();
            showEditToDoDialog(item);
        });

        alertDialog.show();
    }

    private void showEditToDoDialog(ToDoItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog, viewGroup, false);
        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        alertDialog.setCancelable(true);
        currentDialog = alertDialog;

        // Customize for edit mode
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        if (dialogTitle != null) dialogTitle.setText("Edit Task");
        MaterialButton submitBtn = dialogView.findViewById(R.id.fab_add_todo);
        submitBtn.setText("Save Changes");

        EditText title  = dialogView.findViewById(R.id.title);
        TextView date   = dialogView.findViewById(R.id.date);
        TextView time   = dialogView.findViewById(R.id.time);
        EditText notes  = dialogView.findViewById(R.id.notes);
        ChipGroup priorityChipGroup  = dialogView.findViewById(R.id.dialogPriorityChipGroup);
        ChipGroup categoryChipGroup  = dialogView.findViewById(R.id.dialogCategoryChipGroup);

        // Pre-fill existing values
        title.setText(item.getTitle());
        title.setSelection(title.getText().length());
        date.setText(item.getDate());
        time.setText(item.getTime());
        if (item.getNotes() != null) notes.setText(item.getNotes());
        populatePriorityChips(priorityChipGroup, item.getPriority());
        populateCategoryChips(categoryChipGroup, item.getCategoryId());

        ImageButton closeButton = dialogView.findViewById(R.id.delete);
        closeButton.setOnClickListener(v -> alertDialog.dismiss());

        submitBtn.setOnClickListener(v -> {
            String titleStr = title.getText().toString().trim();
            String dateStr  = date.getText().toString().trim();
            String timeStr  = time.getText().toString().trim();
            String notesStr = notes.getText().toString().trim();
            int    priority = getSelectedPriorityFromChipGroup(priorityChipGroup);
            int    catId    = getSelectedCategoryFromChipGroup(categoryChipGroup);

            if (titleStr.isEmpty()) { title.setError("Please enter a title"); return; }
            if (dateStr.isEmpty())  { Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show(); return; }
            if (timeStr.isEmpty())  { Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show(); return; }

            boolean success;
            try (DatabaseHelper db = new DatabaseHelper(this)) {
                success = db.editToDoItem(item.getId(), titleStr, timeStr, dateStr,
                        notesStr.isEmpty() ? null : notesStr, catId, priority);
            }
            if (success) {
                Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
                refreshPage();
            } else {
                Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show();
            }
        });

        date.setOnClickListener(v -> showDatePicker(date));
        time.setOnClickListener(v -> showTimePicker(time));

        alertDialog.show();
    }

    private void showDatePicker(TextView dateView) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (datePicker, y, m, d) -> dateView.setText(d + "-" + (m + 1) + "-" + y),
                year, month, day);
        datePickerDialog.getDatePicker().setMinDate(new Date().getTime());
        datePickerDialog.show();
    }

    private void showTimePicker(TextView timeView) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (timePicker, h, m) -> {
                    try {
                        String raw = h + ":" + m;
                        SimpleDateFormat format24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        SimpleDateFormat format12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                        Date parsed = format24.parse(raw);
                        if (parsed != null) {
                            timeView.setText(format12.format(parsed));
                        }
                    } catch (Exception e) {
                        timeView.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
                    }
                }, hour, min, false);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    private void changeActionBarIconColor(int colorRes) {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.baseline_add_task_24);
        if (drawable != null) {
            drawable.setColorFilter(ContextCompat.getColor(this, colorRes), PorterDuff.Mode.SRC_ATOP);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(drawable);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search tasks...");

        // White text and hint inside the SearchView to match the toolbar
        android.widget.EditText searchEditText =
                searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(0x80FFFFFF); // 50% white for hint
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query;
                refreshPage();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                refreshPage();
                return true;
            }
        });
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                currentSearchQuery = "";
                refreshPage();
                return true;
            }
        });

        return true;
    }

    public void showCompletedTask() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.completed, viewGroup, false);
        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
        }
        alertDialog.setCancelable(true);
        currentDialog = alertDialog;

        completeRecyclerView = dialogView.findViewById(R.id.recycler_view);
        errorCompleted = dialogView.findViewById(R.id.error_for_complete);
        errorCompleted.setVisibility(View.VISIBLE);

        ImageButton closeButton = dialogView.findViewById(R.id.delete);
        closeButton.setOnClickListener(v -> alertDialog.dismiss());

        completeRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        completeRecyclerView.setItemAnimator(new DefaultItemAnimator());

        alertDialog.setOnDismissListener(d -> {
            completeRecyclerView = null;
            errorCompleted = null;
        });

        refreshPageForCompletedTask();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_completed_tasks) {
            showCompletedTask();
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, BackupRestoreActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            gotoAboutPage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void gotoAboutPage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.about_app, viewGroup, false);
        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
        }
        alertDialog.setCancelable(true);
        currentDialog = alertDialog;

        ImageButton closeButton = dialogView.findViewById(R.id.delete);
        closeButton.setOnClickListener(v -> alertDialog.dismiss());

        View btnShare = dialogView.findViewById(R.id.btnShare);
        btnShare.setOnClickListener(v -> {
            String shareMessage = "Check out this TODO App!\n\n"
                    + "https://play.google.com/store/apps/details?id=" + getPackageName();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(intent, "Share via"));
        });

        alertDialog.show();
    }

    public void refreshPage() {
        ArrayList<ToDoItem> toDoItems;
        try (DatabaseHelper db = new DatabaseHelper(getApplicationContext())) {
            toDoItems = db.getAllToDoItems(false);
        }
        ArrayList<CustomAdapter.ListItem> sectionedList = buildSectionedList(toDoItems);
        View error = findViewById(R.id.error);
        error.setVisibility(sectionedList.isEmpty() ? View.VISIBLE : View.GONE);
        mainAdapter = new CustomAdapter(sectionedList, categoryMap,
                id -> { deleteFromDb(id); refreshPage(); },
                (id, isCompleted) -> { toggleInDb(id, !isCompleted); refreshPage(); },
                item -> showEditToDoDialog(item),
                item -> showViewToDoDialog(item));
        recyclerView.setAdapter(mainAdapter);
    }

    public void refreshPageForCompletedTask() {
        if (completeRecyclerView == null || errorCompleted == null) return;
        ArrayList<ToDoItem> items;
        try (DatabaseHelper db = new DatabaseHelper(getApplicationContext())) {
            items = db.getAllToDoItems(true);
        }
        errorCompleted.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        ArrayList<CustomAdapter.ListItem> wrappedItems = new ArrayList<>();
        for (ToDoItem item : items) wrappedItems.add(new CustomAdapter.TaskItem(item));
        completeRecyclerView.setAdapter(new CustomAdapter(wrappedItems, categoryMap,
                id -> { deleteFromDb(id); refreshPageForCompletedTask(); refreshPage(); },
                (id, isCompleted) -> { toggleInDb(id, !isCompleted); refreshPageForCompletedTask(); refreshPage(); },
                item -> showEditToDoDialog(item),
                item -> showViewToDoDialog(item)));
    }

    private ArrayList<CustomAdapter.ListItem> buildSectionedList(ArrayList<ToDoItem> allItems) {
        // Apply search filter
        ArrayList<ToDoItem> filtered = new ArrayList<>();
        if (currentSearchQuery.isEmpty()) {
            filtered.addAll(allItems);
        } else {
            String q = currentSearchQuery.toLowerCase(Locale.getDefault());
            for (ToDoItem item : allItems) {
                if (item.getTitle() != null
                        && item.getTitle().toLowerCase(Locale.getDefault()).contains(q)) {
                    filtered.add(item);
                }
            }
        }

        // Category filter
        if (selectedCategoryId >= 0) {
            ArrayList<ToDoItem> catFiltered = new ArrayList<>();
            for (ToDoItem item : filtered) {
                if (item.getCategoryId() == selectedCategoryId) catFiltered.add(item);
            }
            filtered = catFiltered;
        }

        // Sort by date ascending (parse errors sort to the end)
        final SimpleDateFormat sdf = new SimpleDateFormat("d-M-yyyy", Locale.getDefault());
        sdf.setLenient(false);
        Collections.sort(filtered, (a, b) -> {
            try {
                Date da = sdf.parse(a.getDate());
                Date db = sdf.parse(b.getDate());
                if (da == null && db == null) return 0;
                if (da == null) return 1;
                if (db == null) return -1;
                return da.compareTo(db);
            } catch (ParseException e) {
                return 0;
            }
        });

        // Calculate bucket boundaries at midnight
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date todayStart    = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 1); Date tomorrowStart  = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 1); Date dayAfterStart  = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 5); Date weekEnd        = cal.getTime();

        ArrayList<ToDoItem> overdue  = new ArrayList<>();
        ArrayList<ToDoItem> today    = new ArrayList<>();
        ArrayList<ToDoItem> tomorrow = new ArrayList<>();
        ArrayList<ToDoItem> thisWeek = new ArrayList<>();
        ArrayList<ToDoItem> later    = new ArrayList<>();

        for (ToDoItem item : filtered) {
            try {
                Date d = sdf.parse(item.getDate());
                if (d == null)               { later.add(item);    continue; }
                if (d.before(todayStart))    { overdue.add(item);  continue; }
                if (d.before(tomorrowStart)) { today.add(item);    continue; }
                if (d.before(dayAfterStart)) { tomorrow.add(item); continue; }
                if (d.before(weekEnd))       { thisWeek.add(item); continue; }
                later.add(item);
            } catch (ParseException e) {
                later.add(item);
            }
        }

        ArrayList<CustomAdapter.ListItem> result = new ArrayList<>();
        addSection(result, "Overdue",   overdue);
        addSection(result, "Today",     today);
        addSection(result, "Tomorrow",  tomorrow);
        addSection(result, "This Week", thisWeek);
        addSection(result, "Later",     later);
        return result;
    }

    private void addSection(ArrayList<CustomAdapter.ListItem> result,
                            String label, ArrayList<ToDoItem> items) {
        if (!items.isEmpty()) {
            result.add(new CustomAdapter.SectionHeader(label));
            for (ToDoItem item : items) result.add(new CustomAdapter.TaskItem(item));
        }
    }

    private void deleteFromDb(int id) {
        ToDoItem deletedItem;
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            deletedItem = db.getToDoItemById(id);
            db.deleteToDoItem(id);
        }
        Snackbar.make(binding.getRoot(), "Task deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    if (deletedItem != null) {
                        try (DatabaseHelper db = new DatabaseHelper(this)) {
                            db.addToDoItem(deletedItem);
                        }
                        refreshPage();
                    }
                })
                .show();
    }

    private void toggleInDb(int id, boolean markComplete) {
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            boolean success = db.updateToDoItem(id, markComplete);
            if (success) {
                String msg = markComplete ? "Marked complete" : "Marked incomplete";
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT)
                        .setAction("Undo", v -> {
                            try (DatabaseHelper db2 = new DatabaseHelper(this)) {
                                db2.updateToDoItem(id, !markComplete);
                            }
                            refreshPage();
                        })
                        .show();
            } else {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }

    private void checkForUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager.getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo, AppUpdateType.FLEXIBLE,
                                    this, UPDATE_REQUEST_CODE);
                        } catch (Exception e) {
                            // Update flow unavailable; skip silently
                        }
                    }
                });
    }

    private void showUpdateCompleteDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Update Ready")
                .setMessage("New version downloaded. Restart to apply update.")
                .setCancelable(false)
                .setPositiveButton("Restart", (dialog, which) -> appUpdateManager.completeUpdate())
                .show();
    }

    private void resetAdsIfUpdated() {
        if (isAppUpdated()) {
            MobileAds.disableMediationAdapterInitialization(this);
            MobileAds.setRequestConfiguration(new RequestConfiguration.Builder()
                    .setTagForUnderAgeOfConsent(
                            RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE)
                    .build());
            MobileAds.initialize(this, status -> {});
        }
    }

    private boolean isAppUpdated() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        int savedVersion = prefs.getInt("version_code", -1);
        int currentVersion = BuildConfig.VERSION_CODE;
        if (savedVersion != currentVersion) {
            prefs.edit().putInt("version_code", currentVersion).apply();
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPage();
        if (adView != null) adView.resume();
        if (appUpdateManager != null) appUpdateManager.registerListener(listener);
    }

    @Override
    protected void onPause() {
        if (adView != null) adView.pause();
        if (appUpdateManager != null) appUpdateManager.unregisterListener(listener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (splashHandler != null) {
            splashHandler.removeCallbacksAndMessages(null);
            splashHandler = null;
        }
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }
        if (adView != null) adView.destroy();
        if (appUpdateManager != null) {
            appUpdateManager.unregisterListener(listener);
            appUpdateManager = null;
        }
        super.onDestroy();
    }
}
