package com.example.fln.activities.birds;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.fln.activities.QuestionActivity;
import com.example.fln.R;
import com.example.fln.questions.birds.BirdFindQuestion;
import com.example.fln.tokens.BaseValue;
import com.example.fln.tokens.Bird;

@RequiresApi(api = Build.VERSION_CODES.S)
public class BirdFindActivity extends QuestionActivity {
    private TextView questionView;
    private ImageView birdAnswerInput;
    private TextView correctnessView;
    private ImageView correctImage;
    private ImageView incorrectImage;
    private TextView questionNumberView;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.currentIndex = 0;
    }

    @Override
    protected void setViewsAndLayout() {
        setContentView(R.layout.bird_find);

        questionView = findViewById(R.id.birdsQuestionId);
        birdAnswerInput = findViewById(R.id.birdAnswerInput);
        correctnessView = findViewById(R.id.correctnessView);
        correctImage = findViewById(R.id.correct);
        incorrectImage = findViewById(R.id.incorrect);
        questionNumberView = findViewById(R.id.questionNumberId);
        correctImage.setVisibility(View.INVISIBLE);
        incorrectImage.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void setQuestion() {
        question = (BirdFindQuestion) getIntent().getSerializableExtra("question");
        int questionNumber = getIntent().getIntExtra("question_number", 0) + 1;

        if (question == null || question.getQuestion() == null) {
            Log.e("Bird Find", "Question text not set.");
        } else {
            questionView.setText(question.getQuestion());
            questionNumberView.setText("Question " + questionNumber);
        }
        node = question.generateAnswerTree();
    }

    @Override
    protected QuestionActivity getThis() {
        return this;
    }

    @Override
    protected boolean isCorrectCategory(BaseValue value) {
        return (value instanceof Bird);
    }

    @Override
    protected void updateAnswer() {

    }
}