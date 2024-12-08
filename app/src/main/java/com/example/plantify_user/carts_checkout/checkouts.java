package com.example.plantify_user.carts_checkout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantify_user.R;
import com.example.plantify_user.adapter.CheckOutAdapter;
import com.example.plantify_user.model.CheckOutModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class checkouts extends Fragment {
    private RecyclerView ListCartView;
    private ArrayList<CheckOutModel> CheckList;
    private CheckOutAdapter adapter;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private TextView totalprices;
    private MaterialButton checkOutBtn;

    // Flag to prevent multiple checkouts
    private boolean isOrdering = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.from(container.getContext())
                .inflate(R.layout.fragment_checkouts, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        String userid = firebaseAuth.getCurrentUser().getUid();

        ListCartView = view.findViewById(R.id.ListCartView);
        totalprices = view.findViewById(R.id.TotalPrice);
        checkOutBtn = view.findViewById(R.id.checkOutBtn);

        ListCartView.setLayoutManager(new LinearLayoutManager(getContext()));
        CheckList = new ArrayList<>();
        adapter = new CheckOutAdapter(getContext(), CheckList);
        ListCartView.setAdapter(adapter);

        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(mMessageReceiver, new IntentFilter("MyTotalAmount"));

        // Set onClickListener for the checkout button
        checkOutBtn.setOnClickListener(v -> {
            if (!isOrdering && !CheckList.isEmpty()) {  // Proceed if not ordering and cart is not empty
                isOrdering = true;  // Set the flag to indicate order processing
                checkOutBtn.setEnabled(false); // Disable the button to prevent multiple clicks
                OrderItems(userid); // Call the order function
            } else if (CheckList.isEmpty()) {
                Toast.makeText(getContext(), "Your cart is empty!", Toast.LENGTH_SHORT).show();
            }
        });
        // Fetch data once using single value event listener
        firebaseDatabase.getReference("For_CheckOut")
                .orderByChild("userid")
                .equalTo(userid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        CheckList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            CheckOutModel checkOutModel = ds.getValue(CheckOutModel.class);
                            if (checkOutModel != null) {
                                checkOutModel.setCartKey(ds.getKey());
                                CheckList.add(checkOutModel);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        return view;
    }
    private void OrderItems(String userid) {
        String referenceId = String.valueOf(System.currentTimeMillis());

        List<Map<String, Object>> orderedItems = new ArrayList<>();
        for (CheckOutModel item : CheckList) {
            Map<String, Object> orderItem = new HashMap<>();
            orderItem.put("ProductKey", item.getProductKey());
            orderItem.put("ProductName", item.getProductName());
            orderItem.put("Quantity", item.getQuantity());
            orderItem.put("Price", item.getPrice());
            orderItem.put("ImageUrl", item.getImageUrl());
            orderItem.put("ProductDescription", item.getProductDescription());
            orderedItems.add(orderItem);

            // Remove item from checkout after order in Firebase
            firebaseDatabase.getReference("For_CheckOut").child(item.getCartKey()).removeValue();
        }
        // Clear the local CheckList and notify the adapter to update the RecyclerView
        CheckList.clear();
        adapter.notifyDataSetChanged();  // This updates the RecyclerView UI

        Map<String, Object> orderInfo = new HashMap<>();
        orderInfo.put("userid", userid);
        orderInfo.put("status", "For Review");

        firebaseDatabase.getReference("Orders").child(referenceId).setValue(orderedItems).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update order status to 'For Review' in Firebase
                firebaseDatabase.getReference("Orders").child(referenceId).updateChildren(orderInfo);
                Toast.makeText(getContext(), "Order Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to Initialize", Toast.LENGTH_SHORT).show();
            }

            // Reset the flag and re-enable the button after processing is complete
            isOrdering = false;
            checkOutBtn.setEnabled(true);
        });
    }
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int totalBill = intent.getIntExtra("totalAmount", 0);
            totalprices.setText("PHP " + totalBill); // Added currency symbol
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(mMessageReceiver);
    }
}
