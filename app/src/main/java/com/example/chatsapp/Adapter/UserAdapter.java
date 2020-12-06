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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<UserModel> userModelList;
    private boolean inChat;
    private String lastMessage;

    public UserAdapter(List<UserModel> userModelList, boolean inChat) {
        this.userModelList = userModelList;
        this.inChat = inChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.name.setText(userModelList.get(position).getTitle());
        if (userModelList.get(position).getImage().equals("default")) {
            holder.profile.setImageResource(R.drawable.ic_action_name);
        } else {
            Glide.with(holder.itemView.getContext()).load(userModelList.get(position).getImage()).into(holder.profile);
        }
        if (position == userModelList.size() - 1) {
            holder.divider.setVisibility(View.INVISIBLE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }

        if (inChat){
            lastMessages(userModelList.get(position).getUserId(),holder.last_msg);
        }else{
            holder.last_msg.setVisibility(View.GONE);
        }
        if (inChat) {
            if (userModelList.get(position).getStatus().equals("online")) {
                holder.status.setVisibility(View.VISIBLE);
            }else{
                holder.status.setVisibility(View.GONE);
            }
        } else {
            holder.status.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), MessageActivity.class);
                intent.putExtra("userid", userModelList.get(position).getUserId());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView profile;
        private TextView name;
        private View divider;
        private CircleImageView status;
        private TextView last_msg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.user_profile);
            name = itemView.findViewById(R.id.user_name);
            divider = itemView.findViewById(R.id.divider);
            status = itemView.findViewById(R.id.online_staus);
            last_msg = itemView.findViewById(R.id.last_msg);

        }

    }

    private void lastMessages(final String userid, final TextView lastMsg){
        lastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatModel chatModel = snapshot.getValue(ChatModel.class);
                    if (chatModel.getReceiver().equals(firebaseUser.getUid()) && chatModel.getSender().equals(userid) ||
                            chatModel.getReceiver().equals(userid) && chatModel.getSender().equals(firebaseUser.getUid())) {
                        lastMessage = chatModel.getMessage();
                    }
                }
                switch (lastMessage){
                    case "default":
                        lastMsg.setVisibility(View.GONE);
                        break;
                    default:
                        lastMsg.setVisibility(View.VISIBLE);
                        lastMsg.setText(lastMessage);
                        break;
                }
                lastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
