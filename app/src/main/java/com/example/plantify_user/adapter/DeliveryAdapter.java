package com.example.plantify_user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantify_user.R;
import com.example.plantify_user.model.OrderModel;
import com.example.plantify_user.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.ItemHolder> {
    private ArrayList<OrderModel> orderList;
    private Context context;
    private  onItemClickListener onItemClickListener;
    private String userid;
    public void setOnItemClickListener(onItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface  onItemClickListener {

         void onClick(OrderModel orderModel);

    }
    public DeliveryAdapter( Context context, ArrayList<OrderModel> orderList,String userid){
        this.orderList = orderList;
        this.context = context;
        this.userid = userid;
    }
    @NonNull
    @Override
    public DeliveryAdapter.ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_card,parent,false);
        return new ItemHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull DeliveryAdapter.ItemHolder holder, int position) {
        OrderModel orderModel = orderList.get(position);
        holder.itemView.setOnClickListener(v-> onItemClickListener.onClick(orderList.get(position)));
        holder.onBind(orderModel,userid);

    }
    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static  class ItemHolder extends RecyclerView.ViewHolder{
        private TextView orderId,orderStatus,orderName,orderAddress;
        private FirebaseDatabase firebaseDatabase;
        private FirebaseAuth firebaseAuth;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            firebaseDatabase = FirebaseDatabase.getInstance();
            orderId = itemView.findViewById(R.id.orderId);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderName = itemView.findViewById(R.id.orderName);
            orderAddress = itemView.findViewById(R.id.orderAddress);

        }
        public void onBind(OrderModel orderModel,String userid){
                orderId.setText(orderModel.getKey());
                orderStatus.setText(orderModel.getStatus());

                firebaseDatabase.getReference("Users").child(userid).addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                   UserModel userModel = snapshot.getValue(UserModel.class);
                   orderName.setText(userModel.getUsername());
                   orderAddress.setText(userModel.getAddress());

               }
               @Override
               public void onCancelled(@NonNull DatabaseError error) {

               }
           });
        }
    }
}
