package com.example.plantify_user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plantify_user.R;
import com.example.plantify_user.model.ScheduleModel;

import java.util.ArrayList;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ItemHolder> {

    private Context context;
    private ArrayList<ScheduleModel> SchedList;

    public ScheduleAdapter(Context context,ArrayList<ScheduleModel> SchedList){
        this.context = context;
        this.SchedList = SchedList;
    }

    @NonNull
    @Override
    public ScheduleAdapter.ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sched_card,parent,false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleAdapter.ItemHolder holder, int position) {
        ScheduleModel model = SchedList.get(position);
        holder.onBind(model);

    }

    @Override
    public int getItemCount() {
        return SchedList.size();
    }
    public class ItemHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView schedTime,schedDate;
        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.schedImg);
            schedDate = itemView.findViewById(R.id.schedDate);
            schedTime = itemView.findViewById(R.id.schedTime);

        }

        public void onBind(ScheduleModel scheduleModel){
            Glide.with(itemView.getContext()).load(scheduleModel.getImageUrl()).into(imageView);
            schedTime.setText(scheduleModel.getTime());
            schedDate.setText(scheduleModel.getDate());
        }
    }
}
