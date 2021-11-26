package com.kartal.mysocialmediaapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.kartal.mysocialmediaapp.ChatActivity;
import com.kartal.mysocialmediaapp.R;
import com.kartal.mysocialmediaapp.models.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.List;


public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    Context context ;
    List<ModelUser> userList;


    //constructor
    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType ) {
        //inflate layout (row_users.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users,parent , false);


        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get Data

        final String hisUID = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String userEmail = userList.get(position).getEmail();

        //set Data
        holder.mNameTv.setText(userName);
        holder.mEmailTv.setText(userEmail);
        

        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img).into(holder.mAvatarIv);


        }catch (Exception e) {

        }

        //Handle items click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUID);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {

        return userList.size();
    }



    //View holder class //i created
    class MyHolder extends RecyclerView.ViewHolder {

        ImageView mAvatarIv;
        TextView mNameTv , mEmailTv ;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init Views
            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            mEmailTv = itemView.findViewById(R.id.emailTv);




        }
    }
}
