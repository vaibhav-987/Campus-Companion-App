package com.buildingbadd.demojc.uiscreen.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class PendingUser(
    val uid: String = "",
    val email: String = "",
    val role: String = ""
)

@Composable
fun AdminApprovalScreen(navController:NavHostController) {

    val db = FirebaseFirestore.getInstance()
    var pendingUsers by remember { mutableStateOf(listOf<PendingUser>()) }

    // 🔹 Fetch pending users
    LaunchedEffect(Unit) {
        db.collection("users")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { doc ->
                    PendingUser(
                        uid = doc.getString("uid") ?: "",
                        email = doc.getString("email") ?: "",
                        role = doc.getString("role") ?: ""
                    )
                }
                pendingUsers = users
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(modifier = Modifier.align(Alignment.End), onClick = {
            FirebaseAuth.getInstance().signOut()
            navController.navigate("welcome") {
                popUpTo(0)
            }

        }) {
            Text("Logout")
        }

        Text(
            text = "Pending Approvals",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(pendingUsers) { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text("Email: ${user.email}")
                        Text("Role: ${user.role}")

                        Spacer(modifier = Modifier.height(12.dp))

                        Row {
                            Button(
                                onClick = {
                                    db.collection("users")
                                        .document(user.uid)
                                        .update("status", "approved")
                                        .addOnSuccessListener {
                                            pendingUsers = pendingUsers.filter { it.uid != user.uid }
                                        }
                                }
                            ) {
                                Text("Approve")
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            OutlinedButton(
                                onClick = {
                                    db.collection("users")
                                        .document(user.uid)
                                        .update("status", "rejected")
                                        .addOnSuccessListener {
                                            pendingUsers = pendingUsers.filter { it.uid != user.uid }
                                        }
                                }
                            ) {
                                Text("Reject")
                            }
                        }
                    }
                }
            }
        }
    }
}

