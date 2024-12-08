package com.example.plantify_user.ui.home;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.plantify_user.R;
import com.example.plantify_user.domain.Plant;
import com.example.plantify_user.ui.PlantFormatter;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private final Context context;
    private final List<Plant> plants;

    // RecyclerAdapter constructor to pass the context
    public RecyclerAdapter(Context context, List<Plant> p) {
        this.context = context;
        plants = p;
    }
    // Class that holds the items to be displayed (Views in card_layout)
    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView plantName;
        private final TextView age;
        private final TextView nextWatering;
        private final ImageView plantImage;
        private final Handler handler = new Handler();
        private final Runnable updateTask;
        private int position;

        public ViewHolder(View itemView) {
            super(itemView);
            plantName = itemView.findViewById(R.id.item_title);
            plantImage = itemView.findViewById(R.id.item_image);
            age = itemView.findViewById(R.id.item_age_value);
            nextWatering = itemView.findViewById(R.id.item_watering_value);

            // Initialize Runnable to update next watering text every second
            updateTask = new Runnable() {
                @Override
                public void run() {
                    // Get the plant object and update the next watering text
                    PlantFormatter plantFormatter = new PlantFormatter(context, plants.get(getBindingAdapterPosition()));
                    nextWatering.setText(plantFormatter.timeToNextWatering());
                    // Schedule the next update after 1 second (1000 milliseconds)
                    handler.postDelayed(this, 1000);
                }
            };

            // What to do when an item is clicked
            itemView.setOnClickListener(v -> {
                position = getBindingAdapterPosition();
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                Navigation.findNavController(itemView)
                        .navigate(R.id.action_nav_home_to_detailsFragment, bundle);
            });
        }

        // Start updating next watering text when the item is bound
        public void startUpdating() {
            handler.post(updateTask);
        }

        // Stop updating next watering text when the item is recycled
        public void stopUpdating() {
            handler.removeCallbacks(updateTask);
        }
    }

    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ViewHolder holder, int position) {
        PlantFormatter plant = new PlantFormatter(context, plants.get(position));
        holder.plantName.setText(plant.name());
        holder.plantImage.setImageBitmap(plant.photo());
        holder.age.setText(plant.age());
        holder.nextWatering.setText(plant.timeToNextWatering());

        // Start the periodic update for next watering text
        holder.startUpdating();
    }

    @Override
    public int getItemCount() {
        return plants.size();
    }

    // Stop updating when the RecyclerView is scrolled and ViewHolder is recycled
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.stopUpdating(); // Remove the scheduled task to avoid memory leaks
    }
}


