package com.example.chatsapp.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatsapp.MessageActivity;
import com.example.chatsapp.Model.ChatModel;
import com.example.chatsapp.Model.UserModel;
import com.example.chatsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<ChatModel> chatModelList;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private FirebaseUser user;
    private String phone;

    public MessageAdapter(List<ChatModel> chatModelList,String phone) {
        this.chatModelList = chatModelList;
        this.phone = phone;
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatModelList.get(position).getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_LEFT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        ChatModel chat = chatModelList.get(position);
        holder.message.setText(chat.getMessage());
        holder.phone.setText(phone);
        if (position == chatModelList.size()-1){
            if (chat.isIsseen()){
                holder.seen.setText("seen");
            }else{
                holder.seen.setText("Delivered");
            }
        }else{
            holder.seen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return chatModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView profile;
        private TextView message;
        private TextView phone;
        private TextView seen;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.msg);
            phone = itemView.findViewById(R.id.phone);
            seen = itemView.findViewById(R.id.seen);
        }
    }
}
