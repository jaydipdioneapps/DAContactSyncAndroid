package com.example.dacontactsyncandroid.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.dacontactsyncandroid.R;

public class TestContentObserver extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyContentObserver contentObserver = new MyContentObserver();
        getApplicationContext().getContentResolver().registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI,
            true, 
            contentObserver);
    }

    private class MyContentObserver extends ContentObserver {
        public MyContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange, @Nullable Uri uri) {
            super.onChange(selfChange, uri);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
     //        String ACCOUNT_TYPE = "com.android.account.youraccounttype";
             String WHERE_MODIFIED = "( "+ ContactsContract.RawContacts.DELETED + "=1 OR "+
                    ContactsContract.RawContacts.DIRTY + "=1 )'";

            Cursor c = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                    null,
                    WHERE_MODIFIED,
                    null,
                    null);
            Log.d("tag", "A change has happened");
        }
    }
    public static final String WHERE_MODIFIED1 = "( " + ContactsContract.RawContacts.DELETED + "=1)";

    public void contactAdded(boolean selfChange)
    {
        if (!selfChange) {
            try {
                ContentResolver cr =  getContentResolver ();
                Cursor cursor = cr . query (ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    //moving cursor to last position
                    //to get last element added
                    cursor.moveToLast();
                    String contactName = null, photo = null, contactNumber = null;
                    String id = cursor . getString (cursor.getColumnIndex(ContactsContract.Contacts._ID));

                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr . query (ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = ?", new String[]{ id }, null);
                        if (pCur != null) {
                            pCur.moveToFirst();
                            contactNumber =
                                    pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            contactName =
                                    pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                            //here you will get your contact information


                        }
                        pCur.close();
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}