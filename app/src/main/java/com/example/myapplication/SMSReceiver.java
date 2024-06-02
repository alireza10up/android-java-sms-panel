package com.example.myapplication;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        if (pdus != null) {
            for (Object pdu : pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                String phoneNumber = smsMessage.getDisplayOriginatingAddress();
                String message = smsMessage.getDisplayMessageBody();

                SMSDatabaseHelper dbHelper = new SMSDatabaseHelper(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("phone_number", phoneNumber);
                values.put("message", message);
                values.put("status", "received");
                db.insert("messages", null, values);

                Toast.makeText(context, "New SMS received from: " + phoneNumber, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
