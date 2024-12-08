package com.example.plantify_user.adapter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plantify_user.R;
import com.example.plantify_user.model.AlarmData;
import com.example.plantify_user.plantNotification.AlarmReceiver;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private List<AlarmData> alarms;
    private Context context;

    public AlarmAdapter(List<AlarmData> alarms) {
        this.alarms = alarms;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmData alarm = alarms.get(position);

        holder.nameText.setText(alarm.getName());
        holder.timeText.setText(alarm.getTime());
        holder.alarmSwitch.setChecked(alarm.isActive());

        // Load image using Glide if URL exists
        if (alarm.getImageUrl() != null && !alarm.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(alarm.getImageUrl())
                    .placeholder(R.drawable.image_placeholder_background)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.image_placeholder_background);
        }

        // Handle alarm toggle
        holder.alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            alarm.setActive(isChecked);
            FirebaseDatabase.getInstance()
                    .getReference("alarms")
                    .child(alarm.getId())
                    .child("active")
                    .setValue(isChecked);

            // Update alarm schedule
            if (isChecked) {
                scheduleAlarm(alarm);
            } else {
                cancelAlarm(alarm);
            }
        });
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    private void scheduleAlarm(AlarmData alarm) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e("AlarmAdapter", "AlarmManager is null");
                return;
            }

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("ALARM_ID", alarm.getId());
            intent.putExtra("ALARM_NAME", alarm.getName());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarm.getId().hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            String[] timeParts = alarm.getTime().split(":");
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
            calendar.set(Calendar.SECOND, 0);

            // Schedule for the next day if the time has passed
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Log.d("AlarmAdapter", "Scheduling alarm for: " + calendar.getTime());

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );

        } catch (Exception e) {
            Log.e("AlarmAdapter", "Error scheduling alarm", e);
        }
    }


    private void cancelAlarm(AlarmData alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView timeText;
        ImageView imageView;
        SwitchMaterial alarmSwitch;

        AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.alarmName);
            timeText = itemView.findViewById(R.id.alarmTime);
            imageView = itemView.findViewById(R.id.alarmImage);
            alarmSwitch = itemView.findViewById(R.id.alarmSwitch);
        }
    }
}