package com.example.plantify_user.plantNotification;

import static android.app.Activity.RESULT_OK;
import static java.security.AccessController.getContext;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantify_user.R;
import com.example.plantify_user.adapter.AlarmAdapter;
import com.example.plantify_user.model.AlarmData;
import com.example.plantify_user.plantNotification.AlarmReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AlarmFragment extends Fragment {
    private RecyclerView recyclerView;
    private AlarmAdapter adapter;
    private List<AlarmData> alarms;
    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        // Initialize Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("alarms");
        storageRef = FirebaseStorage.getInstance().getReference("alarm_images");

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        alarms = new ArrayList<>();
        adapter = new AlarmAdapter(alarms);
        recyclerView.setAdapter(adapter);

        // Add new alarm button
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showAddAlarmDialog());

        loadAlarms();
        return view;
    }

    private void showAddAlarmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_alarm, null);

        EditText nameInput = dialogView.findViewById(R.id.nameInput);
        Button timeButton = dialogView.findViewById(R.id.timeButton);
        ImageView imageView = dialogView.findViewById(R.id.imageView);

        timeButton.setOnClickListener(v -> showTimePickerDialog(timeButton));
        imageView.setOnClickListener(v -> openImagePicker());

        builder.setView(dialogView)
                .setTitle("Add New Alarm")
                .setPositiveButton("Save", (dialog, which) -> saveAlarm(nameInput.getText().toString(),
                        timeButton.getText().toString()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showTimePickerDialog(Button timeButton) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute1) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                    timeButton.setText(time);
                },
                hour, minute, true);
        timePickerDialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
        }
    }

    private void saveAlarm(String name, String time) {
        String alarmId = databaseRef.push().getKey();

        if (alarmId == null) {
            Toast.makeText(getContext(), "Failed to generate alarm ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(alarmId + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                AlarmData alarm = new AlarmData(alarmId, name, time, uri.toString());
                                databaseRef.child(alarmId).setValue(alarm)
                                        .addOnSuccessListener(aVoid -> scheduleAlarm(alarm));
                            }));
        } else {
            AlarmData alarm = new AlarmData(alarmId, name, time, null);
            databaseRef.child(alarmId).setValue(alarm)
                    .addOnSuccessListener(aVoid -> scheduleAlarm(alarm));
        }
    }

    private void loadAlarms() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                alarms.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AlarmData alarm = dataSnapshot.getValue(AlarmData.class);
                    if (alarm != null) {
                        alarms.add(alarm);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading alarms", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scheduleAlarm(AlarmData alarm) {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e("AlarmFragment", "AlarmManager is null");
            return;
        }

        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        intent.putExtra("ALARM_ID", alarm.getId());
        intent.putExtra("ALARM_NAME", alarm.getName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                alarm.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String[] timeParts = alarm.getTime().split(":");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
            return;
        }

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );

        Log.d("AlarmFragment", "Alarm scheduled for: " + calendar.getTime());
    }
}
