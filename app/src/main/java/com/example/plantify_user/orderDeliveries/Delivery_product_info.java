package com.example.plantify_user.orderDeliveries;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.plantify_user.R;
import com.example.plantify_user.adapter.DeliveryProductInfoAdapter;
import com.example.plantify_user.model.DeliveryInfoModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class Delivery_product_info extends Fragment {

    String orderKey;


    public Delivery_product_info(String orderKey) {

        this.orderKey = orderKey;

    }


    private DeliveryProductInfoAdapter adapter;
    private ArrayList<DeliveryInfoModel>productListInfo;
    private RecyclerView productList;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;



    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_product_info, container, false);


        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();

        String userid = firebaseAuth.getCurrentUser().getUid();


        productList = view.findViewById(R.id.productList);

        productList.setLayoutManager(new LinearLayoutManager(getContext()));
        productListInfo = new ArrayList<>();

        adapter= new DeliveryProductInfoAdapter(getContext(),productListInfo);

        productList.setAdapter(adapter);

        firebaseDatabase.getReference("Orders").child(orderKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productListInfo.clear(); // Clear the list to prevent duplicate entries
                try{
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        DeliveryInfoModel deliveryInfoModel = childSnapshot.getValue(DeliveryInfoModel.class);
                        if (deliveryInfoModel != null) {
                            productListInfo.add(deliveryInfoModel);
                        }
                    }
                }catch(Exception e){
                  e.printStackTrace();
                }


                adapter.notifyDataSetChanged(); // Notify adapter after adding all items
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors, such as logging the error or displaying a message
            }
        });




        return view;
    }
}