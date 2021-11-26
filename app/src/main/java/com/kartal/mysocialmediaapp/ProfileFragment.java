package com.kartal.mysocialmediaapp;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebStorage;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.HashMap;


public class ProfileFragment extends Fragment {


    //Firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;


    //storage
    StorageReference storageReference;

    //path where images of user profile and cover will be stored
    String storagePath = "Users_Profile_Cover_Imgs/";


    //Views from xml
    ImageView avatarIv, coverIv;
    TextView nameTv, emailTv, phoneTv;
    FloatingActionButton fab;


    //ProgressDialog
    ProgressDialog pd;

    //Permissions contants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    //Arrays of permissons to be requested
    String cameraPermissions[];
    String storagePermissions[];


    //uri of picked image
    Uri image_uri;

    //for checking profile or cover photo
    String profileOrCoverPhoto;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();







        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        //init Views
        avatarIv = view.findViewById(R.id.avatarIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        coverIv = view.findViewById(R.id.coverIv);
        fab = view.findViewById(R.id.fab);

        //init dialog
        pd = new ProgressDialog(getActivity());


        //by using orderbychild querry we will show the detail form a node
        //it will search all nodes , where the key matches it will get its detail
        Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    //Set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    try {

                        //if image recevied then set
                        Picasso.get().load(image).into(avatarIv);


                    } catch (Exception e) {

                        //if there is any exception while getting image then set default
                        Picasso.get().load(R.drawable.ic_default_image_white).into(avatarIv);

                    }

                    try {

                        //if image recevied then set
                        Picasso.get().load(cover).into(coverIv);


                    } catch (Exception e) {

                        //if there is any exception while getting image then set default


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //Floating action bar button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showEditProfileDialog();
            }
        });

        return view;
    }

    private boolean checkStoragePermission() {

        //check srorage result

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission() {
        //request runtime storage permission
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);

    }

    private boolean checkCameraPermission() {

        //check camera result

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {
        //request runtime camera permission
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);

    }

    private void showEditProfileDialog() {
        //* Show dialog containing options
        // 1- Edit Profile Picture
        // 2- Edit Cover Photo
        // 3- Edit Name
        // 4- Edit Phone


        //options to show in dialog
        String options[] = {"Edit Profile Picture", "Edit Cover Photo", "Edit Name", "Edit Phone"};
        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");

        //Set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Handle dialog item clicks


                if (which == 0) {
                    //Edit profile picture click
                    pd.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();


                } else if (which == 1) {
                    //Edit Cover photo
                    pd.setMessage("Updating Cover Photo");
                    profileOrCoverPhoto = "cover";
                    showImagePicDialog();


                } else if (which == 2) {
                    //Edit name
                    pd.setMessage("Updating Name");

                    //calling method and pass key "name" as parameter to update its value in database
                    showNamePhoneUpdateDialog("name");


                } else if (which == 3) {
                    //Edit phone
                    pd.setMessage("Updating Phone");

                    showNamePhoneUpdateDialog("phone");

                }

            }
        });

        //Create and show dialog
        builder.create().show();

    }

    private void showNamePhoneUpdateDialog(String key) {

        //parameter "key" will contain value

        //Custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update" + key); //update name or update phone

        //Set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);


        //Add Edittext
        EditText editText = new EditText(getActivity());
        editText.setHint("Enter" + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //Add Button in dialog to Update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //input text from edit text
                String value = editText.getText().toString().trim();

                //Validate if user has entered something or not
                if (!TextUtils.isEmpty(value)) {

                    pd.show();

                    HashMap<String , Object> result = new HashMap<>();
                    result.put(key,value);

                    databaseReference.child(firebaseUser.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //Updated, dissmis progress
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            //failed , dissmis progress and get and show error message
                            pd.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });



                } else {
                    Toast.makeText(getActivity(), "Please Enter" + key, Toast.LENGTH_SHORT).show();
                }

            }
        });

        //Add Button in dialog to Cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                pd.dismiss();


            }
        });

        //Create and show dialog
        builder.create().show();



    }

    private void showImagePicDialog() {
        //show dialog containing options Camera and Gallery to pick the image

        //options to show in dialog
        String options[] = {"Camera", "Gallery"};
        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");

        //Set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Handle dialog item clicks


                if (which == 0) {
                    //Camera clicked

                    if (!checkCameraPermission()) {

                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }


                } else if (which == 1) {
                    //Gallery clicked

                    if (!checkStoragePermission()) {

                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }


                }
            }
        });

        //Create and show dialog
        builder.create().show();


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //this method called when user press allow or deny from permission request dialog
        //here we will handle permission cases (allowed and denied)

        switch (requestCode) {

            case CAMERA_REQUEST_CODE: {
                //picking from camera , first check if camera and storage permissions allowed or not
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && writeStorageAccepted) {

                        //permissions enable
                        pickFromCamera();

                    } else {
                        //permissions denied

                        Toast.makeText(getActivity(), "Please enable camera & storage permission", Toast.LENGTH_SHORT).show();
                    }

                }

            }
            break;

            case STORAGE_REQUEST_CODE: {

                //picking from Gallery , first check if storage permissions allowed or not
                if (grantResults.length > 0) {

                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAccepted) {

                        //permissions enable
                        pickFromGallery();

                    } else {
                        //permissions denied

                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }

                }
            }
            break;

        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method will be called after picking image from camera or gallery

        if (resultCode == RESULT_OK) {

            if (resultCode == IMAGE_PICK_GALLERY_CODE) {
                //image is picked from gallery , get uri of image
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);


            }
            if (resultCode == IMAGE_PICK_CAMERA_CODE) {
                //image is picked from camera , get uri of image

                uploadProfileCoverPhoto(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri image_uri) {

        //show progress
        pd.show();


        //path and name of image to be stored in firebase storage
        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "" + firebaseUser.getUid();

        StorageReference storageReference2 = storageReference.child(filePathAndName);

        storageReference2.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //image is uploaded to storage , now get it's url and store in user's database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;

                        Uri dowloadUri = uriTask.getResult();

                        //check if image is uploaded or not and url is received(onay)
                        if (uriTask.isSuccessful()) {
                            //image uploaded
                            //add/ update url in user's database
                            HashMap<String, Object> results = new HashMap<>();
                            results.put(profileOrCoverPhoto, dowloadUri.toString());

                            databaseReference.child(firebaseUser.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            //url in database of user is added succesfully
                                            //dismiss progress bar

                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    //Error adding url in database of user
                                    //dismiss progress bar

                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Error Updating Image..!", Toast.LENGTH_SHORT).show();


                                }
                            });


                        } else {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Some error occured please try again !", Toast.LENGTH_SHORT).show();
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //some errors
                        pd.dismiss();
                        Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });


    }

    private void pickFromGallery() {

        //Intent of picking image from device camera

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        //put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);


    }

    private void pickFromCamera() {
        //Pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
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


        super.onCreateOptionsMenu(menu , inflater);
    }

    //Handle menu options click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //get item id
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            CheckUserStatus();
        }

        if (id == R.id.action_add_post) {
            startActivity(new Intent(getActivity() , AddPostActivity.class));

        }



        return super.onOptionsItemSelected(item);
    }

}