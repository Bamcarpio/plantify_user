package com.example.plantify_user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.plantify_user.carts_checkout.carts;
import com.example.plantify_user.chats.chats;
import com.example.plantify_user.feedbacks.Feedbacks;
import com.example.plantify_user.orderDeliveries.Deliveries;

import com.example.plantify_user.plantNotification.AlarmFragment;
import com.example.plantify_user.ui.FirstActivity;
import com.example.plantify_user.userSettings.userSettings;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity {

    private ImageView ImageMenu;
    private NavigationView nav;
    private ImageView imagebutton;
    private DrawerLayout drawer;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nav = findViewById(R.id.home_menu);
        drawer = findViewById(R.id.drawer);
        imagebutton = findViewById(R.id.imageButton);
        firebaseAuth = FirebaseAuth.getInstance();

        setFragment(new home_layout());

        imagebutton.setOnClickListener(v -> {
            drawer.open();
        });

        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemid = menuItem.getItemId();


                if (itemid == R.id.Home) {
                    setFragment(new home_layout());
                    Toast.makeText(Home.this, "Home", Toast.LENGTH_SHORT).show();
                } else if (itemid == R.id.carts) {
                    setFragment(new carts());
                } else if (itemid == R.id.Orders) {
                    setFragment(new Deliveries());
                } else if (itemid == R.id.Chats) {
                    setFragment(new chats());
                } else if (itemid == R.id.FeedBacks) {
                    setFragment(new Feedbacks());
                } else if (itemid == R.id.schedule) {
                    setFragment(new AlarmFragment());
                } else if (itemid == R.id.settings) {
                    setFragment(new userSettings());
                } else if (itemid == R.id.waterPlants) {
                    Intent intent = new Intent(Home.this, FirstActivity.class);
                    startActivity(intent);
                } else if (itemid == R.id.Sign_Out) {
                    signOut();
                }


                drawer.closeDrawer(nav);

                return true;
            }
        });
    }


    private void signOut() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Home.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main, fragment);
        fragmentTransaction.commit();
    }
}

