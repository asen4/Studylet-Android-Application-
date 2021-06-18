package com.example.studyletapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class FileOptionsAdapter extends ArrayAdapter {

    private ArrayList<Item> optionsList;
    private Context context;

    public FileOptionsAdapter(Context context, int textViewResourceID, ArrayList objects) {
        super(context, textViewResourceID, objects);
        this.context = context;
        optionsList = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.grid_view_items, null);

        TextView textView = convertView.findViewById(R.id.sourceFileHeader);
        ImageView imageView = convertView.findViewById(R.id.sourceFileImage);

        textView.setText(optionsList.get(position).getSourceName());
        imageView.setImageResource(optionsList.get(position).getSourceImage());

        if (position == 0) {
            imageView.setBackground(ContextCompat.getDrawable(context, R.drawable.green_circle_background));
        }

        else if (position == 1) {
            imageView.setBackground(ContextCompat.getDrawable(context, R.drawable.purple_circle_background));
        }

        return convertView;
    }
}
