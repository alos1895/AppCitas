package com.hermosaprogramacion.blog.saludmock.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.hermosaprogramacion.blog.saludmock.R;
import com.hermosaprogramacion.blog.saludmock.data.api.model.Doctor;
import com.hermosaprogramacion.blog.saludmock.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador de doctores y sus horarios
 */

public class DoctorSchedulesAdapter extends
        RecyclerView.Adapter<DoctorSchedulesAdapter.DoctorSchedulesViewHolder> {
    private final Context context;
    private List<Doctor> doctors;
    private final OnItemListener listener;

    public interface OnItemListener {
        void onBookingButtonClicked(Doctor bookedDoctor, String timeScheduleSelected);
    }

    public DoctorSchedulesAdapter(Context context, List<Doctor> doctors, OnItemListener listener) {
        this.context = context;
        this.doctors = doctors;
        this.listener = listener;
    }

    public void setDoctors(List<Doctor> doctors) {
        this.doctors = doctors;
        notifyDataSetChanged();
    }


    @Override
    public DoctorSchedulesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DoctorSchedulesViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(DoctorSchedulesViewHolder holder, int position) {
        holder.bind(doctors.get(position));
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    public class DoctorSchedulesViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView specialtyView;
        private final TextView descriptionView;
        private final Spinner scheduleView;
        private final Button bookingButton;

        public DoctorSchedulesViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.schedule_item_list, parent, false));

            nameView = (TextView) itemView.findViewById(R.id.name_text);
            specialtyView = (TextView) itemView.findViewById(R.id.specialty_text);
            descriptionView = (TextView) itemView.findViewById(R.id.description_text);
            scheduleView = (Spinner) itemView.findViewById(R.id.time_schedules_menu);
            bookingButton = (Button) itemView.findViewById(R.id.booking_button);

            bookingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        String selectedItem = (String) scheduleView.getSelectedItem();
                        listener.onBookingButtonClicked(doctors.get(position), selectedItem);
                    }
                }
            });
        }

        public void bind(Doctor doctor) {
            // Formatos para lista de tiempos disponibles
            List<String> formatedTimes = new ArrayList<>();
            for (String time : doctor.getAvailabilityTimes()) {
                formatedTimes.add(DateTimeUtils.formatTimeForUi(time));
            }

            nameView.setText(doctor.getName());
            specialtyView.setText(doctor.getSpecialty());
            descriptionView.setText(doctor.getDescription());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_list_item_1, formatedTimes);
            scheduleView.setAdapter(adapter);
        }
    }
}
