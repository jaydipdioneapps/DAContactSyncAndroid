package com.example.dacontactsyncandroid.Activity

class Address {
    var poBox: String? =""
    var street: String? = ""
    var city: String? = ""
    var state: String? = ""
    var postalCode: String? = ""
    var country: String? = ""
    var type: String? = ""
    private var asString = ""
    override fun toString(): String {
        return if (asString.length > 0) {
            asString
        } else {
            var addr = ""
            if (poBox != null) {
                addr = addr + poBox + "n"
            }
            if (street != null) {
                addr = addr + street + "n"
            }
            if (city != null) {
                addr = addr + city + ", "
            }
            if (state != null) {
                addr = addr + state + " "
            }
            if (postalCode != null) {
                addr = addr + postalCode + " "
            }
            if (country != null) {
                addr = addr + country
            }
            addr
        }
    }

    constructor(asString: String, type: String?) {
        this.asString = asString
        this.type = type
    }

    constructor(
        poBox: String?, street: String?, city: String?, state: String?,
        postal: String?, country: String?, type: String?
    ) {
        this.poBox = poBox
        this.street = street
        this.city = city
        this.state = state
        postalCode = postal
        this.country = country
        this.type = type
    }
}