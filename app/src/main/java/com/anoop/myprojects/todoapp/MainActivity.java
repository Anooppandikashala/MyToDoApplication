package com.anoop.myprojects.todoapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anoop.myprojects.todoapp.DataModels.ToDoItem;
import com.anoop.myprojects.todoapp.Database.DatabaseHelper;
import com.anoop.myprojects.todoapp.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration appBarConfiguration;
    public static  View.OnClickListener deleteOnClickListner;
    public static  View.OnClickListener completeOnClickListner;
    public static  View.OnClickListener deleteOnClickListnerForCompletedTodoList;
    public static  View.OnClickListener completeOnClickListnerForCompletedTodoList;
    private  static RecyclerView recyclerView, completeRecyclerView;
    private TextView errorCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.anoop.myprojects.todoapp.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        changeActionBarIconColor(android.R.color.white);

        deleteOnClickListner = new MainActivity.DeleteOnClickListener(MainActivity.this);
        completeOnClickListner = new MainActivity.CompleteOnClickListener(MainActivity.this);

        deleteOnClickListnerForCompletedTodoList = new MainActivity.DeleteOnClickListenerForComplete(MainActivity.this);
        completeOnClickListnerForCompletedTodoList = new MainActivity.CompleteOnClickListenerForCompletedToDoList(MainActivity.this);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        TextView error = findViewById(R.id.error);
        error.setVisibility(View.VISIBLE);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        refreshPage();
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddToDoDialog();
            }
        });
    }

    private void showAddToDoDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog, viewGroup, false);
        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCancelable(false);

        EditText title = dialogView.findViewById(R.id.title);
        TextView date = dialogView.findViewById(R.id.date);
        TextView time = dialogView.findViewById(R.id.time);
        ImageButton deleteButton = dialogView.findViewById(R.id.delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        FloatingActionButton addTodoButton = (FloatingActionButton) dialogView.findViewById(R.id.fab_add_todo);
        // if button is clicked, close the custom dialog
        addTodoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String dateStr =  date.getText().toString();
                String timeStr =  time.getText().toString();
                String titleStr =  title.getText().toString();

                if(!dateStr.isEmpty() && !timeStr.isEmpty() && !titleStr.isEmpty())
                {
                    ToDoItem toDoItem  = new ToDoItem(titleStr,timeStr,dateStr,0);
                    long id;
                    try (DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this)) {
                        id = databaseHelper.addToDoItem(toDoItem);
                    }

                    if(id >0)
                    {
                        Toast.makeText(MainActivity.this, "ToDo item Added", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                        refreshPage();
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this,"ToDo item Failed to Add",Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this,"Please give details", Toast.LENGTH_SHORT).show();
                }
            }
        });
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mYear = 0,mMonth = 0,mDay = 0;
                DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker,
                                                  int year,
                                                  int Month,
                                                  int day) {

                                date.setText(day + "-"+(Month+1) +"-"+year);

                            }
                        },mYear,mMonth,mDay);
                datePickerDialog.getDatePicker().setMinDate(new Date().getTime());
                datePickerDialog.show();

            }
        });

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int min = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(view.getContext(),
                        new TimePickerDialog.OnTimeSetListener()
                        {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int Hour, int Min) {
                                try {
                                    String _24Hr_time = Hour + ":"+Min;
                                    SimpleDateFormat _24HrFormat = new SimpleDateFormat("hh:mm");
                                    SimpleDateFormat _12HrFormat = new SimpleDateFormat("hh:mm a");
                                    Date _24HrDate = _24HrFormat.parse(_24Hr_time);
                                    if (_24HrDate != null) {
                                        time.setText(_12HrFormat.format(_24HrDate));
                                    }
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        },hour,min,false);


                timePickerDialog.setTitle("Select Time");
                timePickerDialog.show();

            }
        });
        alertDialog.show();
    }

    private void changeActionBarIconColor(int colorRes) {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.baseline_add_task_24); // Replace ic_settings with your icon resource
        if (drawable != null) {
            drawable.setColorFilter(ContextCompat.getColor(this, colorRes), PorterDuff.Mode.SRC_ATOP);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // Optional: Enable Up button
            getSupportActionBar().setHomeAsUpIndicator(drawable);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void showCompletedTask()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.completed, viewGroup, false);
        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;

        completeRecyclerView = dialogView.findViewById(R.id.recycler_view);
        completeRecyclerView.setHasFixedSize(true);

        errorCompleted = dialogView.findViewById(R.id.error_for_complete);
        errorCompleted.setVisibility(View.VISIBLE);

        ImageButton deleteButtonForCompleted = dialogView.findViewById(R.id.delete);
        deleteButtonForCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        RecyclerView.LayoutManager layoutManagerCompleted = new LinearLayoutManager(getApplicationContext());
        completeRecyclerView.setLayoutManager(layoutManagerCompleted);
        completeRecyclerView.setItemAnimator(new DefaultItemAnimator());
        refreshPageForCompletedTask();
        window.setAttributes(wlp);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showCompletedTask();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshPage()
    {
        ArrayList<ToDoItem> toDoItems = new ArrayList<>();
        try (DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext())) {
            toDoItems = databaseHelper.getAllToDoItems(false);
        }
        TextView error = findViewById(R.id.error);
        if(toDoItems.isEmpty())
        {
            error.setVisibility(View.VISIBLE);
        }
        else {
            error.setVisibility(View.GONE);
        }
        RecyclerView.Adapter<CustomAdapter.MyViewHolder> adapter = new CustomAdapter(toDoItems);
        recyclerView.setAdapter(adapter);
    }

    public void refreshPageForCompletedTask()
    {
        ArrayList<ToDoItem> completeToDoItems;
        try (DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext())) {
            completeToDoItems = databaseHelper.getAllToDoItems(true);
        }
        if(completeToDoItems.isEmpty())
        {
            errorCompleted.setVisibility(View.VISIBLE);
        }
        else {
            errorCompleted.setVisibility(View.GONE);
        }
        RecyclerView.Adapter<CustomAdapter.MyViewHolder> completeAdapter = new CustomAdapter(completeToDoItems);
        completeRecyclerView.setAdapter(completeAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return  super.onSupportNavigateUp();
    }

    private  class DeleteOnClickListener implements View.OnClickListener
    {
        private final Context context;
        public DeleteOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View view) {
            String selectedId="";
            ViewGroup parentView =(ViewGroup) view.getParent().getParent();
            TextView id =  parentView.findViewById(R.id.todoId);
            selectedId = id.getText().toString();
            try (DatabaseHelper databaseHelper = new DatabaseHelper(this.context)) {
                databaseHelper.deleteToDoItem(Integer.parseInt(selectedId));
            }
            refreshPage();
            Toast.makeText(context,"Deleted Success", Toast.LENGTH_SHORT).show();
        }
    }

    private  class DeleteOnClickListenerForComplete implements View.OnClickListener
    {
        private final Context context;
        public DeleteOnClickListenerForComplete(Context context) {
            this.context = context;
        }
        @Override
        public void onClick(View view) {
            String selectedId="";
            ViewGroup parentView =(ViewGroup) view.getParent().getParent();
            TextView id =  parentView.findViewById(R.id.todoId);
            selectedId = id.getText().toString();
            try (DatabaseHelper databaseHelper = new DatabaseHelper(this.context)) {
                databaseHelper.deleteToDoItem(Integer.parseInt(selectedId));
            }
            refreshPageForCompletedTask();
            Toast.makeText(context,"Deleted Success", Toast.LENGTH_SHORT).show();
        }
    }

    private class CompleteOnClickListener implements View.OnClickListener
    {
        private final Context context;
        public CompleteOnClickListener(Context context) {
            this.context = context;
        }
        @Override
        public void onClick(View view) {
            String selectedId="";
            ViewGroup parentView =(ViewGroup) view.getParent().getParent();
            TextView id =  parentView.findViewById(R.id.todoId);
            selectedId = id.getText().toString();
            try (DatabaseHelper databaseHelper = new DatabaseHelper(this.context)) {
                databaseHelper.updateToDoItem(Integer.parseInt(selectedId), true);
            }
            refreshPage();
            Toast.makeText(context,"Completed Success", Toast.LENGTH_SHORT).show();
        }
    }

    private class CompleteOnClickListenerForCompletedToDoList implements View.OnClickListener
    {
        private final Context context;
        public CompleteOnClickListenerForCompletedToDoList(Context context) {
            this.context = context;
        }
        @Override
        public void onClick(View view) {
            String selectedId="";
            ViewGroup parentView =(ViewGroup) view.getParent().getParent();
            TextView id =  parentView.findViewById(R.id.todoId);
            selectedId = id.getText().toString();
            try (DatabaseHelper databaseHelper = new DatabaseHelper(this.context)) {
                databaseHelper.updateToDoItem(Integer.parseInt(selectedId), false);
            }
            refreshPageForCompletedTask();
            refreshPage();
            Toast.makeText(context,"Not Completed Success", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPage();
    }
}