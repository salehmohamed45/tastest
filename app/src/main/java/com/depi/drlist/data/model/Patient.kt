package com.depi.drlist.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Patient(
    val id: String = "", // Firestore will generate this
    val doctorId: String = "", // To know which doctor this patient belongs to
    val name: String = "",
    val nationalId: String = "",
    val age: String = "",
    val address: String = "",
    val visitType: String = "",
    val visitPrice: String = "",
    @ServerTimestamp
    val visitDate: Date? = null // Firestore will automatically set the current date and time
)