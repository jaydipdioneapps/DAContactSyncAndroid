package com.example.dacontactsyncandroid.Activity.PermissionUtil

import android.Manifest
import android.R
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log

@TargetApi(Build.VERSION_CODES.M)
class PermissionsActivity : Activity() {
    var noRationale = true
    var allpermissionDone = true
    private var allPermissions: ArrayList<String?>? = null
    private var deniedPermissions: ArrayList<String?>? = null
    private var noRationaleList: ArrayList<String?>? = null
    private var options: Permissions.Options? = null
    @TargetApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(false)
        val intent = intent
        if (intent == null || !intent.hasExtra(EXTRA_PERMISSIONS)) {
            finish()
            return
        }
        window.statusBarColor = 0
        allPermissions = intent.getSerializableExtra(EXTRA_PERMISSIONS) as ArrayList<String?>?
        options = intent.getSerializableExtra(EXTRA_OPTIONS) as Permissions.Options?
        if (options == null) {
            options = Permissions.Options()
        }
        deniedPermissions = ArrayList()
        noRationaleList = ArrayList()
        verifyallPermissions()
        val rationale = intent.getStringExtra(EXTRA_RATIONALE)
        if (noRationale || TextUtils.isEmpty(rationale)) {
            Permissions.log("No rationale.")
            requestPermissions(toArray(deniedPermissions), RC_PERMISSION)
        } else {
            Permissions.log("Show rationale.")
            showRationale(rationale)
        }
    }
    private fun verifyallPermissions(){
        for (permission in allPermissions!!) {
            if (checkSelfPermission(permission!!) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions!!.add(permission)
                if (shouldShowRequestPermissionRationale(permission)) {
                    noRationale = false
                } else {
                    noRationaleList!!.add(permission)
                }
            }
        }
        if (deniedPermissions!!.isEmpty()) {
            grant()
            return
        }
    }

    private fun showRationale(rationale: String?) {
        val listener = DialogInterface.OnClickListener { dialog, which ->
            if (which == DialogInterface.BUTTON_POSITIVE) {
                requestPermissions(toArray(deniedPermissions), RC_PERMISSION)
            } else {
                deny()
            }
        }
        AlertDialog.Builder(this).setTitle(options!!.rationaleDialogTitle)
            .setMessage(rationale)
            .setPositiveButton(R.string.ok, listener)
            .setNegativeButton(R.string.cancel, listener)
            .setOnCancelListener { deny() }.create().show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (grantResults.size == 0) {
            deny()
        } else {
            deniedPermissions!!.clear()
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions!!.add(permissions[i])
                }
            }
            if (deniedPermissions!!.size == 0) {
                Permissions.log("Just allowed.")
                grant()
            } else {
                val blockedList = ArrayList<String?>() //set not to ask again.
                val justBlockedList = ArrayList<String?>() //just set not to ask again.
                val justDeniedList = ArrayList<String?>()
                for (permission in deniedPermissions!!) {
                    if (shouldShowRequestPermissionRationale(permission!!)) {
                        justDeniedList.add(permission)
                    } else {
                        blockedList.add(permission)
                        if (!noRationaleList!!.contains(permission)) {
                            justBlockedList.add(permission)
                        }
                    }
                }
                if (justBlockedList.size > 0) { //checked don't ask again for at least one.
                    val permissionHandler = permissionHandler
                    finish()
                    permissionHandler?.onJustBlocked(
                        applicationContext, justBlockedList,
                        deniedPermissions!!
                    )
                } else if (justDeniedList.size > 0) { //clicked deny for at least one.
                    deny()
                } else { //unavailable permissions were already set not to ask again.
                    if (permissionHandler != null &&
                        !permissionHandler!!.onBlocked(applicationContext, blockedList)
                    ) {
                      //  permissionHandler!!.openSettings()
                        sendToSettings()
                    } else finish()
                }
            }
        }
    }

    private fun sendToSettings() {
        if (!options!!.sendBlockedToSettings) {
            deny()
            return
        }
        Permissions.log("Ask to go to settings.")
        AlertDialog.Builder(this).setTitle(options!!.settingsDialogTitle)
            .setMessage(options!!.settingsDialogMessage)
            .setPositiveButton(options!!.settingsText) { dialog, which ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
               /* val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                startActivityForResult(intent, RC_SETTINGS)*/
            }
            .setNegativeButton(R.string.cancel) { dialog, which -> deny() }
            .setOnCancelListener { deny() }.create().show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("TAG", "CheckLibonactivity: ${requestCode}")
        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == RC_SETTINGS && permissionHandler != null) {
                Permissions.check(
                    this, toArray(allPermissions), null, options,
                    permissionHandler
                )
            }
        }



        // super, because overridden method will make the handler null, and we don't want that.
       // super.finish()
    }

    override fun onPause() {
        super.onPause()
        Log.d("TAG", "checkcheckcheck pause")

    }

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        deniedPermissions = ArrayList()
        noRationaleList = ArrayList()
        for (permission in allPermissions!!) {
            if (checkSelfPermission(permission!!) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions!!.add(permission)
                if (shouldShowRequestPermissionRationale(permission)) {
                    noRationale = false
                } else {
                    noRationaleList!!.add(permission)
                }
            }
        }
        if (deniedPermissions!!.isEmpty()) {
            if(permissionHandler!=null){
                Log.d("TAG", "checkcheckcheck pemisionhydfygf")
                permissionHandler!!.openSettings()
                finish()
            }
        }
    }
    private fun toArray(arrayList: ArrayList<String?>?): Array<String?> {
        val size = arrayList!!.size
        val array = arrayOfNulls<String>(size)
        for (i in 0 until size) {
            array[i] = arrayList[i]
        }
        return array
    }

    override fun finish() {
        permissionHandler = null
        super.finish()
    }

    private fun deny() {
        val permissionHandler = permissionHandler
        finish()
        permissionHandler?.onDenied(applicationContext, deniedPermissions!!)
    }

    private fun grant() {
        val permissionHandler = permissionHandler
        finish()
        permissionHandler?.onGranted()
    }

    companion object {
        private const val RC_SETTINGS = 6739
        private const val RC_PERMISSION = 6937
        const val EXTRA_PERMISSIONS = "permissions"
        const val EXTRA_RATIONALE = "rationale"
        const val EXTRA_OPTIONS = "options"
        @JvmField
        var permissionHandler: PermissionHandler? = null
    }
}