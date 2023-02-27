package com.example.dacontactsyncandroid.Activity.service

data class ContactRoomObject(
        var contactId: Int = 0,
        var contactName: String = "",
        var contactEmail: String? = null,
        var contactImage: String? = "",
        var contactMobileNumber: String? = null,
        var contactCountryCode: String? = null,
        var contactVersion: Int = 0,
        var contactIsSync: Int = 0,
        var contactIsMingler: Int = 0,
        var contactLastUpdatedTimeStamp: Long = 0,
)