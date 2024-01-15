package com.soloak.telegramclone.models;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.soloak.telegramclone.models.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactsUtils {

    public static List<Contact> getDeviceContacts(Context context) {
        List<Contact> contactsList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        // Projection for the columns you want to retrieve
        String[] projection = {
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        // Querying the contacts database
        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        );

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                @SuppressLint("Range") String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                // Trim and remove non-numeric characters from the phone number
                phoneNumber = phoneNumber.replaceAll("[^0-9]", "");

                Contact contact = new Contact(name, phoneNumber);
                contactsList.add(contact);
            }
            cursor.close();
        } else {
            Log.d("ContactsUtils", "No contacts found on the device.");
        }

        return contactsList;
    }
}
