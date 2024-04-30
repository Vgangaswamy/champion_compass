package com.example.champion_compass;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

public class quiz_activity extends AppCompatActivity {

    private static final String TAG = "quiz_activity";
    private String selectedAnswer;
    private String userId;
    private List<Question> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout7);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();  // Retrieve the user ID
            // Optionally, store userId in a class variable if it's used in multiple places
        } else {
            // Handle the case where there is no signed-in user
            Log.d(TAG, "No user is signed in.");
            // Redirect to log in activity or disable specific functionality
        }

        questions = new ArrayList<>();
        fetchQuestions();

        Button button_to_submit = findViewById(R.id.submit_button);
        button_to_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitButtonClick(v); //
            }
        });

        RadioGroup optionsGroup = findViewById(R.id.options_group);
        optionsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selectedButton = findViewById(checkedId);
                selectedAnswer = selectedButton.getText().toString();
                Log.d(TAG, "Selected Option: " + selectedAnswer);
            }
        });
    }

    private void fetchQuestions() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("questions").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Question question = document.toObject(Question.class);
                    questions.add(question);
                }
                if (!questions.isEmpty()) {
                    displayQuestion(questions.get(0));
                }
            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
            }
        });
    }

    private void displayQuestion(Question question) {
        TextView questionTextView = findViewById(R.id.question_text);
        questionTextView.setText(question.getQuestionText());  // Set the question text

        RadioGroup optionsGroup = findViewById(R.id.options_group);
        optionsGroup.removeAllViews();  // Clear previous options

        for (Map.Entry<String, String> entry : question.getOptions().entrySet()) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(View.generateViewId());  // Ensure a unique ID
            radioButton.setText(entry.getValue());  // Set the text for the button
            radioButton.setTextColor(Color.BLACK);
            optionsGroup.addView(radioButton);  // Add to the group
        }
    }


    public void onSubmitButtonClick(View view) {
        RadioGroup optionsGroup = findViewById(R.id.options_group);
        int selectedId = optionsGroup.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(getApplicationContext(), "Please select an answer", Toast.LENGTH_SHORT).show();
        } else {
            RadioButton selectedButton = findViewById(selectedId);
            String selectedAnswer = selectedButton.getText().toString();
            Log.d(TAG, "Selected Option: " + selectedAnswer);

            Question currentQuestion = getCurrentQuestion();
            if (currentQuestion != null && currentQuestion.getId() != null) {
                currentQuestion.setUserAnswer(selectedAnswer);


                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Assuming the user is logged in

                Map<String, Object> answerData = new HashMap<>();
                answerData.put("userAnswer", "User's selected option");
                answerData.put("userId", userId);// If you let Firestore auto-generate the document ID
                db.collection("questions").document("theQuestionId")
                        .collection("userAnswers")
                        .add(answerData) // This will auto-generate a document ID
                        .addOnSuccessListener(documentReference -> Log.d("Firestore", "User answer saved successfully, Document ID: " + documentReference.getId()))
                        .addOnFailureListener(e -> Log.e("Firestore", "Error saving user answer", e));


            }

            int currentIndex = questions.indexOf(currentQuestion);
            if (currentIndex < questions.size() - 1) {
                displayQuestion(questions.get(currentIndex + 1));
            } else {
                Toast.makeText(getApplicationContext(), "End of quiz", Toast.LENGTH_SHORT).show();
                processUserAnswers();
            }
        }
    }

    private void processUserAnswers() {
        processUserAnswers(this.questions); // Use a class field or a default value
    }

    // Overloaded method with parameters
    private void processUserAnswers(List<Question> questions) {
        for (Question question : questions) {
            String userAnswer = question.getUserAnswer();
            Log.d(TAG, "User's Answer: " + userAnswer);
        }
    }

    private Question getCurrentQuestion() {
        TextView questionTextView = findViewById(R.id.question_text);
        String currentQuestionText = questionTextView.getText().toString();
        for (Question question : questions) {
            if (question.getQuestionText().equals(currentQuestionText)) {
                return question;
            }
        }
        return null;
    }

    public static class Question {
        private String id;
        private String questionText;
        private Map<String, String> options;
        private String userAnswer;

        public Question() {}

        public Question(String id, String questionText, Map<String, String> options) {
            this.id = id;
            this.questionText = questionText;
            this.options = options;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUserAnswer() {
            return userAnswer;
        }

        public void setUserAnswer(String userAnswer) {
            this.userAnswer = userAnswer;
        }

        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }

        public Map<String, String> getOptions() {
            return options;
        }

        public void setOptions(Map<String, String> options) {
            this.options = options;
        }
    }








}
