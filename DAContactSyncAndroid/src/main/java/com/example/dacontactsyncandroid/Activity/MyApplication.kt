package com.example.dacontactsyncandroid.Activity

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.ContactsContract
import android.util.Log
import com.example.dacontactsyncandroid.Activity.EabProvider.EabContactSyncController
import com.example.dacontactsyncandroid.Activity.service.ContactService


open class MyApplication : Application() {
    private var mContactCount = 0
    //var mEabContactSyncController:EabContactSyncController ?=null
    val WHERE_MODIFIED1 = "( " + ContactsContract.RawContacts.DELETED + "=1)"
    var listContacts: java.util.ArrayList<Contact>? = null
    private val NOT_INIT_LAST_UPDATED_TIME = -1
    private val LAST_UPDATED_TIME_KEY = "eab_last_updated_time"
    private var mRefreshContactList: List<Uri>? = null
    override fun onCreate() {
        super.onCreate()
        Log.d("TAG", "overviewService: MyApplication")
      //  listContacts=Preference.readArraylist(this@MyApplication)
      //  mEabContactSyncController =EabContactSyncController()
        if (this.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            mContactCount = getContactCount()
            this.contentResolver.registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI,
                true,
                mObserver
            )

        }

       // readContacts()

//        val compressionWork = OneTimeWorkRequest.Builder(UserDataUploadWorker::class.java)
//        WorkManager.getInstance().enqueue(compressionWork.build())
       // startContactSync()
    }
    private fun getContactCount(): Int {
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null,
                null
            )
            return cursor?.count ?: 0
        } catch (ignore: Exception) {
        } finally {
            cursor?.close()
        }
        return 0
    }

    private val mObserver: ContentObserver = object : ContentObserver(Handler()) {
     /*   override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
           // mRefreshContactList = mEabContactSyncController!!.syncContactToEabProvider(this@MyApplication)
            Log.e("TAG", "onChange:checkdata vff     "+mRefreshContactList!!.size )
        }*/
        override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
            super.onChange(selfChange, uri, flags)

            val currentCount = getContactCount()
            if (currentCount < mContactCount) {
               // contactDelete()
                Log.d("TAG", "overviewService: DELETE HAPPEN")
            // DELETE HAPPEN.
            } else if (currentCount == mContactCount) {
                Log.d("TAG", "overviewService: UPDATE HAPPEN")
                // UPDATE HAPPEN.
            } else {
                Log.d("TAG", "overviewService: INSERT HAPPEN")
                // INSERT HAPPEN.
              //  contactAdded(selfChange)
            }
            mContactCount = currentCount
        }

    }


    fun hasHoneycomb(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
    }
 /*   fun contactDelete() {
        val cr = contentResolver
        val cur = cr.query(
            ContactsContract.RawContacts.CONTENT_URI,
            null,
            WHERE_MODIFIED1,
            null,
            null)
        if (cur!!.count > 0) {
            while (cur.moveToNext()) {
                val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                var phone: String? = null
                //if (!(Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)) {
                println("name : $name, ID : $id")

                // get the phone number
                val pCur = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null
                )!!
                while (pCur.moveToNext()) {
                    phone = pCur.getString(
                        pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    )
                    println("phone$phone")
                }
                pCur.close()
                //}
                if (id != null) {
                   // contactList.add(Contact(name, phone!!, id))
                }
                Log.d("TAG", "overviewService:bbb contactName de   : ${name}")
                Log.d("TAG", "overviewService:bbb contactNumber de   : ${phone}")
            }
        }
        cur.close()
    }*/

    fun contactAdded(selfChange: Boolean) {
        if (!selfChange) {
            try {
                val cr = this.contentResolver
                val cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
                if (cursor != null && cursor.count > 0) {
                    //moving cursor to last position
                    //to get last element added
                    cursor.moveToLast()
                    var contactName: String? = null
                    val photo: String? = null
                    var contactNumber: String? = null
                    val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    if (cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                            .toInt() > 0
                    ) {
                        val pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        if (pCur != null) {
                            pCur.moveToFirst()
                            contactNumber =
                                pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            contactName =
                                pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                            Log.d("TAG", "overviewService:bbb contactNumber : ${contactNumber}")
                            Log.d("TAG", "overviewService:bbb contactName : ${contactName}")

                            //here you will get your contact information
                        }
                        pCur!!.close()
                    }
                    cursor.close()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun startContactSync() {
        Log.d("TAG", "overviewService: myapp")
        val service = Intent(this, ContactService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(service)
        } else {
            this.startService(service)
        }
    }
}