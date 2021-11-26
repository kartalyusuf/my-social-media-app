package com.kartal.mysocialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText RegisterMail , RegisterPassword ;
    Button mRegisterbtn;
    TextView mHaveAccount ;

    ProgressDialog progressDialog ;


    //Firebase Auth
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        //init
        RegisterMail = findViewById(R.id.register_mail);
        RegisterPassword= findViewById(R.id.register_password);
        mRegisterbtn = findViewById(R.id.register_create_account);
        mHaveAccount = findViewById(R.id.have_accountTv);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");

        //init firebaseauth
        mAuth = FirebaseAuth.getInstance();

        //Handle register btn click
        mRegisterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //input email, password
                String email = RegisterMail.getText().toString().trim();
                String password = RegisterPassword.getText().toString().trim();

                //validate(dogrulama)

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set Error and focus to email edittext
                    RegisterMail.setError("Invalid Email");
                    RegisterMail.setFocusable(true);

                }else if (password.length()<6) {
                    //set Error and focus to password edittext
                    RegisterPassword.setError("Password lenght at least 6 characters");
                    RegisterPassword.setFocusable(true);


                }else {

                    registerUser(email,password) ; //register the user
                }

            }
        });


        //Handle login TextView
        mHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();


            }
        });


        //Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");

        //enable back buttun
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

    }



    private void registerUser(String email, String password) {

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    progressDialog.dismiss();
                    FirebaseUser user = mAuth.getCurrentUser();

                    //Get user email and uid from auth
                    String email = user.getEmail();
                    String uid = user.getUid();


                    //When user is registered store user info firebase realtime database too
                    //Using Hashmap
                    HashMap<Object, String> hashMap = new HashMap<>();
                    hashMap.put("email",email);
                    hashMap.put("uid",uid);
                    hashMap.put("name","");
                    hashMap.put("phone","");//will add later (then edit profile)
                    hashMap.put("image","");
                    hashMap.put("cover","");

                    //FirebaseDatabase instance
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    //path to store user data named "Users"
                    DatabaseReference reference = database.getReference("Users");
                    //put database within hashmap in database
                    reference.child(uid).setValue(hashMap);





                    Toast.makeText(RegisterActivity.this, "Registered...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                    finish();

                }else {

                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();  //go previous activity
        return super.onSupportNavigateUp();
    }
}