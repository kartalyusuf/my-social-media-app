package com.kartal.mysocialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {


    //firebase auth
    private FirebaseAuth mAuth ;

    ActionBar actionBar;
    BottomNavigationView navigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        //Actionbar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");


        //init
        mAuth = FirebaseAuth.getInstance();


        //Bottom navigation
        navigationView = findViewById(R.id.navigation);

        //home fragment transaction
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1,"");
        ft1.commit();


        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //Handle item clicks
                switch (item.getItemId()) {

                    case R.id.nav_home:

                        actionBar.setTitle("Home");

                        //home fragment transaction
                        HomeFragment fragment1 = new HomeFragment();
                        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                        ft1.replace(R.id.content, fragment1,"");
                        ft1.commit();

                        return true;

                    case R.id.nav_profile:
                        actionBar.setTitle("Profile");

                        //Profile fragment transaction
                        ProfileFragment fragment2 = new ProfileFragment();
                        FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                        ft2.replace(R.id.content, fragment2,"");
                        ft2.commit();

                        return true;

                    case R.id.nav_users:
                        actionBar.setTitle("Users");

                        UsersFragment fragment3 = new UsersFragment();
                        FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                        ft3.replace(R.id.content,fragment3,"");
                        ft3.commit();

                        return true;

                    case R.id.nav_chat:
                        actionBar.setTitle("Chats");

                        ChatListFragment fragment4 = new ChatListFragment();
                        FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                        ft4.replace(R.id.content,fragment4,"");
                        ft4.commit();
                        return true;

                }

                return false;
            }
        });

    }







    private void CheckUserStatus() {

        //get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            //user is signed in stay here
            //set email of logged in user


        }else {

            //user not signed in, go to main activity

            startActivity(new Intent(DashboardActivity.this,MainActivity.class));
            finish();

        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {

        //Check on start of app

        CheckUserStatus();

        super.onStart();

    }





}