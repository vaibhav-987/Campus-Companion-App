package com.buildingbadd.demojc.uiscreen.student

data class Assignment(
    val id: String,
    val title: String,
    val description: String,
    val subjectId: String,
    val subjectName: String,
    val className: String,
    val dueDate: String,
    val attachmentName: String?,
    val attachmentUrl: String?
)

