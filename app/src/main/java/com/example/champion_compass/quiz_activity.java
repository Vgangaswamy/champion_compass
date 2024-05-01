package com.example.champion_compass;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class quiz_activity extends AppCompatActivity {

    private static final String TAG = "quiz_activity";
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String selectedAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout7);

        if (user == null) {
            Log.d(TAG, "No user is signed in.");
            finish();  // Exit the activity if no user is logged in
            return;
        }

        questions = new ArrayList<>();
        fetchQuestions();

        Button button_to_submit = findViewById(R.id.submit_button);
        button_to_submit.setOnClickListener(this::onSubmitButtonClick);

        RadioGroup optionsGroup = findViewById(R.id.options_group);
        optionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedButton = findViewById(checkedId);
            if (selectedButton != null) {
                String selectedAnswer = selectedButton.getText().toString();
                Log.d(TAG, "Selected Option: " + selectedAnswer);
                storeUserAnswer(selectedAnswer);
            }
        });
    }

    private void fetchQuestions() {
        DatabaseReference questionsRef = FirebaseDatabase.getInstance().getReference("questions");
        questionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Question question = snapshot.getValue(Question.class);
                        if (question != null && question.getQuestionId() != null && !question.getOptions().isEmpty()) {
                            questions.add(question);
                            Log.d(TAG, "Fetched Question ID: " + question.getQuestionId());
                        } else {
                            // Safely logging potentially null data
                            Object data = snapshot.getValue();
                            Log.e(TAG, "Invalid question data: " + (data == null ? "null" : data.toString()));
                        }
                    }
                    if (!questions.isEmpty()) {
                        displayQuestion();
                    }
                } else {
                    Log.e(TAG, "No data found for questions");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "loadQuestion:onCancelled", databaseError.toException());
            }
        });
    }




    private void displayQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question question = questions.get(currentQuestionIndex);
            if (question != null && question.getQuestionId() != null) {
                updateUIWithQuestion(question);  // Update the UI with the new question
            } else {
                Log.e(TAG, "Current question or question ID is null at index " + currentQuestionIndex);
            }
        } else {
            Log.e(TAG, "Question index out of bounds: " + currentQuestionIndex);
            finishQuiz();
        }
    }

    private void updateUIWithQuestion(Question question) {
        TextView questionTextView = findViewById(R.id.question_text);
        questionTextView.setText(question.getText());  // Set the text of the question to TextView

        RadioGroup optionsGroup = findViewById(R.id.options_group);
        optionsGroup.clearCheck();  // Clear any previous selection
        optionsGroup.removeAllViews();  // Remove all previous options

        // Dynamically create radio buttons for each option
        for (Map.Entry<String, String> entry : question.getOptions().entrySet()) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(View.generateViewId());  // Generate a unique ID for each radio button
            radioButton.setText(entry.getKey() + ": " + entry.getValue());  // Set the text for the radio button
            radioButton.setTextColor(Color.BLACK);  // Set the text color
            optionsGroup.addView(radioButton);  // Add the radio button to the RadioGroup
            Log.d(TAG, "Option added: " + entry.getKey() + ": " + entry.getValue());  // Log the addition of each option
        }
    }




    private void storeUserAnswer(String selectedAnswer) {
        Question currentQuestion = questions.get(currentQuestionIndex);
        if (currentQuestion == null || currentQuestion.getId() == null) {
            Log.e(TAG, "Current question or question ID is null");
            return; // Early return to avoid crashing
        }
        currentQuestion.setUserAnswer(selectedAnswer);

        Map<String, Object> answerData = new HashMap<>();
        answerData.put("userAnswer", selectedAnswer);
        answerData.put("userId", user.getUid());

        db.collection("questions").document(currentQuestion.getId())
                .collection("answers").document(user.getUid())
                .set(answerData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User answer saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving user answer", e));
    }


    private void onSubmitButtonClick(View view) {
        // Check to prevent out-of-bounds access
        if (currentQuestionIndex < questions.size()) {
            storeUserAnswer(selectedAnswer);  // Make sure to capture the selected answer correctly

            currentQuestionIndex++;  // Increment after storing the answer

            if (currentQuestionIndex < questions.size()) {
                displayQuestion();
            } else {
                Log.d(TAG, "End of Quiz");
                finishQuiz();
            }
        } else {
            Log.e(TAG, "Invalid question index access: " + currentQuestionIndex);
        }
    }


    private void finishQuiz() {
        // Disable interaction or navigate to another activity
    }

    public static class Question {
        private String questionId;
        private String id;
        private String text;
        private Map<String, String> options;
        private String userAnswer;

        public Question() {
            // Default no-arg constructor needed for Firebase deserialization
        }

        // Getters and setters
        public String getQuestionId() {
            return questionId;
        }

        public void setQuestionId(String questionId) {
            this.questionId = questionId;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Map<String, String> getOptions() {
            return options;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setOptions(Map<String, String> options) {
            this.options = options;
        }

        public String getUserAnswer() {
            return userAnswer;
        }

        public void setUserAnswer(String userAnswer) {
            this.userAnswer = userAnswer;
        }

    }




}
