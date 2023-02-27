package com.example.dacontactsyncandroid.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.*
import android.telephony.TelephonyManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.content.CursorLoader
import com.example.dacontactsyncandroid.Activity.PermissionUtil.ContactPermissionHandler
import com.example.dacontactsyncandroid.Activity.PermissionUtil.PermissionHandler
import com.example.dacontactsyncandroid.Activity.PermissionUtil.Permissions
import com.example.dacontactsyncandroid.Activity.service.ContactRoomObject
import com.example.dacontactsyncandroid.R
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.util.*
import kotlin.collections.ArrayList


class ContactSync(private val activity: Activity) : AppCompatActivity() {
    private lateinit var phoneNumberUtil: PhoneNumberUtil
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_sync)
    }

    fun isContactAccess(handler: ContactPermissionHandler?
    ) {
        Permissions.check(
            activity,
            Manifest.permission.READ_CONTACTS,
            null,
            object : PermissionHandler() {
                override fun onGranted() {
                    handler!!.onGranted()
                }

                override fun onDenied(
                    context: Context?,
                    deniedPermissions: ArrayList<String?>
                ) {
                    super.onDenied(context, deniedPermissions)
                    handler!!.onDenied()                }

                override fun openSettings() {
                    handler!!.onGranted()
                    Log.d("TAG", "checkcheckcheck isPermissionGranted")
                }
            })
    }


    open fun fetchAll(): ArrayList<Contact> {
        val projectionFields = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
        )
        phoneNumberUtil = PhoneNumberUtil.createInstance(activity)
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
                if(contactDisplayName!=null){
                    val contact = Contact(contactId, contactDisplayName)
                    contactsMap[contactId] = contact
                    getAddress(contactId, contactsMap)
                    listContacts.add(contact)
                }

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


        // appPreferencesHelper = AppPreferencesHelper(applicationContext)
        if(phone.count>0){
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
                    val countryIsoCode = getCountryIsoCode(number)



                   // val phonenum = deleteCountry(number)
                    contact.addNumber(number, phoneType.toString(),countryIsoCode)
                    phone.moveToNext()
                }
            }
            phone.close()
        }
    }
     @SuppressLint("Range")
     fun fetchAllContact(): ArrayList<ContactRoomObject> {
        var countryIso = "ZM"
        val manager = activity.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val networkCountryIso = manager.networkCountryIso.uppercase(Locale.ROOT)
        if (networkCountryIso.isNotEmpty()) {
            countryIso = networkCountryIso
        }
         var listContacts: ArrayList<ContactRoomObject> = ArrayList<ContactRoomObject>()

        val phoneUtil = PhoneNumberUtil.createInstance(activity)
        try {
            val id = ContactsContract.Contacts._ID
            val displayName = ContactsContract.Contacts.DISPLAY_NAME
            val photoUri = ContactsContract.Contacts.PHOTO_URI
            val version = ContactsContract.RawContacts.VERSION

            val cursorContact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                val timeStamp = ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP
               // val contactLastUpdatedTimeStamp = chatDao.getMaxContactLastUpdatedTimeStamp() ?: 0
                val projection = arrayOf(id, displayName, photoUri, timeStamp)
                activity.contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection, "$timeStamp > $0", null, null)

                activity.contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, null)
            } else {
                val projection = arrayOf(id, displayName, photoUri, version)
                activity.contentResolver.query(ContactsContract.RawContacts.CONTENT_URI, projection, null, null, null)
            }
            cursorContact?.let { cursor ->
              //  Timber.i("Modified Contacts %s", cursor.count)
                var syncedContactCount = 0
                if (cursor.count > 0) {
                    val contacts = mutableListOf<ContactRoomObject>()
                    val emails = mutableListOf<ContactRoomObject>()
                    while (cursor.moveToNext()) {
                        val contactId = cursor.getString(cursor.getColumnIndex(id))
                        val name = cursor.getString(cursor.getColumnIndex(displayName))
                        val photo = cursor.getString(cursor.getColumnIndex(photoUri))
                        val contactLastUpdatedTimeStamp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            val timeStamp = ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP
                            cursor.getString(cursor.getColumnIndex(timeStamp)).toLong()
                        } else {
                            0
                        }

                        val contactVersion = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            cursor.getString(cursor.getColumnIndex(version)).toInt()
                        } else {
                            0
                        }

                        val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone._ID)
                        activity.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, phoneProjection,
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?", arrayOf(contactId), null)?.let { phoneCursor ->
                            while (phoneCursor.moveToNext()) {
                                val phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                if (phoneNumber != null && phoneNumber.isNotEmpty()) {

                                  //  Timber.i("phone contact id %s = %s", phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)), phoneNumber)

                                    try {
                                        val nationalNumber = phoneUtil.parse(phoneNumber, countryIso)
                                        if (nationalNumber.countryCode.toString().isNotEmpty()) {
                                            contacts.add(
                                                ContactRoomObject(
                                                contactId = phoneCursor.getString(phoneCursor.getColumnIndex(
                                                    Phone._ID)).toInt(),
                                                contactCountryCode = "+${nationalNumber.countryCode}",
                                                contactMobileNumber = nationalNumber.nationalNumber.toString(),
                                                contactName = name,
                                                contactImage = photo,
                                                contactIsSync = 0,
                                                contactLastUpdatedTimeStamp = contactLastUpdatedTimeStamp,
                                                contactVersion = contactVersion)
                                            )
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            phoneCursor.close()
                        }
                        val emailProjection = arrayOf(ContactsContract.CommonDataKinds.Email._ID, ContactsContract.CommonDataKinds.Email.DATA)
                        activity.contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, emailProjection, "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?", arrayOf(contactId), null)?.let { emailCursor ->
                            while (emailCursor.moveToNext()) {
                                val email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                                if (email != null && email.isNotEmpty()) {

                                //    Timber.i("email contact id %s = %s", emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email._ID)), email)
                                    emails.add(ContactRoomObject(
                                        contactId = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email._ID)).toInt(),
                                        contactEmail = email.replace("'", "\\'"),
                                        contactName = name,
                                        contactImage = photo,
                                        contactIsSync = 0,
                                        contactLastUpdatedTimeStamp = contactLastUpdatedTimeStamp,
                                        contactVersion = contactVersion))
                                }
                            }
                            emailCursor.close()
                        }
                    }
                    Log.d("TAG", "onHandleIntent: ${contacts.size}")
                    Log.d("TAG", "onHandleIntent: ${emails.size}")
                    listContacts = contacts.distinctBy { it.contactMobileNumber } as ArrayList<ContactRoomObject>
                }
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
         return listContacts
    }

    private fun getCountryIsoCode(number: String): String? {
        val validatedNumber = if (number.startsWith("+")) number else "+$number"
        val phoneNumber = try {
            phoneNumberUtil.parse(validatedNumber, null)
        } catch (e: NumberParseException) {
            Log.e("TAG", "error during parsing a number")
            null
        } ?: return ""
        Log.d("TAG", "getCountryIsoCode----: ${phoneNumber.countryCode.toString()}")
        return phoneNumberUtil.getRegionCodeForCountryCode(phoneNumber.countryCode)
      //  return phoneNumber.countryCode
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
        if(postal_cursor.count>0){
            while (postal_cursor.moveToNext()) {
                val contactIdColumnsIndex =
                    postal_cursor.getString(postal_cursor.getColumnIndex(StructuredPostal.CONTACT_ID))
                val poBox: String =
                    postal_cursor.getString(postal_cursor.getColumnIndex(StructuredPostal.POBOX))
                val street: String =
                    postal_cursor.getString(postal_cursor.getColumnIndex(StructuredPostal.STREET))
                val city: String =
                    postal_cursor.getString(postal_cursor.getColumnIndex(StructuredPostal.CITY))
                val state: String =
                    postal_cursor.getString(postal_cursor.getColumnIndex(StructuredPostal.REGION))
                val postalCode: String =
                    postal_cursor.getString(postal_cursor.getColumnIndex(StructuredPostal.POSTCODE))
                val country: String =
                    postal_cursor.getString(postal_cursor.getColumnIndex(StructuredPostal.COUNTRY))
                val type: String =
                    postal_cursor.getString(postal_cursor.getColumnIndex(StructuredPostal.TYPE))

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