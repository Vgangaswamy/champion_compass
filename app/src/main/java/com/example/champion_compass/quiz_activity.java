package com.example.champion_compass;

import android.content.Intent;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class quiz_activity extends AppCompatActivity {

    private static final String TAG = "quiz_activity";
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
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
                selectedAnswer = selectedButton.getText().toString().substring(0,1);
                Log.d(TAG, "Selected Option: " + selectedAnswer);
               //  storeUserAnswer(selectedAnswer);
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
                            question.setId(snapshot.getKey());
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
            countUserAnswers(user.getUid());
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
        Map<String, Object> answerData = new HashMap<>();
        answerData.put("userAnswer", selectedAnswer);
        answerData.put("userId", user.getUid());

        db.getReference("questions").child(currentQuestion.getId())
                .child("userAnswer").child(user.getUid())
                .setValue(answerData)
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

                countUserAnswers(user.getUid());
            }
        } else {
            Log.e(TAG, "Invalid question index access: " + currentQuestionIndex);
        }
    }


    private void countUserAnswers(String userId) {
        db.getReference("questions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int countA = 0;
                int countB = 0;
                int countC = 0;
                int countD = 0;
                int countE = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DataSnapshot answerSnapshot = snapshot.child("userAnswer").child(userId);
                    if (answerSnapshot.exists()) {
                        String answer = answerSnapshot.child("userAnswer").getValue(String.class);
                        if ("A".equals(answer)) {
                            countA++;
                        } else if ("B".equals(answer)) {
                            countB++;
                        } else if ("C".equals(answer)) {
                            countC++;
                        } else if ("D".equals(answer)) {
                            countD++;
                        } else {
                            countE++;
                        }
                    }
                }

                char highestCountLetter = 'A';
                int highestCount = countA;
                Intent intent;

                if (countB > highestCount) {
                    highestCount = countB;
                    highestCountLetter = 'B';
                }
                if (countC > highestCount) {
                    highestCount = countC;
                    highestCountLetter = 'C';
                }
                if (countD > highestCount) {
                    highestCount = countD;
                    highestCountLetter = 'D';
                }
                if (countE > highestCount) {
                    highestCount = countE;
                    highestCountLetter = 'E';
                }

                // Display or use the counts
                System.out.println("Count of A's: " + countA);
                System.out.println("Count of B's: " + countB);
                System.out.println("Count of C's: " + countC);
                System.out.println("Count of D's: " + countD);
                System.out.println("Count of E's: " + countE);
                System.out.println("Highest count is " + highestCount + " for letter: " + highestCountLetter);
                System.out.println("switch: " + highestCountLetter);

                switch (highestCountLetter) {
                    case 'A':
                        System.out.println("Highest count is " + highestCount + " for letter: " + highestCountLetter);
                        intent = new Intent(quiz_activity.this, ActivityA.class); // Top lane
                        break;
                    case 'B':
                        intent = new Intent(quiz_activity.this, ActivityB.class); //Jungle lane
                        break;
                    case 'C':
                        intent = new Intent(quiz_activity.this, ActivityC.class); // Mid lane
                        break;
                    case 'D':
                        intent = new Intent(quiz_activity.this, ActivityD.class); // Support lane
                        break;
                    case 'E':
                        intent = new Intent(quiz_activity.this, ActivityE.class); // Bot lane
                        break;
                    default:
                        return; // No action if no cases match
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }




        public static class Question {
        private String questionId;
        private String id;
        private String text;
        private Map<String, String> options;
        private Map<String, Object> userAnswer;

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

        public Map<String, Object> getUserAnswer() {
            return userAnswer;
        }

        public void setUserAnswer(Map<String, Object> userAnswer) {
            this.userAnswer = userAnswer;
        }

    }




}
