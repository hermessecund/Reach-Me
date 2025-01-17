package com.example.reachme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.example.reachme.Adapters.FragmentsAdapter;
import com.example.reachme.Adapters.UsersAdapter;
import com.example.reachme.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    private FirebaseAuth auth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar2);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager()));
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        manageConnections();
    }

    private void manageConnections() {
        // final DatabaseReference connectionReference = database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        final DatabaseReference connectionReference = database.getReference().child("Users/" + uid + "/connectionStatus");
        final DatabaseReference lastSeen = database.getReference().child("Users/" + uid + "/lastSeen");
        final DatabaseReference infoConnected = database.getReference(".info/connected");
        if (user != null) {
            infoConnected.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {
                        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                        connectionReference.setValue("Online");
                        connectionReference.onDisconnect().setValue("Offline");
                        lastSeen.onDisconnect().setValue(ServerValue.TIMESTAMP);
                    } else {
                        Toast.makeText(MainActivity.this, "Not connected", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Log.w(TAG, "Listener was cancelled");
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                SignOut();
                break;
            case R.id.groupChat:
                Intent intent = new Intent(MainActivity.this, GroupChatActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.settings:
                Intent intent1 = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent1);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void SignOut() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }
}