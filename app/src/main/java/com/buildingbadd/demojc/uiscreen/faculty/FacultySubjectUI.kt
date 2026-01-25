package com.buildingbadd.demojc.uiscreen.faculty

data class FacultySubjectUI(
    val subjectId: String,
    val subjectName: String,
    val courseId: String,
    val semesterId: String
) {
    val displayText: String
        get() = "$subjectId - $subjectName"
}