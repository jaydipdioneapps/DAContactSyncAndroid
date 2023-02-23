package com.example.dacontactsyncandroid.Activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.content.CursorLoader
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.dacontactsyncandroid.Activity.PermissionUtil.PermissionHandler
import com.example.dacontactsyncandroid.Activity.PermissionUtil.Permissions
import com.example.dacontactsyncandroid.Activity.service.ContactService
import com.example.dacontactsyncandroid.Activity.service.UserDataUploadWorker
import com.example.dacontactsyncandroid.R


class ContactSync(private val activity: Activity) : AppCompatActivity() {
    private var isPermissionGranted: Boolean? = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_sync)
        isPermissionGranted = false
        Log.d("TAG", "overviewService: contacsynconcreatw")

    }


    fun isContactAccess(): Boolean {
        Permissions.check(
            activity,
            Manifest.permission.READ_CONTACTS,
            null,
            object : PermissionHandler() {
                override fun onGranted() {
                    isPermissionGranted = true
                }

                override fun onDenied(
                    context: Context?,
                    deniedPermissions: ArrayList<String?>
                ) {
                    super.onDenied(context, deniedPermissions)
                    isPermissionGranted = false
                }

                override fun openSettings() {
                    isPermissionGranted = true
                    Log.d("TAG", "checkcheckcheck isPermissionGranted")
                }
            })
        return isPermissionGranted!!
    }

    open fun fetchAll(): ArrayList<Contact> {
        val projectionFields = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
        )
        val listContacts: ArrayList<Contact> = ArrayList<Contact>()
        val cursorLoader = CursorLoader(
            activity,
            ContactsContract.Contacts.CONTENT_URI,
            projectionFields,  // the columns to retrieve
            null,  // the selection criteria (none)
            null,  // the selection args (none)
            null // the sort order (default)
        )
        val c: Cursor = cursorLoader.loadInBackground()!!
        val contactsMap: MutableMap<String, Contact> = HashMap<String, Contact>(c.count)
        if (c.moveToFirst()) {
            val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            do {
                val contactId = c.getString(idIndex)
                val contactDisplayName = c.getString(nameIndex)
                val contact = Contact(contactId, contactDisplayName)
                contactsMap[contactId] = contact
                getAddress(contactId, contactsMap)
                listContacts.add(contact)
            } while (c.moveToNext())
        }
        c.close()
        matchContactNumbers(contactsMap)
        matchContactEmails(contactsMap)
        return listContacts
        // AsyncTaskExample(activity, progressdialog,contactData).execute()
    }

    open fun matchContactNumbers(contactsMap: Map<String, Contact>) {
        // Get numbers
        val numberProjection = arrayOf(
            Phone.NUMBER,
            Phone.TYPE,
            Phone.CONTACT_ID
        )
        val phone: Cursor = CursorLoader(
            activity,
            Phone.CONTENT_URI,
            numberProjection,
            null,
            null,
            null
        ).loadInBackground()!!
        if (phone.moveToFirst()) {
            val contactNumberColumnIndex = phone.getColumnIndex(Phone.NUMBER)
            val contactTypeColumnIndex = phone.getColumnIndex(Phone.TYPE)
            val contactIdColumnIndex = phone.getColumnIndex(Phone.CONTACT_ID)
            while (!phone.isAfterLast) {
                val number = phone.getString(contactNumberColumnIndex)
                val contactId = phone.getString(contactIdColumnIndex)
                val contact: Contact = contactsMap[contactId] ?: continue
                val type = phone.getInt(contactTypeColumnIndex)
                val customLabel = "Custom"
                val phoneType = Phone.getTypeLabel(activity.getResources(), type, customLabel)
                contact.addNumber(number, phoneType.toString())
                phone.moveToNext()
            }
        }
        phone.close()
    }

    fun getAddress(contactId: String, contactsMap: MutableMap<String, Contact>) {
        val postal_uri: Uri = StructuredPostal.CONTENT_URI
        val postal_cursor = activity.contentResolver.query(
            postal_uri,
            null,
            ContactsContract.Data.CONTACT_ID + "=" + contactId,
            null,
            null
        )
        Log.d("TAG", "fetchDogResponse fddfgf  : ${postal_cursor!!.count}")

        while (postal_cursor!!.moveToNext()) {
            val contactIdColumnsIndex =
                postal_cursor!!.getString(postal_cursor!!.getColumnIndex(StructuredPostal.CONTACT_ID))
            val poBox: String =
                postal_cursor!!.getString(postal_cursor!!.getColumnIndex(StructuredPostal.POBOX))
            val street: String =
                postal_cursor!!.getString(postal_cursor!!.getColumnIndex(StructuredPostal.STREET))
            val city: String =
                postal_cursor!!.getString(postal_cursor!!.getColumnIndex(StructuredPostal.CITY))
            val state: String =
                postal_cursor!!.getString(postal_cursor!!.getColumnIndex(StructuredPostal.REGION))
            val postalCode: String =
                postal_cursor!!.getString(postal_cursor!!.getColumnIndex(StructuredPostal.POSTCODE))
            val country: String =
                postal_cursor!!.getString(postal_cursor!!.getColumnIndex(StructuredPostal.COUNTRY))
            val type: String =
                postal_cursor!!.getString(postal_cursor!!.getColumnIndex(StructuredPostal.TYPE))

            Log.d("TAG", "fetchDogResponse   : ${poBox}")
            Log.d("TAG", "fetchDogResponse   : ${street}")
            Log.d("TAG", "fetchDogResponse   : ${city}")
            Log.d("TAG", "fetchDogResponse   : ${state}")
            Log.d("TAG", "fetchDogResponse   : ${postalCode}")
            Log.d("TAG", "fetchDogResponse   : ${country}")
            Log.d("TAG", "fetchDogResponse   : ${type}")
            val contact: Contact = contactsMap[contactId] ?: continue
            contact.addAddress(poBox!!, street!!, city!!, state!!, postalCode!!, country!!, type!!)
        }
        postal_cursor!!.close()
    }

    fun matchContactEmails(contactsMap: Map<String, Contact>) {
        // Get email
        val emailProjection = arrayOf(
            Email.DATA,
            Email.TYPE,
            Email.CONTACT_ID
        )
        val email: Cursor = CursorLoader(
            activity,
            Email.CONTENT_URI,
            emailProjection,
            null,
            null,
            null
        ).loadInBackground()!!
        if (email.moveToFirst()) {
            val contactEmailColumnIndex = email.getColumnIndex(Email.DATA)
            val contactTypeColumnIndex = email.getColumnIndex(Email.TYPE)
            val contactIdColumnsIndex = email.getColumnIndex(Email.CONTACT_ID)
            while (!email.isAfterLast) {
                val address = email.getString(contactEmailColumnIndex)
                val contactId = email.getString(contactIdColumnsIndex)
                val type = email.getInt(contactTypeColumnIndex)
                val customLabel = "Custom"
                val contact: Contact = contactsMap[contactId] ?: continue
                val emailType = Email.getTypeLabel(activity.getResources(), type, customLabel)
                contact.addEmail(address, emailType.toString())
                email.moveToNext()
            }
        }
        email.close()
    }

}