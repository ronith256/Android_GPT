package com.lucario.gpt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MessageView extends AppCompatActivity {
    RecyclerView recyclerView;
    EditText messageEditText;
    ImageButton sendButton;
    List<Message> messageList;

    JSONArray messageArray;
    MessageAdapter messageAdapter;
    public static String api_key = null;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();

    private File chatFile;
    private Chat chat;

    private List<Chat> mChatList;
//    static boolean firstPrompt = true;


    @Override
    protected void onStart() {
        super.onStart();
        if(api_key == null){
            api_key = getSharedPreferences("apiKey", MODE_PRIVATE).getString("api","");
            if(!api_key.contains("Bearer")){
                SettingsDialogFragment dialog = new SettingsDialogFragment();
                dialog.show(getSupportFragmentManager(), "SettingsDialogFragment");
            }
        }
        api_key = getSharedPreferences("apiKey", MODE_PRIVATE).getString("api","");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_view);
        chat = (Chat) getIntent().getSerializableExtra("chat");
        mChatList = (List<Chat>) getIntent().getSerializableExtra("chatList");
        chatFile = chat.getChatArray();
        messageList = new ArrayList<>();
        messageArray = new JSONArray();
        loadChatList(chatFile);
        recyclerView = findViewById(R.id.recycler_view);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);

        //setup recycler view
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        sendButton.setOnClickListener((v)->{
            String question = messageEditText.getText().toString().trim().toLowerCase(Locale.ROOT);
            addToChat(question,Message.SENT_BY_ME);
            messageEditText.setText("");
            callAPI(question);
        });
    }

    void addToChat(String message,String sentBy){
        runOnUiThread(() -> {
            messageList.add(new Message(message,sentBy));
            saveChatList(chatFile.getName(), messageList);
            messageAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        });
    }

    private void saveChatList(String name, List saveList) {
        // Write the chat list to a file
        try {
            FileOutputStream fos = openFileOutput(name, MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(saveList);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadChatList(File name) {
        // Read the chat list from a file
        try {
            FileInputStream fis = openFileInput(name.getName());
            ObjectInputStream ois = new ObjectInputStream(fis);
            messageList = (ArrayList<Message>) ois.readObject();
            ois.close();
            fis.close();
            createMessageArray();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
//            Toast.makeText(this, "Load failed", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Initialize the chat list if it doesn't exist
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
    }

    void addResponse(String response, boolean failed){
        JSONObject obj = new JSONObject();
        try {
            obj.put("role", "assistant");
            obj.put("content", response);
            messageArray.put(obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        messageList.remove(messageList.size()-1);
        if(failed){
            addToChat(response,Message.FAILED_RESPONSE);
        } else {
            addToChat(response,Message.SENT_BY_BOT);
        }
        if(response.length() > 20){
            chat.setLatestChat(response.substring(0, 17) + "...");
        } else {
            chat.setLatestChat(response);
        }
        mChatList.set(chat.getChatId()-1, chat);
        saveChatList("chat_list.ser", mChatList);
    }

    void createMessageArray() throws JSONException {
        for(int i = 0; i < messageList.size(); i++){
            JSONObject obj = new JSONObject();
            if(messageList.get(i).sentBy.equals("bot")){
                obj.put("role", "assistant");
            } else if(messageList.get(i).sentBy.equals("me")){
                obj.put("role", "user");
            }
            else {
                continue;
            }
            obj.put("content", messageList.get(i).message);
            messageArray.put(obj);
        }
    }
    void callAPI(String question){
        messageList.add(new Message("Typing... ",Message.SENT_BY_BOT));
        if(chat.getChatName().equals("null") || !(chat.getChatName().length() > 1)){
            if(question.length() > 15){
                chat.setChatName(question.substring(0,15));}
            else{
                chat.setChatName(question);}
            mChatList.set(chat.getChatId()-1, chat);
            saveChatList("chat_list.ser", mChatList);
        }
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model","gpt-3.5-turbo");
            JSONObject obj = new JSONObject();
            obj.put("role","user");
            obj.put("content",question);
            messageArray.put(obj);
            System.out.println(messageArray.toString());
            jsonBody.put("messages",messageArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization",api_key)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to "+e.getMessage().toString(), true);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    JSONObject  jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        addResponse(result.trim(), false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    addResponse("Failed to load response due to "+response.body().string(), true);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(MessageView.this, MainActivity.class));
    }
}