package com.kartal.mysocialmediaapp.adapters;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kartal.mysocialmediaapp.R;

public class AdapterPosts {


    //view holder class

    class MyHolder extends RecyclerView.ViewHolder {


        //views from row_post xml
        ImageView uPictureIv , pImageIv ;
        TextView uNameTv , pTimeTv , pTitleTv , pDescriptionTv , pLikesTv ;
        ImageButton moreBtn ;
        Button likeBtn , commentBtn , shareBtn ;

        public MyHolder(@NonNull View itemView) {
            super(itemView);


            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.nameTv);
            pTimeTv = itemView.findViewById(R.id.timeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn= itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);




        }
    }


}
