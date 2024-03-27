package com.anoop.myprojects.todoapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anoop.myprojects.todoapp.DataModels.ToDoItem;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {
    ArrayList<ToDoItem> dataSet;

    public static  class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView title,date,time,id;
        ImageView delete;
        ImageView complete;
        ImageView header;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            this.id= itemView.findViewById(R.id.id);
            this.title= itemView.findViewById(R.id.title);
            this.date= itemView.findViewById(R.id.date);
            this.time= itemView.findViewById(R.id.time);
            this.delete= itemView.findViewById(R.id.delete);
            this.header = itemView.findViewById(R.id.header);
            this.complete = itemView.findViewById(R.id.complete);

        }
    }
    public CustomAdapter(ArrayList<ToDoItem> dataSet) {
        this.dataSet = dataSet;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.cardlayout,
                        parent,
                        false);

        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder viewHolder, int position) {

        TextView title,date,time,id;
        ImageView delete,header,complete;

        title = viewHolder.title;
        date = viewHolder.date;
        time = viewHolder.time;
        id = viewHolder.id;
        delete = viewHolder.delete;
        header = viewHolder.header;
        complete = viewHolder.complete;


        title.setText(dataSet.get(position).getTitle());
        date.setText(dataSet.get(position).getDate());
        time.setText(dataSet.get(position).getTime());
        id.setText(String.valueOf(dataSet.get(position).getId()));

        delete.setImageResource(R.drawable.ic_delete_forever_black_24dp);
        delete.setOnClickListener(MainActivity.deleteOnClickListner);

        complete.setImageResource(R.drawable.baseline_check_circle_outline_24);
        complete.setOnClickListener(MainActivity.completeOnClickListner);

        String titleText = dataSet.get(position).getTitle();
        char c = titleText.toLowerCase().charAt(0);

        header.setImageResource(getImageForHeader(c));


    }

    private int getImageForHeader(char c)
    {
        switch (c) {
            case 'a':
                return R.drawable.a;
            case 'b':
                return R.drawable.b;
            case 'c':
                return R.drawable.c;
            case 'd':
                return R.drawable.d;
            case 'e':
                return R.drawable.e;
            case 'f':
                return R.drawable.f;
            case 'g':
                return R.drawable.g;
            case 'h':
                return R.drawable.h;
            case 'i':
                return R.drawable.i;
            case 'j':
                return R.drawable.j;
            case 'k':
                return R.drawable.k;
            case 'l':
                return R.drawable.l;
            case 'm':
                return R.drawable.m;
            case 'n':
                return R.drawable.n;
            case 'o':
                return R.drawable.o;
            case 'p':
                return R.drawable.p;
            case 'q':
                return R.drawable.q;
            case 'r':
                return R.drawable.r;
            case 's':
                return R.drawable.s;
            case 't':
                return R.drawable.t;
            case 'u':
                return R.drawable.u;
            case 'v':
                return R.drawable.v;
            case 'w':
                return R.drawable.w;
            case 'x':
                return R.drawable.x;
            case 'y':
                return R.drawable.y;
            case 'z':
                return R.drawable.z;
            default:
                return R.drawable.unnamed;
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}