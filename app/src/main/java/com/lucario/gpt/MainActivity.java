package com.lucario.gpt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements ChatAdapter.onClick{

    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    private List<Chat> mChatList;

    private FloatingActionButton newChatButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Objects.requireNonNull(getSupportActionBar()).hide();
        mRecyclerView = findViewById(R.id.chat_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageButton settingsButton = findViewById(R.id.toolbar_settings);
        settingsButton.setOnClickListener(e->{
            SettingsDialogFragment dialog = new SettingsDialogFragment();
            dialog.show(getSupportFragmentManager(), "SettingsDialogFragment");
        });

        newChatButton = findViewById(R.id.fab_new_chat);
        mChatList = new ArrayList<>();
        loadChatList();

        newChatButton.setOnClickListener(e->{
            Chat chat = new Chat(mChatList.size()+1, 0, "null", "null", new File(String.valueOf(mChatList.size()+1)));
           mChatList.add(chat);
           saveChatList();
           Intent intent = new Intent(MainActivity.this, MessageView.class);
           intent.putExtra("chat", chat);
           intent.putExtra("chatList", (Serializable) mChatList);
           startActivity(intent);
           finish();
        });
        mAdapter = new ChatAdapter(this, mChatList, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void saveChatList() {
        // Write the chat list to a file
        try {
            FileOutputStream fos = openFileOutput("chat_list.ser", MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mChatList);
            oos.close();
            fos.close();
//            Toast.makeText(this, "Chat list saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
//            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadChatList() {
        // Read the chat list from a file
        try {
            FileInputStream fis = openFileInput("chat_list.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            mChatList = (ArrayList<Chat>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
//            Toast.makeText(this, "Load failed", Toast.LENGTH_SHORT).show();
        }

        // Initialize the chat list if it doesn't exist
        if (mChatList == null) {
            mChatList = new ArrayList<>();
        }
    }

    @Override
    public void clicked(int position) {
        Chat item = mChatList.get(position);
        // Create an Intent to start a new activity
        Intent intent = new Intent(MainActivity.this, MessageView.class);
        intent.putExtra("chat", item);
        intent.putExtra("chatList", (Serializable) mChatList);
        // Start the new activity
        startActivity(intent);
    }

}