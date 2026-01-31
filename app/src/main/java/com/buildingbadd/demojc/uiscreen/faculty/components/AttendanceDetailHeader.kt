package com.buildingbadd.demojc.uiscreen.faculty.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AttendanceDetailHeader(
    subject: String,
    date: String,
    total: Int,
    present: Int
) {
    val absent = total - present

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("Lecture: $subject", style = MaterialTheme.typography.titleMedium)
            Text("Date: $date")

            Spacer(modifier = Modifier.height(8.dp))

            Text("Total: $total | Present: $present | Absent: $absent")
        }
    }
}