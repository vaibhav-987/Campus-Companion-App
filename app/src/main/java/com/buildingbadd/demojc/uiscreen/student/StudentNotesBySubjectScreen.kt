package com.buildingbadd.demojc.uiscreen.student

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentNotesBySubjectScreen(
    navController: NavHostController,
    subjectId: String
) {

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var notes by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(subjectId) {
        try {
            val snapshot = db.collection("notes")
                .whereEqualTo("subjectId", subjectId)
                .get()
                .await()

            notes = snapshot.documents.mapNotNull { it.data }

        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Notes") })
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                notes.isEmpty() -> Text(
                    "No notes available",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn {
                    items(notes) { note ->
                        StudentNoteCard(note, context)
                    }
                }
            }
        }
    }
}
