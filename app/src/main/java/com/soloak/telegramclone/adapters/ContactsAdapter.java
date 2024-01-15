package com.soloak.telegramclone.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.soloak.telegramclone.R;

public class ContactsAdapter extends CursorAdapter {

    public ContactsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate the list item layout
        View view = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false);
        // Create a new view holder and store it in the view tag
        ViewHolder holder = new ViewHolder();
        holder.name = view.findViewById(R.id.contact_name);
        holder.number = view.findViewById(R.id.contact_number);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Get the view holder from the view tag
        ViewHolder holder = (ViewHolder) view.getTag();
        // Get the contact name and number from the cursor
        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        // Set the text views with the contact data
        holder.name.setText(name);
        holder.number.setText(number);
    }

    // A static class to hold the views of each list item
    static class ViewHolder {
        TextView name;
        TextView number;
    }
}
