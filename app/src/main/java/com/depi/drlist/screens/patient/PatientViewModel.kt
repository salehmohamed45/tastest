package com.depi.drlist.screens.patient

import androidx.lifecycle.ViewModel
import com.depi.drlist.data.model.Patient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.util.Calendar
import java.util.Date

sealed interface SaveState {
    object Idle : SaveState
    object Loading : SaveState
    data class Success(val message: String) : SaveState
    data class Error(val message: String) : SaveState
}

sealed interface PatientListState {
    object Loading : PatientListState
    data class Success(val patients: List<Patient>) : PatientListState
    data class Error(val message: String) : PatientListState
}

class PatientViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // States for form fields
    private val _patientName = MutableStateFlow("")
    val patientName = _patientName.asStateFlow()
    private val _nationalId = MutableStateFlow("")
    val nationalId = _nationalId.asStateFlow()
    private val _age = MutableStateFlow("")
    val age = _age.asStateFlow()
    private val _address = MutableStateFlow("")
    val address = _address.asStateFlow()
    private val _visitType = MutableStateFlow("كشف")
    val visitType = _visitType.asStateFlow()
    private val _visitPrice = MutableStateFlow("200")
    val visitPrice = _visitPrice.asStateFlow()

    // Operation States
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState = _saveState.asStateFlow()
    private val _patientsListState = MutableStateFlow<PatientListState>(PatientListState.Loading)

    // Summary States
    private val _todaysPatientCount = MutableStateFlow(0)
    val todaysPatientCount = _todaysPatientCount.asStateFlow()
    private val _todaysIncome = MutableStateFlow(0.0)
    val todaysIncome = _todaysIncome.asStateFlow()
    private val _weeklyPatientCount = MutableStateFlow(0)
    val weeklyPatientCount = _weeklyPatientCount.asStateFlow()
    private val _weeklyIncome = MutableStateFlow(0.0)
    val weeklyIncome = _weeklyIncome.asStateFlow()
    private val _totalPatientCount = MutableStateFlow(0)
    val totalPatientCount = _totalPatientCount.asStateFlow()
    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome = _totalIncome.asStateFlow()

    // Filter States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private val _fromDate = MutableStateFlow<Date?>(null)
    val fromDate = _fromDate.asStateFlow()
    private val _toDate = MutableStateFlow<Date?>(null)
    val toDate = _toDate.asStateFlow()

    val filteredPatientsState = combine(
        _patientsListState, _searchQuery, _fromDate, _toDate
    ) { state, query, from, to ->
        when (state) {
            is PatientListState.Success -> {
                val searchFilteredList = if (query.isBlank()) {
                    state.patients
                } else {
                    state.patients.filter {
                        it.name.contains(query, ignoreCase = true) || it.nationalId.contains(query)
                    }
                }
                val dateFilteredList = if (from != null && to != null) {
                    val calendar = Calendar.getInstance().apply { time = to; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59) }
                    val endOfToDate = calendar.time
                    searchFilteredList.filter {
                        it.visitDate != null && !it.visitDate.before(from) && !it.visitDate.after(endOfToDate)
                    }
                } else {
                    searchFilteredList
                }
                PatientListState.Success(dateFilteredList)
            }
            else -> state
        }
    }.flowOn(Dispatchers.Default)

    // The init block that caused the problem has been REMOVED.

    fun fetchPatients() { // Now public and called from HomeScreen
        val currentDoctorId = auth.currentUser?.uid
        if (currentDoctorId == null) {
            _patientsListState.value = PatientListState.Error("User not logged in.")
            return
        }
        _patientsListState.value = PatientListState.Loading

        firestore.collection("doctors").document(currentDoctorId)
            .collection("patients")
            // Make sure you have created the Firestore index if you uncomment the line below
            // .orderBy("visitDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _patientsListState.value = PatientListState.Error(error.message ?: "Failed to fetch patients.")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val allPatients = snapshot.toObjects(Patient::class.java)
                    _patientsListState.value = PatientListState.Success(allPatients)
                    updateSummaries(allPatients)
                    _totalPatientCount.value = allPatients.size
                    _totalIncome.value = allPatients.sumOf { it.visitPrice.toDoubleOrNull() ?: 0.0 }
                }
            }
    }

    private fun updateSummaries(patients: List<Patient>) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
        val startOfToday = calendar.time
        calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59)
        val endOfToday = calendar.time

        val todaysPatients = patients.filter { it.visitDate != null && it.visitDate.after(startOfToday) && it.visitDate.before(endOfToday) }
        _todaysPatientCount.value = todaysPatients.size
        _todaysIncome.value = todaysPatients.sumOf { it.visitPrice.toDoubleOrNull() ?: 0.0 }

        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
        val startOfWeek = calendar.time

        val weeklyPatients = patients.filter { it.visitDate != null && it.visitDate.after(startOfWeek) }
        _weeklyPatientCount.value = weeklyPatients.size
        _weeklyIncome.value = weeklyPatients.sumOf { it.visitPrice.toDoubleOrNull() ?: 0.0 }
    }

    // Filter update functions
    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun onFromDateSelected(dateMillis: Long?) { _fromDate.value = dateMillis?.let { Date(it) } }
    fun onToDateSelected(dateMillis: Long?) { _toDate.value = dateMillis?.let { Date(it) } }

    // Add Patient functions
    fun onPatientNameChange(newName: String) { _patientName.value = newName }
    fun onNationalIdChange(newId: String) { _nationalId.value = newId }
    fun onAgeChange(newAge: String) { _age.value = newAge }
    fun onAddressChange(newAddress: String) { _address.value = newAddress }
    fun onVisitTypeChange(newType: String) {
        _visitType.value = newType
        _visitPrice.value = if (newType == "كشف") "200" else "120"
    }

    fun addPatient() {
        val currentDoctorId = auth.currentUser?.uid
        if (currentDoctorId == null) {
            _saveState.value = SaveState.Error("User not logged in.")
            return
        }
        if (_patientName.value.isBlank() || _nationalId.value.isBlank()) {
            _saveState.value = SaveState.Error("Patient name and ID cannot be empty.")
            return
        }
        _saveState.value = SaveState.Loading
        val patient = Patient(
            doctorId = currentDoctorId,
            name = _patientName.value.trim(),
            nationalId = _nationalId.value.trim(),
            age = _age.value.trim(),
            address = _address.value.trim(),
            visitType = _visitType.value,
            visitPrice = _visitPrice.value
        )
        firestore.collection("doctors").document(currentDoctorId)
            .collection("patients").add(patient)
            .addOnSuccessListener {
                _saveState.value = SaveState.Success("Patient saved successfully!")
                resetPatientForm()
            }
            .addOnFailureListener { e ->
                _saveState.value = SaveState.Error(e.message ?: "Failed to save patient.")
            }
    }

    fun resetPatientForm() {
        _patientName.value = ""
        _nationalId.value = ""
        _age.value = ""
        _address.value = ""
        _visitType.value = "كشف"
        _visitPrice.value = "200"
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}