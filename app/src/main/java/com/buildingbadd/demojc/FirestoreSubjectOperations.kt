package com.buildingbadd.demojc

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

data class FacultySubjectUpdate(
    val facultyId: String,
    val subjects: List<String> // List of Subject IDs like "IT101IP", "COM101FA"
)

fun updateFacultySubjects(context: Context,onCompletion: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val batch = db.batch()

    // Mapping Faculty IDs to the Subjects we assigned in the previous step
    val updates = listOf(
        // BSCIT Faculty
        FacultySubjectUpdate("FAC_IT_01", listOf("IT101IP", "IT201DS")),
        FacultySubjectUpdate("FAC_IT_02", listOf("IT102DE", "IT202CO", "IT301OS","IT401JP")),
        FacultySubjectUpdate("FAC_IT_03", listOf("IT103MA", "IT203MA", "IT302DB", "IT402WT","IT501AJ", "IT601AI")),
        FacultySubjectUpdate("FAC_IT_04", listOf("IT104FD", "IT204WT", "IT303CN","IT403CG", "IT502CC","IT602BD")),
        FacultySubjectUpdate("FAC_IT_05", listOf("IT105CS", "IT205EV", "IT304SE", "IT404DC","IT503IS", "IT603IoT")),
        FacultySubjectUpdate("FAC_IT_06", listOf("IT305PY", "IT405BC","IT504MD","IT604ST")),
        FacultySubjectUpdate("FAC_IT_07", listOf("IT505PJ", "IT605PJ")),

        // BCOM Faculty
        FacultySubjectUpdate("FAC_COM_01", listOf("COM101FA", "COM201CA", "COM505PJ", "COM605PJ")),
        FacultySubjectUpdate("FAC_COM_02", listOf("COM102BE", "COM202BM", "COM301CA", "COM401FM")),
        FacultySubjectUpdate("FAC_COM_03", listOf("COM103BC", "COM203PM", "COM302IT", "COM402BS", "COM501AA", "COM601SM")),
        FacultySubjectUpdate("FAC_COM_04", listOf("COM104CL", "COM204CL", "COM303BF", "COM403HR", "COM502IB", "COM602FM")),
        FacultySubjectUpdate("FAC_COM_05", listOf("COM105EV", "COM205EC", "COM304AU", "COM404ED", "COM503CC", "COM603CG")),
        FacultySubjectUpdate("FAC_COM_06", listOf("COM305MM", "COM405GT", "COM504BE", "COM604IA"))
    )

    for (item in updates) {
        // We assume the Document ID is the Faculty ID (e.g., FAC_IT_01)
        val docRef = db.collection("faculty_details").document(item.facultyId)

        // update() only modifies the 'subjects' field
        batch.update(docRef, "assignedSubjectIds", item.subjects)
    }

    batch.commit()
        .addOnSuccessListener {
            Toast.makeText(context, "Faculty subjects updated!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Log.e("FirestoreUpdate", "Error updating faculty: ${e.message}")
        }
}