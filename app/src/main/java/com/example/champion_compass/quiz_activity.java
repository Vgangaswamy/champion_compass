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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class quiz_activity extends AppCompatActivity {

    private static final String TAG = "quiz_activity";
    private String selectedAnswer;
    private List<Question> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout7);

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
        RadioGroup optionsGroup = findViewById(R.id.options_group);

        questionTextView.setText(question.getQuestionText());
        optionsGroup.removeAllViews();

        for (Map.Entry<String, String> entry : question.getOptions().entrySet()) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(View.generateViewId());
            radioButton.setText(String.format("%s: %s", entry.getKey(), entry.getValue()));
            optionsGroup.addView(radioButton);
            radioButton.setTextColor(Color.BLACK);
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
                db.collection("questions").document(currentQuestion.getId())
                        .update("userAnswer", selectedAnswer)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "User answer updated successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating user answer", e);
                            }
                        });
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

    private void processUserAnswers() {
        for (Question question : questions) {
            String userAnswer = question.getUserAnswer();
            Log.d(TAG, "User's Answer: " + userAnswer);
        }
    }
}
