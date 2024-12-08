package com.example.plantify_user.products;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantify_user.R;
import com.example.plantify_user.home_layout;
import com.example.plantify_user.model.userData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class confirm_order extends Fragment {



    Map<String,Object> productConfirm;

    String userid;
    String productKey;


    public confirm_order(Map<String,Object> productConfirm,String userid,String productKey) {

        this.productConfirm = productConfirm;
        this.userid = userid;
        this.productKey = productKey;

    }

    private ImageView productLogo;
    private TextView productName,productPrice,totalAmount;
    private MaterialButton confirmBtn,rejectBtn;

    //For Shipping address Component Text
    private TextView  username,userAddress,userZip,userPhone;

    private FirebaseDatabase firebaseDatabase;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm_order, container, false);


        firebaseDatabase = FirebaseDatabase.getInstance();

        //Product Logo
        productLogo = view.findViewById(R.id.productLogo);

        //TextView
        productName = view.findViewById(R.id.productName);
        productPrice = view.findViewById(R.id.productPrice);
        totalAmount = view.findViewById(R.id.totalAmount);

        // Shipping address
        username = view.findViewById(R.id.username);
        userAddress = view.findViewById(R.id.userAddress);
        userZip = view.findViewById(R.id.userZip);
        userPhone = view.findViewById(R.id.userPhone);

        //ButtonsBtn);
        rejectBtn = view.findViewById(R.id.rejectBtn);
        confirmBtn = view.findViewById(R.id.confirmBtn);

        String amount = productConfirm.get("Price").toString();
        String Quantity = productConfirm.get("Quantity").toString();
        String nameProduct = productConfirm.get("ProductName").toString();


        int Aamount = Integer.parseInt(amount);
        int Aquantity = Integer.parseInt(Quantity);

        productName.setText(nameProduct);
        productPrice.setText("PHP" + Aamount + "X" +Aquantity +"          " + "PHP" + Aamount*Aquantity);
        totalAmount.setText("Amount" +"        "+ "PHP" + Aamount*Aquantity );


        confirmBtn.setOnClickListener(v->{
            confirmOrder();
        });
        rejectBtn.setOnClickListener(v->{
                cancelOrder();
        });

        DisplayShippingInfo();


        return  view;
    }

    private void confirmOrder(){

        String uidpackage = String.valueOf(System.currentTimeMillis());

        Map<String,Object> orderInfo = new HashMap<>();
        orderInfo.put("userid",userid);
        orderInfo.put("status","For Review");


        firebaseDatabase.getReference("Orders").child(uidpackage).setValue(orderInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    firebaseDatabase.getReference("Orders").child(uidpackage).child(productKey).updateChildren(productConfirm);
                    Toast.makeText(getContext(), "Order Confirmed", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(), "Failed to Confirmed Orders", Toast.LENGTH_SHORT).show();
                }
            }
        });

        AlertDialog.Builder orderConfirm = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.confirm_dialog,null,false);

        orderConfirm.setView(view);

        orderConfirm.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        orderConfirm.show();

        replaceFragment(new home_layout());


    }


    private void cancelOrder(){

        replaceFragment(new product_info(productKey));
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main,fragment);
        fragmentTransaction.commit();
    }


    private void DisplayShippingInfo(){

        firebaseDatabase.getReference("Users").child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userData userdata = snapshot.getValue(userData.class);

                username.setText(userdata.getUsername());
                userAddress.setText(userdata.getAddress());
                userZip.setText(userdata.getZipcode());
                userPhone.setText(userdata.getContact());



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



}