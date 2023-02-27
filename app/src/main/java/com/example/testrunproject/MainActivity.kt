package com.example.testrunproject

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.dacontactsyncandroid.Activity.ContactSync
import com.example.dacontactsyncandroid.Activity.PermissionUtil.ContactPermissionHandler
import com.example.dacontactsyncandroid.Activity.service.ContactRoomObject
import java.util.*


public class MainActivity : AppCompatActivity() {
    var button: Button? = null
    var relamdataget: Button? = null
    var listContacts: java.util.ArrayList<ContactRoomObject>? = null
    var listAddContact: java.util.ArrayList<Add>? = null
    var listupdatecontact: java.util.ArrayList<Add>? = null
    var listdeletecontact: java.util.ArrayList<Add>? = null
    var listcontactSync: java.util.ArrayList<ContactSyncBody>? = null
    lateinit var contactSync: ContactSync

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.btn)
        relamdataget = findViewById(R.id.relamdataget)
        listAddContact = ArrayList()
        listdeletecontact = ArrayList()
        listcontactSync = ArrayList()
        listupdatecontact = ArrayList()
        contactSync = ContactSync(this@MainActivity)

        button!!.setOnClickListener {
            contactSync.isContactAccess(
                object : ContactPermissionHandler() {
                    override fun onGranted() {
                        listContacts = contactSync.fetchAllContact()
                        for (i in listContacts!!.indices) {
                            val name = listContacts!![i].contactName
                            val code = listContacts!![i].contactCountryCode
                            val number = listContacts!![i].contactMobileNumber
                            listAddContact!!.add(
                                Add(
                                    code!!.toInt(), name, number!!.toLongOrNull()!!
                                )
                            )
                        }
                        Log.d("TAG", "onCreateVVVVV: ${listAddContact!!.size}")
                    }
                    override fun onDenied() {
                    }

                })

        }


    }


}