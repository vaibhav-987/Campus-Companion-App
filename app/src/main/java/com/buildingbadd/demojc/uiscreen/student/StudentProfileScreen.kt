package com.buildingbadd.demojc.uiscreen.student

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.buildingbadd.demojc.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(navController: NavHostController) {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var profile by remember { mutableStateOf<StudentProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAddressDialog by remember { mutableStateOf(false) }
    var showPhotoWarning by remember { mutableStateOf(false) }

    var attendancePercent by remember { mutableStateOf(0) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        uri ->
            if(uri != null && profile != null){
                uploadProfilePhoto(enrollmentId = profile!!.enrollmentId, imageUri = uri)
            }
    }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        val userDoc = db.collection("users").document(uid).get().await()
        val enrollmentId = userDoc.getString("enrollmentId") ?: return@LaunchedEffect

        val studentDoc =
            db.collection("students_detail").document(enrollmentId).get().await()

        profile = StudentProfile(
            enrollmentId = enrollmentId,
            name = studentDoc.getString("name") ?: "",
            email = studentDoc.getString("email") ?: "",
            phone = studentDoc.getString("phone") ?: "",
            courseId = studentDoc.getString("courseId") ?: "",
            currentSemesterId = studentDoc.getString("currentSemesterId") ?: "",
            dob = studentDoc.getString("dob") ?: "",
            gender = studentDoc.getString("gender") ?: "",
            address = studentDoc.getString("address") ?: "",
            addressLocked = studentDoc.getBoolean("addressLocked") ?: false,
            profilePhotoUrl = studentDoc.getString("profilePhotoUrl") ?: "",
            photoLocked = studentDoc.getBoolean("photoLocked") ?: false
        )

        isLoading = false

        attendancePercent = calculateAttendancePercentage(
            enrollmentId = enrollmentId,
            studentClass = studentDoc.getString("class") ?: "",
            currentSemesterId = studentDoc.getString("currentSemesterId") ?: ""
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                actions = {
                    TextButton(onClick = { showLogoutDialog = true }) {
                        Text("Logout")
                    }
                }
            )
        },
        bottomBar = { StudentBottomNavBar(navController) }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                profile != null -> {
                    ProfileContent(
                        profile = profile!!,
                        attendancePercent = attendancePercent,
                        onEditAddress = { showAddressDialog = true },
                        onPhotoClick = { showPhotoWarning = true }
                    )
                }
            }
        }
    }

    if (showPhotoWarning) {
        AlertDialog(
            onDismissRequest = { showPhotoWarning = false },
            confirmButton = {
                TextButton(onClick = {
                    showPhotoWarning = false
                    imagePicker.launch("image/*")
                }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoWarning = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Upload Profile Photo") },
            text = {
                Text("You can upload profile photo only once. Please choose a clear and correct photo.")
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    auth.signOut()
                    navController.navigate("login") { popUpTo(0) }
                }) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") }
        )
    }

    if (showAddressDialog && profile != null) {
        EditAddressDialog(
            initialAddress = profile!!.address,
            onSave = { newAddress ->
                saveAddressOnce(
                    enrollmentId = profile!!.enrollmentId,
                    address = newAddress
                ) {
                    showAddressDialog = false
                }
            },
            onDismiss = { showAddressDialog = false }
        )
    }
}

fun saveAddressOnce(
    enrollmentId: String,
    address: String,
    onDone: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("students_detail")
        .document(enrollmentId)
        .update(
            mapOf(
                "address" to address,
                "addressLocked" to true
            )
        )
        .addOnSuccessListener { onDone() }
}



@Composable
fun InfoRowSimple(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value)
    }
}

@Composable
fun ProfileContent(
    profile: StudentProfile,
    attendancePercent: Int,
    onEditAddress: () -> Unit,
    onPhotoClick: () -> Unit
) {
    val semesterNumber =
        profile.currentSemesterId.takeLast(1).toIntOrNull() ?: 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        ProfileHeader(
            name = profile.name,
            enrollmentId = profile.enrollmentId,
            studentClass = profile.courseId,
            photoUrl = profile.profilePhotoUrl,
            photoLocked = profile.photoLocked,
            onPhotoClick = onPhotoClick
        )

        StatsCard(
            attendancePercent = attendancePercent,
            semesterNumber = semesterNumber
        )

        InfoCard(
            title = "Personal Information",
            items = listOf(
                "Date of Birth" to profile.dob,
                "Gender" to profile.gender
            )
        )

        ContactDetailsCard(
            profile = profile,
            onAddressEdit = onEditAddress
        )
    }
}

@Composable
fun ContactDetailsCard(
    profile: StudentProfile,
    onAddressEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("Contact Details", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            InfoRowSimple("Phone", profile.phone)
            InfoRowSimple("Email", profile.email)

            Spacer(modifier = Modifier.height(8.dp))

            AddressRow(
                address = profile.address,
                addressLocked = profile.addressLocked,
                onEditClick = onAddressEdit
            )
        }
    }
}

data class StudentProfile(
    val enrollmentId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val courseId: String = "",
    val currentSemesterId: String = "",
    val dob: String = "",
    val gender: String = "",
    val address: String = "",
    val addressLocked: Boolean = false,
    val profilePhotoUrl: String = "",
    val photoLocked: Boolean = false
)


@Composable
fun ProfileHeader(
    name: String,
    enrollmentId: String,
    studentClass: String,
    photoUrl: String,
    photoLocked: Boolean,
    onPhotoClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {

        Box(contentAlignment = Alignment.BottomEnd) {

            if (photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.graduate),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                )
            }

            if (!photoLocked) {
                IconButton(
                    onClick = onPhotoClick,
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Upload")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(name, style = MaterialTheme.typography.titleLarge)
        Text(enrollmentId, color = Color.Gray)
        Text(studentClass, color = MaterialTheme.colorScheme.primary)
    }
}


@Composable
fun StatsCard(
    attendancePercent: Int,
    semesterNumber: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$attendancePercent%",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Attendance",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = semesterNumber.toString(),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Semester",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    items: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, color = Color.Gray)
                    Text(value.ifEmpty { "-" })
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AddressRow(
    address: String,
    addressLocked: Boolean,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Address", color = Color.Gray)
            Text(address.ifEmpty { "-" })
        }

        if (!addressLocked) {
            TextButton(onClick = onEditClick) {
                Text("Edit")
            }
        }
    }
}

@Composable
fun EditAddressDialog(
    initialAddress: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var address by remember { mutableStateOf(initialAddress) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (address.isNotBlank()) {
                        onSave(address)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Set Address") },
        text = {
            Column {
                Text(
                    "You can edit address only once.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

fun uploadProfilePhoto(
    enrollmentId: String,
    imageUri: Uri
) {
    val storage = FirebaseStorage.getInstance()
    val db = FirebaseFirestore.getInstance()

    val ref = storage.reference
        .child("profile_photos/students/$enrollmentId.jpg")

    ref.putFile(imageUri)
        .continueWithTask { ref.downloadUrl }
        .addOnSuccessListener { url ->
            db.collection("students_detail")
                .document(enrollmentId)
                .update(
                    mapOf(
                        "profilePhotoUrl" to url.toString(),
                        "photoLocked" to true
                    )
                )
        }
}

suspend fun calculateAttendancePercentage(
    enrollmentId: String,
    studentClass: String,
    currentSemesterId: String
): Int {

    val db = FirebaseFirestore.getInstance()

    val subjectSnapshot = db.collection("subjects")
        .whereEqualTo("semesterId", currentSemesterId)
        .get()
        .await()

    val subjectIds = subjectSnapshot.documents.mapNotNull {
        it.getString("subjectID")
    }

    if (subjectIds.isEmpty()) return 0

    val attendanceSnapshot = db.collection("attendance")
        .whereEqualTo("class", studentClass)
        .whereIn("subjectId", subjectIds)
        .get()
        .await()

    var totalLectures = 0
    var presentLectures = 0

    for (doc in attendanceSnapshot.documents) {
        val records = doc.get("records") as? Map<*, *> ?: continue
        totalLectures++

        if (records[enrollmentId] == true) {
            presentLectures++
        }
    }

    if (totalLectures == 0) return 0

    return ((presentLectures * 100) / totalLectures)
}