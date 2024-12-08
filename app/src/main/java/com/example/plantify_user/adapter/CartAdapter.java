package com.example.plantify_user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plantify_user.R;
import com.example.plantify_user.model.CartModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewItemHolder> {

    Context context;
    ArrayList<CartModel> cartList;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    public CartAdapter(Context context, ArrayList<CartModel> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @NonNull
    @Override
    public ViewItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_card_checkout, parent, false);
        return new ViewItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewItemHolder holder, int position) {
        CartModel cartModel = cartList.get(position);
        holder.onBind(cartModel);
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public class ViewItemHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView productname, totalPrice;
        MaterialButton checkOut;
        String productKey;
        String imageUrl;
        String productDescription;
        String productQuantities;
        String userid;
        String realPrice;
        String cartkey;

        public ViewItemHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.cartproductLogo);
            productname = itemView.findViewById(R.id.cartproductName);
            totalPrice = itemView.findViewById(R.id.cartproductPrice);
            checkOut = itemView.findViewById(R.id.checkOutBtn);

            checkOut.setOnClickListener(v -> {
                // Create a map with all details for checkout
                Map<String, Object> checkoutData = new HashMap<>();
                checkoutData.put("ImageUrl", imageUrl);
                checkoutData.put("Price", realPrice);
                checkoutData.put("ProductDescription", productDescription);
                checkoutData.put("ProductName", productname.getText().toString());
                checkoutData.put("Quantity", productQuantities);
                checkoutData.put("userid", userid);
                checkoutData.put("ProductKey", productKey);

                // Add to "For_CheckOut" node in Firebase
                database.getReference().child("For_CheckOut").child(cartkey).setValue(checkoutData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(itemView.getContext(), "Successfully Added To Check Out", Toast.LENGTH_SHORT).show();

                                // Remove from the Cart after successful addition to "For_CheckOut"
                                database.getReference("Cart").child(cartkey).removeValue()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                // Remove the item from the local list and notify the adapter
                                                cartList.remove(getAdapterPosition());
                                                notifyItemRemoved(getAdapterPosition());
                                                Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(context, "Failed to remove item from cart", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(itemView.getContext(), "Failed To Add To Check Out", Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }

        void onBind(CartModel cartModel) {
            // Retrieve all product details from the model
            String productPrice = cartModel.getPrice();
            String productQuantity = cartModel.getQuantity();

            int price = Integer.parseInt(productPrice);
            int quantity = Integer.parseInt(productQuantity);

            // Assigning values to member variables
            cartkey = cartModel.getCartKey();
            productKey = cartModel.getProductKey();
            imageUrl = cartModel.getImageUrl();
            productDescription = cartModel.getProductDescription();
            productQuantities = cartModel.getQuantity();
            realPrice = cartModel.getPrice();
            userid = cartModel.getUserid();  // Assuming CartModel has a user ID field

            // Loading image and setting text
            Glide.with(itemView.getContext()).load(imageUrl).into(imageView);
            productname.setText(cartModel.getProductName());
            totalPrice.setText("Amount " + price + " x " + quantity + " = PHP " + (price * quantity));
        }
    }
}


