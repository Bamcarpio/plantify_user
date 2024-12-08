package com.example.plantify_user.feedbacks;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.plantify_user.R;
import com.example.plantify_user.adapter.FeedBackAdapter;
import com.example.plantify_user.model.FeedBackModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class  Feedbacks extends Fragment {



    public Feedbacks() {

    }


    private RecyclerView FeedBackList ;
    private FeedBackAdapter adapter;
    private ArrayList<FeedBackModel> listFeedBack;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FloatingActionButton materialButton;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedbacks, container, false);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        FeedBackList = view.findViewById(R.id.FeedBackList);
        materialButton = view.findViewById(R.id.fab);
        FeedBackList.setLayoutManager(new LinearLayoutManager(getContext()));
        listFeedBack = new ArrayList<>();
        adapter = new FeedBackAdapter(getContext(),listFeedBack);
        FeedBackList.setAdapter(adapter);



        firebaseDatabase.getReference("FeedBacks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    FeedBackModel feedBackModel = ds.getValue(FeedBackModel.class);
                    listFeedBack.add(feedBackModel);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        materialButton.setOnClickListener(v->{

            String userid = firebaseAuth.getCurrentUser().getUid();
            String email = firebaseAuth.getCurrentUser().getEmail();

            AlertDialog.Builder feedbackRating = new AlertDialog.Builder(getContext());

            View view1 = LayoutInflater.from(getContext()).inflate(R.layout.comment_feedback,null,false);

            feedbackRating.setView(view1);

            EditText Comment;

            Comment = view1.findViewById(R.id.CommentHere);

            feedbackRating.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Map<String,Object> feedComment = new HashMap<>();
                    feedComment.put("feedbacks",Comment.getText().toString());
                    feedComment.put("email",email);

                    firebaseDatabase.getReference().child("FeedBacks").push().setValue(feedComment);

                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                }
            });
            feedbackRating.show();



        });


        return view;
    }
}