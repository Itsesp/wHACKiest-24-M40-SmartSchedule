package com.example.smartschedule;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExamManager {

    // Data model class for Exam
    public static class Exam {
        private String examName;
        private String examId;
        private String date;

        public Exam(String examName, String examId, String date) {
            this.examName = examName;
            this.examId = examId;
            this.date = date;
        }

        public String getExamName() {
            return examName;
        }

        public String getExamId() {
            return examId;
        }

        public String getDate() {
            return date;
        }
    }

    // Fetch and update exams from Firestore
    public void fetchAndUpdateExams(Context context) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Exams")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Exam> fetchedExams = new ArrayList<>();

                    for (QueryDocumentSnapshot examDoc : queryDocumentSnapshots) {
                        String examName = examDoc.getId(); // Exam name is the document ID
                        // Fetch data from each exam's subcollection (1, 2, 3, ...)
                        firestore.collection("Exams")
                                .document(examName)
                                .get()
                                .addOnSuccessListener(examData -> {
                                    for (String examId : examData.getData().keySet()) {
                                        Timestamp timestamp = (Timestamp) examData.get(examId);

                                        if (timestamp != null) {
                                            Date date = timestamp.toDate();
                                            String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                                            fetchedExams.add(new Exam(examName, examId, formattedDate));
                                        }
                                    }

                                    updateLocalExams(context, fetchedExams);
                                })
                                .addOnFailureListener(e -> e.printStackTrace());
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    // Update local exams data
    private void updateLocalExams(Context context, List<Exam> fetchedExams) {
        List<Exam> localExams = loadExamsLocally(context);

        // Add only new exams
        for (Exam fetchedExam : fetchedExams) {
            boolean exists = false;

            for (Exam localExam : localExams) {
                if (localExam.getExamName().equals(fetchedExam.getExamName()) && localExam.getExamId().equals(fetchedExam.getExamId())) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                localExams.add(fetchedExam);
            }
        }

        // Save updated list locally
        saveExamsLocally(context, localExams);
    }

    // Save exams locally
    private void saveExamsLocally(Context context, List<Exam> examList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("ExamData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        // Convert List<Exam> to JSON
        String json = gson.toJson(examList);
        editor.putString("exams", json);
        editor.apply();
    }

    // Load exams from local storage
    public List<Exam> loadExamsLocally(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("ExamData", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("exams", null);
        Gson gson = new Gson();

        // Convert JSON back to List<Exam>
        Type type = new TypeToken<List<Exam>>() {}.getType();
        return json != null ? gson.fromJson(json, type) : new ArrayList<>();
    }
}
