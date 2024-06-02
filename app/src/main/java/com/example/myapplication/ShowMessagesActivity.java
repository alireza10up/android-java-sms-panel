package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ShowMessagesActivity extends AppCompatActivity {

    private ListView messagesListView;
    private SQLiteOpenHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_messages);

        messagesListView = findViewById(R.id.messagesListView);
        dbHelper = new SMSDatabaseHelper(this);
        db = dbHelper.getWritableDatabase();


        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) messagesListView.getItemAtPosition(i);
                Intent intent = new Intent(ShowMessagesActivity.this,MainActivity.class);
                intent.putExtra("TextMessage",item.split(":")[1].split("\\(")[0]);
                startActivity(intent);
            }
        });


        loadMessages();
    }

    private void loadMessages() {
        ArrayList<String> messagesList = new ArrayList<>();
        Cursor cursor = db.query("messages", null, null, null, null, null, "id DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"));
                String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                messagesList.add(phoneNumber + ": " + message + " (" + status + ")");
            }
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messagesList);
        messagesListView.setAdapter(adapter);
    }
}
