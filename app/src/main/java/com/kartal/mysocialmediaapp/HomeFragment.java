package com.kartal.mysocialmediaapp;

import android.content.Intent;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kartal.mysocialmediaapp.adapters.AdapterPosts;
import com.kartal.mysocialmediaapp.models.ModelPost;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {


    FirebaseAuth mAuth ;
    RecyclerView recyclerView ;
    List<ModelPost> postList ;
    AdapterPosts adapterPosts ;



    public HomeFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //init firebaseauth
        mAuth = FirebaseAuth.getInstance();

        //recyclerview and its properties
        recyclerView = view.findViewById(R.id.postsRecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);


        //init postlist
        postList = new ArrayList<>();

        loadPosts();

        return view ;
    }

    private void loadPosts() {
        //patch of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //get all data from ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    postList.add(modelPost);

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(),postList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //in case of error
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPosts(String searchQuery) {

        //patch of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //get all data from ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    if (modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        postList.add(modelPost);
                    }


                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(),postList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //in case of error
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
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

            startActivity(new Intent(getActivity(),MainActivity.class));
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
    public void onCreateOptionsMenu(Menu menu , MenuInflater inflater) {

        //inflating menu
        inflater.inflate(R.menu.menu_main,menu);

        //searchview to search posts by post title/description
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button
                if (!TextUtils.isEmpty(query)) {
                    searchPosts(query);
                }else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(query)) {
                    searchPosts(query);
                }else {
                    loadPosts();
                }

                return false;
            }
        });

        super.onCreateOptionsMenu(menu , inflater);
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
        if (id == R.id.action_add_post) {
            startActivity(new Intent(getActivity() , AddPostActivity.class));

        }


        return super.onOptionsItemSelected(item);
    }
}