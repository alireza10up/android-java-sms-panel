package com.example.myapplication;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;

    private EditText phoneNumberEditText;
    private EditText messageEditText;
    private Button sendButton;
    private Button forwardButton;
    private Button showMessagesButton;
    private Button showContactsButton;
    private SQLiteOpenHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        forwardButton = findViewById(R.id.forwardButton);
        showMessagesButton = findViewById(R.id.showMessagesButton);
        showContactsButton = findViewById(R.id.showContactsButton);

        Intent intent = getIntent();
        String PhoneNumberIntent = intent.getStringExtra("PhoneNumber");
        String TextMessageIntent = intent.getStringExtra("TextMessage");

        if (PhoneNumberIntent != null) {
            phoneNumberEditText.setText(PhoneNumberIntent);
        }
        if (TextMessageIntent != null) {
            messageEditText.setText(TextMessageIntent);
        }

        dbHelper = new SMSDatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSMS();
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forwardSMS();
            }
        });

        showMessagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShowMessagesActivity.class);
                startActivity(intent);
            }
        });

        showContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShowContactsActivity.class);
                startActivity(intent);
            }
        });

        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_SMS
            }, PERMISSION_REQUEST_CODE);
        }
    }

    private void sendSMS() {
        String phoneNumber = phoneNumberEditText.getText().toString();
        String message = messageEditText.getText().toString();

        if (phoneNumber.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number and a message", Toast.LENGTH_SHORT).show();
            return;
        }

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        saveMessage(phoneNumber, message, "sent");

        Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
    }

    private void forwardSMS() {
        Cursor cursor = db.query("messages", null, "status=?", new String[]{"received"}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"));
            String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
            cursor.close();

            messageEditText.setText(message);
            phoneNumberEditText.setText(phoneNumber);
        } else {
            Toast.makeText(this, "No messages to forward", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveMessage(String phoneNumber, String message, String status) {
        ContentValues values = new ContentValues();
        values.put("phone_number", phoneNumber);
        values.put("message", message);
        values.put("status", status);
        db.insert("messages", null, values);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
