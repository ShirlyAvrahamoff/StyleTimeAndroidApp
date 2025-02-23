package com.example.styletimeandroidapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.styletimeandroidapp.R;
import com.example.styletimeandroidapp.models.Treatment;

import java.util.List;

public class TreatmentAdapter extends ArrayAdapter<Treatment> {

    private final Context context;
    private final List<Treatment> treatments;

    public TreatmentAdapter(@NonNull Context context, List<Treatment> treatments) {
        super(context, 0, treatments);
        this.context = context;
        this.treatments = treatments;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent); // תצוגה של הפריט שנבחר
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent); // תצוגת הפריטים ברשימה
    }

    private View createView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_treatment, parent, false);
        }

        Treatment treatment = treatments.get(position);

        TextView treatmentName = convertView.findViewById(R.id.treatmentName);
        ImageView treatmentIcon = convertView.findViewById(R.id.treatmentIcon);

        treatmentName.setText(treatment.getName());
        treatmentIcon.setImageResource(R.drawable.ic_scissors);

        return convertView;
    }
}
