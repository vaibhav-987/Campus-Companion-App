package com.buildingbadd.demojc.uiscreen.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class SubjectReport(
    val subject: String,
    val attendancePercent: Int,
    val assignmentsCompleted: Int,
    val totalAssignments: Int
)

@Composable
fun StudentReportsScreen() {

    // 🔹 Dummy report data
    val reports = listOf(
        SubjectReport("Computer Networks", 85, 3, 4),
        SubjectReport("DBMS", 90, 4, 4),
        SubjectReport("Operating Systems", 72, 2, 3),
        SubjectReport("Maths", 78, 3, 5)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Performance Report",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Overall summary
        OverallPerformanceCard(reports)

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Subject-wise reports
        LazyColumn {
            items(reports) { report ->
                SubjectReportCard(report)
            }
        }
    }
}

@Composable
fun OverallPerformanceCard(reports: List<SubjectReport>) {

    val avgAttendance =
        reports.sumOf { it.attendancePercent } / reports.size

    val totalCompleted =
        reports.sumOf { it.assignmentsCompleted }

    val totalAssignments =
        reports.sumOf { it.totalAssignments }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Overall Summary",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Average Attendance: $avgAttendance%")
            Text("Assignments: $totalCompleted / $totalAssignments")

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = avgAttendance / 100f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SubjectReportCard(report: SubjectReport) {

    val assignmentProgress =
        report.assignmentsCompleted.toFloat() / report.totalAssignments

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = report.subject,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Attendance: ${report.attendancePercent}%")
            LinearProgressIndicator(
                progress = report.attendancePercent / 100f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Assignments: ${report.assignmentsCompleted} / ${report.totalAssignments}"
            )
            LinearProgressIndicator(
                progress = assignmentProgress,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
