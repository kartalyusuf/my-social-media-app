package com.kartal.mysocialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    ActionBar actionBar ;
    FirebaseAuth mAuth ;
    DatabaseReference userDbRef ;

    //views
    EditText titleEt , descriptionEt ;
    ImageView imageIv ;
    Button uploadBtn ;

    //user info
    String name , email , uid , dp ;


    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100 ;
    private static final int STORAGE_REQUEST_CODE = 200 ;

    //image pick constans
    private static final int IMAGE_PICK_CAMERA_CODE = 300 ;
    private static final int IMAGE_PICK_GALLERY_CODE = 400 ;


    //permissions array
    String[] cameraPermissions;
    String[] storagePermissions;

    Uri image_uri = null ;

    //progress dialog
    ProgressDialog pd;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        //enable back button
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        CheckUserStatus();

        actionBar.setSubtitle(email);

        pd = new ProgressDialog(this);

        //get some info of current user to include in post
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    name = "" +ds.child("name").getValue();
                    email= "" +ds.child("email").getValue();
                    dp = "" +ds.child("image").getValue();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //init views
        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionEt);
        imageIv = findViewById(R.id.pImageIv) ;
        uploadBtn = findViewById(R.id.pUploadBtn) ;

        //init permissions array
        cameraPermissions = new String[] {Manifest.permission.CAMERA , Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};





        //get image from camere/gallery on click
        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialog
                showImagePickDialog();
            }
        });

        //upload button clikc
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get data
                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(AddPostActivity.this, "Enter title...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(AddPostActivity.this, "Enter description...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (image_uri == null) {
                    //post without image
                    uploadData(title ,description , "noImage");
                }
                else {
                    //post with image
                    uploadData(title ,description , String.valueOf(image_uri));


                }

            }
        });

    }

    private void uploadData(String title, String description, String uri) {

        pd.setMessage("Publishing post...");
        pd.show();

        //for post-image name , post- id , post-publish time
        String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post_" + timeStamp ;

        if (!uri.equals("noImage")) {
            //post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putFile(Uri.parse(uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // image is uploaded to firebase storage , now get's url
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) {

                        String dowloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            //url is received upload post to firebase database

                            HashMap<String , Object> hashMap = new HashMap<>() ;
                            //put post info
                            hashMap.put("uid",uid) ;
                            hashMap.put("uName",name) ;
                            hashMap.put("uEmail",email) ;
                            hashMap.put("uDp",dp) ;
                            hashMap.put("pId",timeStamp) ;
                            hashMap.put("pTitle",title) ;
                            hashMap.put("pDescr",description) ;
                            hashMap.put("pImage",dowloadUri) ;
                            hashMap.put("pTime",timeStamp) ;

                            //path to store post data
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts") ;
                            //put data in this ref
                            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //added in database
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();

                                    //reset views
                                    titleEt.setText("");
                                    descriptionEt.setText("");
                                    imageIv.setImageURI(null);
                                    image_uri = null ;

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed adding post in database
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                                }
                            });

                        }
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed uploading image
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });

        }else {
            //post without image

            HashMap<String , Object> hashMap = new HashMap<>() ;
            //put post info
            hashMap.put("uid",uid) ;
            hashMap.put("uName",name) ;
            hashMap.put("uEmail",email) ;
            hashMap.put("uDp",dp) ;
            hashMap.put("pId",timeStamp) ;
            hashMap.put("pTitle",title) ;
            hashMap.put("pDescr",description) ;
            hashMap.put("pImage","noImage") ;
            hashMap.put("pTime",timeStamp) ;

            //path to store post data
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts") ;
            //put data in this ref
            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    //added in database
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();

                    //reset views
                    titleEt.setText("");
                    descriptionEt.setText("");
                    imageIv.setImageURI(null);
                    image_uri = null ;


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed adding post in database
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                }
            });
        }

    }

    private void showImagePickDialog() {
        //options camera/gallery to show in dialog

        String [] options = {"Camera" , "Gallery"} ;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image From") ;
        //set options dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //item click handle
                if (which == 0) {
                    //camera clicked

                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    }else {
                        PickFromCamera();
                    }
                }
                if (which == 1 ) {
                    //gallery clicked

                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    }else {
                        PickFromGallery();

                    }
                }

            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void PickFromGallery() {
        //intent to pick image from gallery

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);



    }

    private void PickFromCamera() {
        //intent to pick image from camera

        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE , "Temp Pick") ;
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);


    }

    private boolean checkStoragePermission() {

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);


        return result;
    }

    private void requestStoragePermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);

    }

    private boolean checkCameraPermission() {

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {
        //request runtime camera permission
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);

    }

    @Override
    protected void onStart() {
        CheckUserStatus();
        super.onStart();
    }

    @Override
    protected void onResume() {
        CheckUserStatus();
        super.onResume();
    }

    private void CheckUserStatus() {

        //get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            //user is signed in stay here
            email = user.getEmail();
            uid = user.getUid();



        } else {
            //user not signed in, go to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go to previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main , menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

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

    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //both permissons are granted
                        PickFromCamera();
                    } else {
                        // camera or gallery permissions were denied
                        Toast.makeText(this, "Camera & Gallery both permissions are neccessary", Toast.LENGTH_SHORT).show();

                    }
                } else {

                }

                break;

            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {

                        PickFromGallery();
                    } else {
                        Toast.makeText(this, "Storage permission neccessary", Toast.LENGTH_SHORT).show();
                    }
                } else {

                }



                break;
        }



        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method will be called after picking image from camera or gallery
        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();

                //set to imageview
                imageIv.setImageURI(image_uri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE) {


                imageIv.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}