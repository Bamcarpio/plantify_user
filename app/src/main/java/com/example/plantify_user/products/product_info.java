package com.example.plantify_user.products;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.plantify_user.R;
import com.example.plantify_user.adapter.CommentAdapter;
import com.example.plantify_user.home_layout;
import com.example.plantify_user.model.ListCommentModel;
import com.example.plantify_user.model.ProductModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class product_info extends Fragment {


    String productKey;
    public product_info(String productKey) {
        this.productKey = productKey;
    }
    private  FirebaseDatabase firebaseDatabase;
    private  ImageView productImg, ratingProduct,AddCartBtn,backbtn;
    private  TextView productName,productPrice,productDesc, productCategory;
    private  MaterialButton Buy ;
    private  RatingBar ratingBar;
    private  RecyclerView Comments;
    private FirebaseAuth firebaseAuth;
    private String ProductRating,TotalUserRating,keyProduct;
    private CommentAdapter adapter;
    private ArrayList<ListCommentModel> commentList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_info, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        String userid = firebaseAuth.getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        // Products Details
        productName = view.findViewById(R.id.productName);
        productCategory = view.findViewById(R.id.productCategory);
        productPrice = view.findViewById(R.id.productPrice);
        productDesc = view.findViewById(R.id.productDesc);
        productImg = view.findViewById(R.id.productImg);
        backbtn = view.findViewById(R.id.backbtn);
        //Product Rating Stars
        ratingBar = view.findViewById(R.id.ratingBar);
        //Product Comments not yet Implent A RecyclerView
        Comments = view.findViewById(R.id.ListComment);
        //Cart Button
        AddCartBtn = view.findViewById(R.id.AddCart);
        // Buy Button
        Buy = view.findViewById(R.id.BuyBtn);
        //Rating Product
        ratingProduct = view.findViewById(R.id.ratingProduct);
        //display comments
        Comments.setLayoutManager(new LinearLayoutManager(getContext()));
        commentList = new ArrayList<>();
        adapter = new CommentAdapter(getContext(),commentList);
        Comments.setAdapter(adapter);
        firebaseDatabase.getReference("Products").child(productKey).child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ListCommentModel listCommentModel = ds.getValue(ListCommentModel.class);
                    commentList.add(listCommentModel);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //Bar Rating
        ratingBar.getRating();

        backbtn.setOnClickListener(v->{
            replaceFragment(new home_layout());
        });
        AddCartBtn.setOnClickListener(v->{
            getproduct();
        });
        Buy.setOnClickListener(v->{
            BuyProduct();
        });
        ratingProduct.setOnClickListener(v->{
            addCommentRatingProduct(keyProduct);
        });
        return view ;
    }
    private void BuyProduct() {
        AlertDialog.Builder AddCart = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.product_qty_layout, null, false);
        AddCart.setView(view);

        TextInputEditText Quantity = view.findViewById(R.id.QuantityCart);
        AddCart.setTitle("How Many");

        AddCart.setPositiveButton("Proceed", (dialogInterface, i) -> {
            String Qty = Quantity.getText().toString();
            if (Qty.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a quantity", Toast.LENGTH_SHORT).show();
                return;
            }
            firebaseDatabase.getReference("Products").child(productKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ProductModel productModel = snapshot.getValue(ProductModel.class);
                    if (productModel == null) return;
                    String userid = firebaseAuth.getCurrentUser().getUid();
                    String key = snapshot.getKey();
                    Map<String, Object> Description = new HashMap<>();
                    Description.put("ImageUrl", productModel.getImageUrl());
                    Description.put("Price", productModel.getPrice());
                    Description.put("ProductDescription", productModel.getProductDescription());
                    Description.put("ProductName", productModel.getProductName());
                    Description.put("Quantity", Qty);
                    replaceFragment(new confirm_order(Description, userid, key));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        AddCart.show();
    }
    private void getproduct (){
        Context context = getContext();
        if (context == null) return;
        AlertDialog.Builder AddCart = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.product_qty_layout,null,false);
        AddCart.setView(view);
        TextInputEditText Quantity;
        Quantity = view.findViewById(R.id.QuantityCart);
        AddCart.setTitle("Add To Cart");
        AddCart.setPositiveButton("Add To Cart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                firebaseDatabase.getReference("Products").child(productKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ProductModel productModel = snapshot.getValue(ProductModel.class);
                        String userid = firebaseAuth.getCurrentUser().getUid();
                        String Qty = Quantity.getText().toString();
                        String key = snapshot.getKey().toString();
                        String UniqueId = String.valueOf(System.currentTimeMillis());
                        Map<String,Object> Description = new HashMap<>();
                        Description.put("ImageUrl",productModel.getImageUrl());
                        Description.put("Price",productModel.getPrice());
                        Description.put("ProductDescription",productModel.getProductDescription());
                        Description.put("ProductName",productModel.getProductName());
                        Description.put("Quantity", Qty);
                        Description.put("userid",userid);
                        if(!Qty.isEmpty()){
                            firebaseDatabase.getReference("Cart").child(UniqueId).setValue(Description).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(getContext(), "Add To Cart", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(getContext(), "Failed to Add Cart", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(getContext(), "Filled the Quantities", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AddCart.show();
    }
    private void DisplayProductInfo(){
        firebaseDatabase.getReference("Products").child(productKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ProductModel productModel = snapshot.getValue(ProductModel.class);
                String totalRatings = productModel.getTotalRating() == null ? "0" : productModel.getTotalRating();
                String productRatings = productModel.getProductRating() == null ? "0" : productModel.getProductRating();
                ProductRating = productRatings;
                TotalUserRating = totalRatings;
                keyProduct = snapshot.getKey();
                int ratingstotal = totalRatings.isEmpty() ? 1 : Integer.parseInt(totalRatings);
                int ratingProducts = productRatings.isEmpty() ? 1 : Integer.parseInt(productRatings);

                Glide.with(getActivity())
                        .load(productModel.getImageUrl())
                        .error(R.drawable.plant_logo)
                        .placeholder(R.drawable.plant_logo)
                        .into(productImg);
                productName.setText(productModel.getProductName());
                productCategory.setText(productModel.getCategory());
                productPrice.setText(productModel.getPrice());
                productDesc.setText(productModel.getProductDescription());
                ratingBar.setRating((float) ratingProducts / ratingstotal);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void addCommentRatingProduct(String ProductId) {
        String userid = firebaseAuth.getCurrentUser().getUid();
        String email = firebaseAuth.getCurrentUser().getEmail();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.product_rating_comment, null, false);
        alertDialogBuilder.setView(view);

        EditText Comment = view.findViewById(R.id.CommentHere);
        EditText Rating = view.findViewById(R.id.productRating);

        alertDialogBuilder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            String ratingInput = Rating.getText().toString().trim();
            String commentInput = Comment.getText().toString();

            // Validate rating input
            if (ratingInput.isEmpty() || !ratingInput.matches("\\d+")) { // Ensure numeric input
                Toast.makeText(getContext(), "Please enter a valid numeric rating", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int ratingValue = Integer.parseInt(ratingInput);
                if (ratingValue < 1 || ratingValue > 5) { // Validate range
                    Toast.makeText(getContext(), "Rating must be between 1 and 5", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Convert numerical rating to stars
                String stars = new String(new char[ratingValue]).replace("\0", "★") +
                        new String(new char[5 - ratingValue]).replace("\0", "☆");

                // Parse existing ratings
                int totalRating = TotalUserRating != null && !TotalUserRating.isEmpty()
                        ? Integer.parseInt(TotalUserRating) : 0;
                int productRating = ProductRating != null && !ProductRating.isEmpty()
                        ? Integer.parseInt(ProductRating) : 0;
                // Update ratings
                totalRating += 1;
                productRating += ratingValue;

                Map<String, Object> RatingUpdate = new HashMap<>();
                RatingUpdate.put("ProductRating", String.valueOf(productRating));
                RatingUpdate.put("TotalRating", String.valueOf(totalRating));

                firebaseDatabase.getReference("Products").child(ProductId).updateChildren(RatingUpdate)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Rating updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to update rating", Toast.LENGTH_SHORT).show();
                            }
                        });
                // Add comment with stars
                Map<String, Object> Comments = new HashMap<>();
                Comments.put("Comment", commentInput.isEmpty() ? "No comment provided" : commentInput);
                Comments.put("Stars", stars); // Save stars instead of raw numbers
                Comments.put("Email", email);
                Comments.put("Userid", userid);

                firebaseDatabase.getReference("Products").child(ProductId).child("Comments").push()
                        .setValue(Comments)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Comment added successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to add comment", Toast.LENGTH_SHORT).show();
                            }
                        });

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid rating entered", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialogBuilder.show();
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main,fragment);
        fragmentTransaction.commit();

    }
    @Override
    public void onStart() {
        super.onStart();
        DisplayProductInfo();
    }

}

