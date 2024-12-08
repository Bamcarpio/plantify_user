package com.example.plantify_user.carts_checkout;

import android.content.Intent;
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
import android.widget.Toast;

import com.example.plantify_user.Home;
import com.example.plantify_user.R;
import com.example.plantify_user.adapter.CartAdapter;
import com.example.plantify_user.model.CartModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class carts extends Fragment {
    public carts() {
        // Required empty public constructor
    }
    private RecyclerView ListCartView;
    private ArrayList<CartModel> listCart;
    private MaterialButton backBtn,CheckOutBtn;

    private CartAdapter adapter;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carts, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        String userid = firebaseAuth.getCurrentUser().getUid();

        if(userid == null){
            Toast.makeText(getContext(), "Failed to fetch uid", Toast.LENGTH_SHORT).show();
        }

        ListCartView = view.findViewById(R.id.ListCartView);

        //Buttons
        backBtn = view.findViewById(R.id.backBtn);
        CheckOutBtn = view.findViewById(R.id.CheckOutBtn);

        CheckOutBtn.setOnClickListener(v->{
            setFragment(new checkouts());
        });

        backBtn.setOnClickListener(v->{
            Intent intent = new Intent(getContext(), Home.class);
            startActivity(intent);
        });


        ListCartView.setLayoutManager(new LinearLayoutManager(getContext()));
        listCart = new ArrayList<>();
        adapter = new CartAdapter(getContext(),listCart);
        ListCartView.setAdapter(adapter);

        firebaseDatabase.getReference("Cart").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    for(DataSnapshot ds: snapshot.getChildren()){
                        CartModel cartModel = ds.getValue(CartModel.class);
                        if(cartModel.getUserid().equals(userid)) {
                            listCart.add(cartModel);
                            cartModel.setCartKey(ds.getKey());
                            cartModel.setProductKey(cartModel.getProductKey());
                        }
                        adapter.notifyDataSetChanged();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void setFragment(Fragment fragment){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction =  fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main,fragment);
        fragmentTransaction.commit();

    }
}