package com.kartal.mysocialmediaapp;import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kartal.mysocialmediaapp.adapters.AdapterUsers;
import com.kartal.mysocialmediaapp.models.ModelUser;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUser> userList;

    private FirebaseAuth mAuth;


    public UsersFragment() {
        //required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        //init firebaseauth
        mAuth = FirebaseAuth.getInstance();

        //init recyclerview
        recyclerView = view.findViewById(R.id.users_recyclerView);
        //set it's proporties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init userList
        userList = new ArrayList<>();

        //getAll users
        getAllUsers();


        return view;


    }

    private void getAllUsers() {
        //get current users
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "Users" containing users info
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        //get all data from path
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //get all users except currentl signed in user
                    if (!modelUser.getUid().equals(firebaseUser.getUid())) {
                        userList.add(modelUser);
                    }

                    //Adapter
                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterUsers);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });

    }

    private void searchUsers(String query) {
        //get current users
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "Users" containing users info
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        //get all data from path
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //get all searched users except currentl signed in user
                    if (!modelUser.getUid().equals(firebaseUser.getUid())) {

                        if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                                modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            userList.add(modelUser);
                        }
                    }

                    //Adapter
                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    //refresh adapter
                    adapterUsers.notifyDataSetChanged();
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterUsers);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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



        //SearchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        //Search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button from keyboard

                //if search querry is not empty then search
                if (!TextUtils.isEmpty(query.trim())) {
                    //search text contains , search it
                    searchUsers(query);


                } else {
                    //search text empty , get all users
                    getAllUsers();

                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                //called whenever user press any single letter
                //if search querry is not empty then search
                if (!TextUtils.isEmpty(query.trim())) {
                    //search text contains , search it
                    searchUsers(query);


                } else {
                    //search text empty , get all users
                    getAllUsers();

                }


                return false;
            }
        });


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