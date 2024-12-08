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
import com.example.plantify_user.model.DeliveryInfoModel;

import java.util.ArrayList;

public class DeliveryProductInfoAdapter  extends RecyclerView.Adapter<DeliveryProductInfoAdapter.ItemHolder> {

   private Context context;
   private ArrayList<DeliveryInfoModel> ProductList;

    public DeliveryProductInfoAdapter( Context context,ArrayList<DeliveryInfoModel> ProductList){
       this.context = context;
       this.ProductList = ProductList;
    }
    @NonNull
    @Override
    public DeliveryProductInfoAdapter.ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_card,parent,false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryProductInfoAdapter.ItemHolder holder, int position) {
        DeliveryInfoModel deliveryInfoModel = ProductList.get(position);
        holder.onBind(deliveryInfoModel);
    }

    @Override
    public int getItemCount() {
        return ProductList.size();
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {

       private TextView nameproduct,priceproduct,orderquantity;
       private ImageView productImg;
        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            nameproduct = itemView.findViewById(R.id.nameproduct);
            priceproduct = itemView.findViewById(R.id.priceproduct);
            orderquantity = itemView.findViewById(R.id.orderquantity);
            productImg = itemView.findViewById(R.id.productImg);

        }

        public void onBind(DeliveryInfoModel infoModel){

            nameproduct.setText(infoModel.getProductName());
            priceproduct.setText(infoModel.getPrice());
            orderquantity.setText(infoModel.getQuantity());

            Glide.with(itemView.getContext()).load(infoModel.getImageUrl()).into(productImg);

        }
    }
}
