package com.depi.drlist.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val phoneNumber: String? = null,
    val address: String? = null,
    val role: String = "customer"
)
