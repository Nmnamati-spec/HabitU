package com.habitu.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class HabitSpinnerAdapter extends ArrayAdapter<String> {

    private final LayoutInflater inflater;

    public HabitSpinnerAdapter(@NonNull Context context, @NonNull List<String> items) {
        super(context, 0, items);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return buildView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return buildView(position, convertView, parent);
    }

    private View buildView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_spinner_habit, parent, false);
        }
        String item = getItem(position);
        ImageView imgIcon = convertView.findViewById(R.id.imgSpinnerIcon);
        TextView  tvLabel = convertView.findViewById(R.id.tvSpinnerLabel);
        imgIcon.setImageResource(HabitIconMapper.getIcon(item));
        tvLabel.setText(item);
        return convertView;
    }
}
