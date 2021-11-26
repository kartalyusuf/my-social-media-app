package com.kartal.mysocialmediaapp.adapters;


import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kartal.mysocialmediaapp.R;
import com.kartal.mysocialmediaapp.models.ModelChat;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends  RecyclerView.Adapter<AdapterChat.MyHolder>{

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RİGHT = 1;
    Context context;
    List<ModelChat> chatList ;
    String imageUrl ;

    //firebaseuser
    FirebaseUser firebaseUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layouts : row_chat_left.xml for receiver , row_chat_right.xml for sender
        if (viewType == MSG_TYPE_RİGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false) ;
            return new MyHolder(view);

        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false) ;
            return new MyHolder(view);

        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();


        //set data
        holder.messageTv.setText(message);
        holder.timeTv.setText(dateTime);

        try {
            Picasso.get().load(imageUrl).into(holder.profilIv);
        } catch (Exception e) {

        }

        if (position == chatList.size() - 1) {
            if (chatList.get(position).isSeen()) {

                holder.isSeenTv.setText("Seen");
            } else {
                holder.isSeenTv.setText("Delivered");

        }

        }else {
            holder.isSeenTv.setVisibility(View.GONE);
        }



        }


    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) { //we created this
        //get currently signed in user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RİGHT;
        }else {
            return MSG_TYPE_LEFT;
        }

    }

    //View Holder class
    class MyHolder extends RecyclerView.ViewHolder {

        //Views
        ImageView profilIv;
        TextView messageTv , timeTv , isSeenTv ;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            profilIv = itemView.findViewById(R.id.profileIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv=itemView.findViewById(R.id.isSeenTv);


        }
    }
}
