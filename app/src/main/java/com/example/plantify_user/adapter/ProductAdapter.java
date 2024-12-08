package com.example.plantify_user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plantify_user.R;
import com.example.plantify_user.model.ProductModel;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private ArrayList<ProductModel> productList;
    private Context context;
    private onItemClickListener onItemClickListener;

    public ProductAdapter(Context context, ArrayList<ProductModel> productList) {
        this.productList = productList;
        this.context = context;
    }

    public void updateList(ArrayList<ProductModel> filteredList) {
        this.productList.clear();
        this.productList.addAll(filteredList);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface onItemClickListener {
        void OnClick(ProductModel productModel);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_items, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        ProductModel productModel = productList.get(position);

        Glide.with(context).load(productModel.getImageUrl()).into(holder.productImageView);
        holder.productName.setText(productModel.getProductName());
        holder.productPrice.setText("₱" + productModel.getPrice());

        holder.itemView.setOnClickListener(v -> onItemClickListener.OnClick(productModel));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView productImageView;
        TextView productName, productPrice;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImg);
            productName = itemView.findViewById(R.id.productname);
            productPrice = itemView.findViewById(R.id.productprice);
        }
    }
}
