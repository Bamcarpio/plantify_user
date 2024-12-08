package com.example.plantify_user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantify_user.R;
import com.example.plantify_user.model.FeedBackModel;

import java.util.ArrayList;

public class FeedBackAdapter  extends RecyclerView.Adapter<FeedBackAdapter.ItemHolder> {
    Context context;
    ArrayList<FeedBackModel>FeedBackList;
   public FeedBackAdapter(Context context, ArrayList<FeedBackModel>FeedBackList){
        this.context = context;
        this.FeedBackList = FeedBackList;
    }
    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feedback_card,parent,false);

        return new ItemHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
       FeedBackModel feedBackModel = FeedBackList.get(position);
       holder.onBind(feedBackModel);

    }

    @Override
    public int getItemCount() {
        return FeedBackList.size();
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        TextView userEmail,userComment;
        public ItemHolder(@NonNull View itemView) {
            super(itemView);


            userEmail = itemView.findViewById(R.id.userEmail);
            userComment = itemView.findViewById(R.id.userComment);

        }
        public void onBind(FeedBackModel feedBackModel){
            userEmail.setText(feedBackModel.getEmail());
            userComment.setText(feedBackModel.getFeedbacks());
        }
   }
}
