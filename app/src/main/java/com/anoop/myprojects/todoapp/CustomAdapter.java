package com.anoop.myprojects.todoapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.anoop.myprojects.todoapp.DataModels.Category;
import com.anoop.myprojects.todoapp.DataModels.ToDoItem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.BaseViewHolder> {

    // ── View types ─────────────────────────────────────────────────────────────

    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_TASK   = 1;

    // ── ListItem model hierarchy ───────────────────────────────────────────────

    public abstract static class ListItem {
        public abstract int getType();
    }

    public static class SectionHeader extends ListItem {
        public final String label;
        public SectionHeader(String label) { this.label = label; }
        @Override public int getType() { return VIEW_TYPE_HEADER; }
    }

    public static class TaskItem extends ListItem {
        public final ToDoItem item;
        public TaskItem(ToDoItem item) { this.item = item; }
        @Override public int getType() { return VIEW_TYPE_TASK; }
    }

    // ── Interfaces ─────────────────────────────────────────────────────────────

    public interface OnDeleteListener { void onDelete(int id); }
    public interface OnToggleListener { void onToggle(int id, boolean isCurrentlyCompleted); }
    public interface OnEditListener   { void onEdit(ToDoItem item); }
    public interface OnViewListener   { void onView(ToDoItem item); }

    // ── ViewHolders ────────────────────────────────────────────────────────────

    public abstract static class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(@NonNull View itemView) { super(itemView); }
    }

    public static class HeaderViewHolder extends BaseViewHolder {
        final TextView label;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.sectionLabel);
        }
    }

    public static class TaskViewHolder extends BaseViewHolder {
        final TextView title, date, time, id, notesPreview, categoryChip;
        final ImageView delete, complete, header;
        final View priorityIndicator;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            id                = itemView.findViewById(R.id.todoId);
            title             = itemView.findViewById(R.id.title);
            date              = itemView.findViewById(R.id.date);
            time              = itemView.findViewById(R.id.time);
            notesPreview      = itemView.findViewById(R.id.notesPreview);
            categoryChip      = itemView.findViewById(R.id.categoryChip);
            delete            = itemView.findViewById(R.id.delete);
            header            = itemView.findViewById(R.id.header);
            complete          = itemView.findViewById(R.id.complete);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
        }
    }

    // ── Fields ─────────────────────────────────────────────────────────────────

    private final ArrayList<ListItem> items;
    private final HashMap<Integer, Category> categoryMap;
    private final OnDeleteListener deleteListener;
    private final OnToggleListener toggleListener;
    private final OnEditListener   editListener;
    private final OnViewListener   viewListener;

    public CustomAdapter(ArrayList<ListItem> items,
                         HashMap<Integer, Category> categoryMap,
                         OnDeleteListener deleteListener,
                         OnToggleListener toggleListener,
                         OnEditListener   editListener,
                         OnViewListener   viewListener) {
        this.items          = items;
        this.categoryMap    = categoryMap;
        this.deleteListener = deleteListener;
        this.toggleListener = toggleListener;
        this.editListener   = editListener;
        this.viewListener   = viewListener;
    }

    // ── RecyclerView overrides ─────────────────────────────────────────────────

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(inf.inflate(R.layout.section_header, parent, false));
        }
        return new TaskViewHolder(inf.inflate(R.layout.cardlayout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        ListItem listItem = items.get(position);
        if (listItem.getType() == VIEW_TYPE_HEADER) {
            ((HeaderViewHolder) holder).label.setText(((SectionHeader) listItem).label);
        } else {
            bindTask((TaskViewHolder) holder, ((TaskItem) listItem).item);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    // ── Task binding ───────────────────────────────────────────────────────────

    private void bindTask(@NonNull TaskViewHolder vh, ToDoItem item) {
        Context ctx = vh.itemView.getContext();

        vh.title.setText(item.getTitle());
        vh.date.setText(item.getDate());
        vh.time.setText(item.getTime());
        vh.id.setText(String.valueOf(item.getId()));

        // Notes preview
        String notes = item.getNotes();
        if (notes != null && !notes.isEmpty()) {
            vh.notesPreview.setText(notes);
            vh.notesPreview.setVisibility(View.VISIBLE);
        } else {
            vh.notesPreview.setVisibility(View.GONE);
        }

        // Category chip
        Category cat = (categoryMap != null) ? categoryMap.get(item.getCategoryId()) : null;
        if (cat != null) {
            try {
                int color = Color.parseColor(cat.color);
                boolean isNight = (ctx.getResources().getConfiguration().uiMode
                        & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                        == android.content.res.Configuration.UI_MODE_NIGHT_YES;
                float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20,
                        ctx.getResources().getDisplayMetrics());
                GradientDrawable bg = new GradientDrawable();
                if (isNight) {
                    // Solid enough to show on dark card surfaces
                    bg.setColor(Color.argb(140, Color.red(color), Color.green(color), Color.blue(color)));
                    vh.categoryChip.setTextColor(Color.WHITE);
                } else {
                    bg.setColor(Color.argb(40, Color.red(color), Color.green(color), Color.blue(color)));
                    vh.categoryChip.setTextColor(color);
                }
                bg.setCornerRadius(radius);
                vh.categoryChip.setBackground(bg);
                vh.categoryChip.setText(cat.name);
                vh.categoryChip.setVisibility(View.VISIBLE);
            } catch (IllegalArgumentException e) {
                vh.categoryChip.setVisibility(View.GONE);
            }
        } else {
            vh.categoryChip.setVisibility(View.GONE);
        }

        int itemId      = item.getId();
        boolean isCompleted = item.getCompleted() == 1;

        vh.delete.setImageResource(R.drawable.ic_delete_forever_red_24dp);
        vh.complete.setImageResource(isCompleted
                ? R.drawable.baseline_check_circle_24
                : R.drawable.baseline_check_circle_outline_24);

        // Priority accent strip
        if (vh.priorityIndicator != null) {
            int[] priorityColors = {
                ContextCompat.getColor(ctx, R.color.priorityLow),
                ContextCompat.getColor(ctx, R.color.priorityMedium),
                ContextCompat.getColor(ctx, R.color.priorityHigh)
            };
            int p = item.getPriority();
            vh.priorityIndicator.setBackgroundColor(
                    priorityColors[p >= 0 && p < priorityColors.length ? p : 1]);
        }

        // Overdue highlighting
        boolean overdue = !isCompleted && isOverdue(item.getDate());
        vh.date.setBackgroundResource(overdue ? R.drawable.chip_bg_overdue : R.drawable.chip_bg);
        ColorStateList dateColor = ColorStateList.valueOf(
                ContextCompat.getColor(ctx, overdue ? R.color.colorDeleteRed : R.color.chipText));
        vh.date.setTextColor(dateColor);
        TextViewCompat.setCompoundDrawableTintList(vh.date, dateColor);

        vh.delete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(itemId);
        });
        vh.complete.setOnClickListener(v -> {
            if (toggleListener != null) toggleListener.onToggle(itemId, isCompleted);
        });
        vh.itemView.setOnClickListener(v -> {
            if (viewListener != null) viewListener.onView(item);
        });
        vh.itemView.setOnLongClickListener(v -> {
            if (editListener != null) editListener.onEdit(item);
            return true;
        });

        String titleText = item.getTitle();
        if (titleText != null && !titleText.isEmpty()) {
            vh.header.setImageResource(getImageForHeader(titleText.toLowerCase().charAt(0)));
        } else {
            vh.header.setImageResource(R.drawable.unnamed);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private boolean isOverdue(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d-M-yyyy", Locale.getDefault());
            sdf.setLenient(false);
            Date taskDate = sdf.parse(dateStr);
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            return taskDate != null && taskDate.before(today.getTime());
        } catch (Exception e) {
            return false;
        }
    }

    private int getImageForHeader(char c) {
        switch (c) {
            case 'a': return R.drawable.a;
            case 'b': return R.drawable.b;
            case 'c': return R.drawable.c;
            case 'd': return R.drawable.d;
            case 'e': return R.drawable.e;
            case 'f': return R.drawable.f;
            case 'g': return R.drawable.g;
            case 'h': return R.drawable.h;
            case 'i': return R.drawable.i;
            case 'j': return R.drawable.j;
            case 'k': return R.drawable.k;
            case 'l': return R.drawable.l;
            case 'm': return R.drawable.m;
            case 'n': return R.drawable.n;
            case 'o': return R.drawable.o;
            case 'p': return R.drawable.p;
            case 'q': return R.drawable.q;
            case 'r': return R.drawable.r;
            case 's': return R.drawable.s;
            case 't': return R.drawable.t;
            case 'u': return R.drawable.u;
            case 'v': return R.drawable.v;
            case 'w': return R.drawable.w;
            case 'x': return R.drawable.x;
            case 'y': return R.drawable.y;
            case 'z': return R.drawable.z;
            default:  return R.drawable.unnamed;
        }
    }
}
