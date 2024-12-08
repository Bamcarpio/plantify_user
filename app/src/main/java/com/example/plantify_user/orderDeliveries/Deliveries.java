package com.example.plantify_user.orderDeliveries;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.plantify_user.R;
import com.example.plantify_user.adapter.DeliveryAdapter;
import com.example.plantify_user.model.OrderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class Deliveries extends Fragment {

    public Deliveries() {
        // Required empty public constructor
    }

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private DeliveryAdapter adapter;
    private ArrayList<OrderModel> orderList;
    private RecyclerView deliveryList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deliveries, container, false);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        String userid = firebaseAuth.getCurrentUser().getUid();

        deliveryList = view.findViewById(R.id.deliveryList);
        deliveryList.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        adapter = new DeliveryAdapter(getContext(), orderList, userid);
        deliveryList.setAdapter(adapter);

        // Attach listener to Firebase
        firebaseDatabase.getReference("Orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear(); // Clear the list to avoid duplicates
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        // Attempt to map the snapshot to OrderModel
                        OrderModel orderModel = ds.getValue(OrderModel.class);

                        // Validate the data and add to list if it matches the user
                        if (orderModel != null && userid.equals(orderModel.getUserid())) {
                            orderModel.setKey(ds.getKey()); // Store Firebase key
                            orderList.add(orderModel);
                        }
                    } catch (DatabaseException e) {
                        Log.e("FirebaseError", "Error parsing data: " + e.getMessage());
                    }
                }
                adapter.notifyDataSetChanged(); // Notify adapter about data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Database error: " + error.getMessage());
            }
        });

        adapter.setOnItemClickListener(new DeliveryAdapter.onItemClickListener() {
            @Override
            public void onClick(OrderModel orderModel) {
                ProductInfo(new Delivery_product_info(orderModel.getKey()));
            }
        });

        return view;
    }

    private void ProductInfo(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main, fragment);
        fragmentTransaction.commit();
    }
}
