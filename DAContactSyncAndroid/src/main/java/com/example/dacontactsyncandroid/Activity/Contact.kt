package com.example.dacontactsyncandroid.Activity

import android.util.Log

class Contact(var id: String, var name: String) {
    var emails: ArrayList<ContactEmail>
    var adress: ArrayList<Address>
    var numbers: ArrayList<ContactPhone>

    init {
        emails = ArrayList()
        numbers = ArrayList()
        adress = ArrayList()
    }

    override fun toString(): String {
        var result = name
        if (numbers.size > 0) {
            val number = numbers[0]
            result += " (" + number.number + " - " + number.type +" - " + number.countryIsoCode + ")"
        }
        if (emails.size > 0) {
            val email = emails[0]
            result += " [" + email.address + " - " + email.type + "]"
        }
        if (adress.size > 0) {
            val adress = adress[0]
            var addr = ""
            if (adress.poBox != null) {
                addr = addr + adress.poBox + "n"
            }
            if (adress.street != null) {
                addr = addr + adress.street + "n"
            }
            if (adress.city != null) {
                addr = addr + adress.city + ", "
            }
            if (adress.state != null) {
                addr = addr + adress.state + " "
            }
            if (adress.postalCode != null) {
                addr = addr + adress.postalCode + " "
            }
            if (adress.country != null) {
                addr = addr + adress.country
            }

            result += " [" + addr + "]"
        }
        return result
    }

    fun addEmail(address: String?, type: String?) {
        emails.add(ContactEmail(address!!, type!!))
    }
    fun addAddress(poBox: String?, street: String?, city: String?, state: String?,
                 postal: String?, country: String?, type: String?) {
        adress.add(Address(poBox!!, street!!, city!! , state!! ,postal!!,country!!,type!!))
    }

    fun addNumber(number: String?, type: String?, countryIsoCode: String?) {
        Log.d("TAG", "matchContactNumbersvff:addNumber   ${countryIsoCode}")
        numbers.add(ContactPhone(number!!, type!!,countryIsoCode!!))
    }
}