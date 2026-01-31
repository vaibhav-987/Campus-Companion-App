package com.buildingbadd.demojc.uiscreen.faculty.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.buildingbadd.demojc.uiscreen.faculty.StudentAttendanceUI

@Composable
fun StudentAttendanceRow(
    student: StudentAttendanceUI
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = student.enrollmentId,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = if (student.isPresent) "Present" else "Absent",
                color = if (student.isPresent)
                    Color(0xFF2E7D32)   // green
                else
                    Color(0xFFC62828), // red
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}