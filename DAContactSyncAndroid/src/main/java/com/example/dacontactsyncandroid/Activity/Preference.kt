package com.example.dacontactsyncandroid.Activity

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Preference {
    fun storeArraylist(context: Context, listContacts: java.util.ArrayList<Contact>?) {
        val sharedPreferences = context.getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(listContacts)
        editor.putString("contactSync", json)
        editor.commit()
    }

    fun readArraylist(context: Context): ArrayList<Contact> {
        val sharedPreferences =
            context.getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("contactSync", "")
        val type =
            object : TypeToken<ArrayList<Contact?>?>() {}.type
        return gson.fromJson(json, type)
    }
}