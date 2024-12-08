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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.plantify_user.R;
import com.example.plantify_user.model.ChatModel;

import java.util.List;

public class chatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatModel> chatList;
    private String currentUserId;
    private Context context;

    private static final int VIEW_TYPE_SENDER = 1;
    private static final int VIEW_TYPE_RECEIVER = 2;

    public chatAdapter(Context context, List<ChatModel> chatList, String currentUserId) {
        this.context = context;
        this.chatList = chatList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel message = chatList.get(position);
        if (message.getSender().equals(currentUserId)) {
            return VIEW_TYPE_SENDER;
        } else {
            return VIEW_TYPE_RECEIVER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENDER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatModel message = chatList.get(position);

        if (holder instanceof SenderViewHolder) {
            ((SenderViewHolder) holder).bindSender(message);
        } else if (holder instanceof ReceiverViewHolder) {
            ((ReceiverViewHolder) holder).bindReceiver(message);
        }
    }

    @Override
    public int getItemCount() {
        return chatList != null ? chatList.size() : 0;
    }

    // ViewHolder for sender messages
    static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;
        ImageView imageView;

        SenderViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.sender_message_text);
            timeText = itemView.findViewById(R.id.sender_time_text);
            imageView = itemView.findViewById(R.id.sender_image_view);
        }

        void bindSender(ChatModel message) {
            try {
                if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                    imageView.setVisibility(View.VISIBLE);
                    messageText.setVisibility(View.GONE);
                    Glide.with(itemView.getContext())
                            .load(message.getImageUrl())
                            .error(R.drawable.plant_logo)
                            .placeholder(R.drawable.plant_logo)
                            .into(imageView);
                } else {
                    imageView.setVisibility(View.GONE);
                    messageText.setVisibility(View.VISIBLE);
                    messageText.setText(message.getMessages() != null ? message.getMessages() : "");
                }
                timeText.setText(message.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // ViewHolder for receiver messages
    static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;
        ImageView imageView;

        ReceiverViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.receiver_message_text);
            timeText = itemView.findViewById(R.id.receiver_time_text);
            imageView = itemView.findViewById(R.id.receiver_image_view);
        }

        void bindReceiver(ChatModel message) {
            try {
                if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                    imageView.setVisibility(View.VISIBLE);
                    messageText.setVisibility(View.GONE);
                    Glide.with(itemView.getContext())
                            .load(message.getImageUrl())
                            .into(imageView);
                } else {
                    imageView.setVisibility(View.GONE);
                    messageText.setVisibility(View.VISIBLE);
                    messageText.setText(message.getMessages() != null ? message.getMessages() : "");
                }
                timeText.setText(message.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}