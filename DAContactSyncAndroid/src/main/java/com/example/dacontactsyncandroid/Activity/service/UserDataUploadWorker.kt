package com.example.dacontactsyncandroid.Activity.service

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.os.*
import android.provider.ContactsContract
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters


class UserDataUploadWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private var mContactCount = 0
    private var handler: Handler? = null
    override fun doWork(): Result {
        Log.d("TAG", "overviewService: doWork")

        mContactCount = getContactCount()
        context.contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI, true, mObserver
        )
        return Result.success()
    }
    private fun getContactCount(): Int {
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
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
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            val currentCount = getContactCount()
            if (currentCount < mContactCount) {
                //Log.d("TAG", "overviewService: DELETE HAPPEN")
                // DELETE HAPPEN.
            } else if (currentCount == mContactCount) {
              //  Log.d("TAG", "overviewService: UPDATE HAPPEN")
                // UPDATE HAPPEN.
            } else {
              //  Log.d("TAG", "overviewService: INSERT HAPPEN")
                // INSERT HAPPEN.
            }
            mContactCount = currentCount
        }
    }


}