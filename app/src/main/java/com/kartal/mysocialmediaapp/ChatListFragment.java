package com.kartal.mysocialmediaapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ChatListFragment extends Fragment {

    private FirebaseAuth mAuth;

    public ChatListFragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);



        //init firebaseauth
        mAuth = FirebaseAuth.getInstance();

        return  view ;
    }

    private void CheckUserStatus() {

        //get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            //user is signed in stay here
            //set email of logged in user

        } else {
            //user not signed in, go to main activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    //Inflate options Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        //hide add post icon
        menu.findItem(R.id.action_add_post).setVisible(false);



        super.onCreateOptionsMenu(menu, inflater);
    }



    //Handle menu options click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //get item id
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            mAuth.signOut();
            CheckUserStatus();
        }


        return super.onOptionsItemSelected(item);
    }
}