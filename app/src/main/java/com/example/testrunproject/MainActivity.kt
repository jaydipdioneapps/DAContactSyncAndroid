package com.example.testrunproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.dacontactsyncandroid.Activity.Contact
import com.example.dacontactsyncandroid.Activity.ContactSync
import com.example.dacontactsyncandroid.Activity.Preference


public class MainActivity : AppCompatActivity() {
    var button: Button? = null
    var listContacts: java.util.ArrayList<Contact>? = null
    lateinit var contactSync: ContactSync
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.btn)

        contactSync = ContactSync(this@MainActivity)
        button!!.setOnClickListener {
            if(contactSync.isContactAccess()){
                Log.d("TAG", "isContactAccess   permission garanted")
                listContacts=contactSync.fetchAll() //fetch contactlist
                Preference.storeArraylist(this@MainActivity,listContacts)
                // permission garanted
            }else{
                Log.d("TAG", "isContactAccess   permission denied")
                //permission denied
            }
        }

    }
}