package com.kartal.mysocialmediaapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kartal.mysocialmediaapp.adapters.AdapterChat;
import com.kartal.mysocialmediaapp.models.ModelChat;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

   //views from xml
    Toolbar toolbar ;
    RecyclerView recyclerView ;
    ImageView profileIv;
    TextView nameTv , userStatusTv ;
    EditText messageEt ;
    ImageButton sendBtn ;



    List<ModelChat> chatList ;
    AdapterChat adapterChat ;

    //firebase auth
    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;


    String hisUid ;
    String myUid ;
    String hisImage ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //init Views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        recyclerView = findViewById(R.id.chat_rcyclerView);
        profileIv = findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);

        //Layout (Linearlayout) for recyclerview
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //recyclerview proporties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        //init firebaseauth
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbRef = firebaseDatabase.getReference("Users");

        //search user to get that user's info
        Query userQuery = userDbRef.orderByChild("uid").equalTo(hisUid);
        //get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required info is received
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String name = ""+ ds.child("name").getValue();
                    hisImage= ""+ ds.child("image").getValue();


                    //set data
                    nameTv.setText(name);

                    try {
                        //image is received , set it to imageview in toolbar
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_image_white).into(profileIv);


                    }catch (Exception e) {
                        //there is exception getting picture , set default picture
                        Picasso.get().load(R.drawable.ic_default_image_white).into(profileIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //click button to send message
        sendBtn.setOnClickListener(v -> {
            //get text from edittext
            String message = messageEt.getText().toString().trim();

            //check if text is empty or not
            if (TextUtils.isEmpty(message)) {
                //text empty
                Toast.makeText(ChatActivity.this, "Cannot send the empty message", Toast.LENGTH_SHORT).show();
            }else {
                //text not empty
                sendMessage(message);
            }

        });

        readMessages();


    }



    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    assert chat != null;
                    if ((!chat.getReceiver().equals(myUid) || !chat.getSender().equals(hisUid)) &&
                            (!chat.getReceiver().equals(hisUid) || !chat.getSender().equals(myUid))) {
                    } else {
                        chatList.add(chat);

                    }

                    //adapter
                    adapterChat = new AdapterChat(ChatActivity.this, chatList , hisImage);
                    adapterChat.notifyDataSetChanged();
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterChat);



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String , Object> hashMap = new HashMap<>() ;
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isSeen",false);

        databaseReference.child("Chats").push().setValue(hashMap);

        //reset edittext after sending message
        messageEt.setText("");

    }


    private void CheckUserStatus() {

        //get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            //user is signed in stay here
            //set email of logged in user

            myUid = user.getUid(); //currently signed in user's uid

        } else {
            //user not signed in, go to main activity
            startActivity(new Intent(ChatActivity.this, MainActivity.class));
            finish();
        }
    }




    @Override
    protected void onStart() {
        CheckUserStatus();
        //set online

        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();



    }

    @Override
    protected void onResume() {


        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main , menu);
        //hide searchview , as we dont need it here
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            CheckUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}
