package com.example.reachme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.reachme.Adapters.ChatAdapter;
import com.example.reachme.Models.MessageModel;
import com.example.reachme.databinding.ActivityChatsDetailedBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatsDetailedActivity extends AppCompatActivity {

    ActivityChatsDetailedBinding binding;

    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatsDetailedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

       final String senderId = auth.getUid();

        String reciverId = getIntent().getStringExtra("userID");
        String userName = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePic");

        binding.name.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.avatar).into(binding.profileImage);

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatsDetailedActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // chat Adapter set
        ArrayList<MessageModel>messageModels = new ArrayList<>();

        final ChatAdapter chatAdapter = new ChatAdapter(this,messageModels);
        binding.chatsRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.chatsRecyclerView.setLayoutManager(linearLayoutManager);

        // Chatting logic

        final String senderRoom = senderId + reciverId;
        final String reciverRoom = reciverId + senderId;

        // getting chats from database

        database.getReference().child("Chats").child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()){
                            MessageModel model = snapshot1.getValue(MessageModel.class);
                            messageModels.add(model);
                        }
                        chatAdapter.notifyDataSetChanged();
                        //binding.chatsRecyclerView.smoothScrollToPosition(binding.chatsRecyclerView.getAdapter().getItemCount());
                        binding.chatsRecyclerView.scrollToPosition(binding.chatsRecyclerView.getAdapter().getItemCount()-1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // storing chats in database

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = binding.message.getText().toString();
                final MessageModel model = new MessageModel(senderId,message);
                model.setTimeStamp(new Date().getTime());
                binding.message.setText("");

                // storing message in database
                database.getReference().child("Chats").child(senderRoom).push()
                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("Chats").child(reciverRoom).push()
                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                    }
                });
            }
        });

    }
}