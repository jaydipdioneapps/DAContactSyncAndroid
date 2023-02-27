package com.example.testrunproject

data class ContactSyncBody(
    val add: List<Add>,
    val delete: List<Add>,
    val update: List<Add>
)
data class Add(
    val code: Int,
    val name: String,
    val phone: Long
)