package com.hermosaprogramacion.blog.saludmock.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hermosaprogramacion.blog.saludmock.data.api.model.MedicalCenter;

import java.util.List;

/**
 * Adaptador de centros médicos
 */

public class MedicalCenterAdapter extends ArrayAdapter<MedicalCenter> {

    private Context mContext;
    private List<MedicalCenter> mItems;

    public MedicalCenterAdapter(@NonNull Context context, @NonNull List<MedicalCenter> items) {
        super(context, 0, items);
        mContext = context;
        mItems = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        MedicalCenter medicalCenter = mItems.get(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        } else {
            view = convertView;
        }

        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(medicalCenter.getName());

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
