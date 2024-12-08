package com.example.plantify_user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantify_user.R;
import com.example.plantify_user.model.ListCommentModel;

import java.util.ArrayList;

public class CommentAdapter  extends RecyclerView.Adapter<CommentAdapter.ItemHolder>  {



   private Context context;
   private ArrayList<ListCommentModel> commentList;

    public CommentAdapter(Context context,ArrayList<ListCommentModel> commentList){
            this.context = context;
            this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentAdapter.ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_comment,parent,false);

        return new ItemHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ItemHolder holder, int position) {
                ListCommentModel listCommentModel = commentList.get(position);
                holder.onBind(listCommentModel);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
    public static class ItemHolder extends RecyclerView.ViewHolder {
        TextView userEmail, userComment, userStars; // Separate TextViews for Stars and Comment

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            userEmail = itemView.findViewById(R.id.userEmail);
            userComment = itemView.findViewById(R.id.Comment); // Corresponds to the actual comment
            userStars = itemView.findViewById(R.id.Stars);     // Corresponds to the star rating
        }

        public void onBind(ListCommentModel listCommentModel) {
            userEmail.setText(listCommentModel.getEmail());
            userComment.setText(listCommentModel.getComment());
            userStars.setText(listCommentModel.getStars()); // Display stars correctly
        }
    }

}
