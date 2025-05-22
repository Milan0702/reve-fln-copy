package com.example.fln.activities.maths;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.fln.activities.QuestionActivity;
import com.example.fln.R;
import com.example.fln.questions.math.MathOperationQuestion;
import com.example.fln.tokens.BaseValue;
import com.example.fln.tokens.Number;
import com.example.fln.tokens.Symbol;


@RequiresApi(api = Build.VERSION_CODES.S)
public class MathOperationActivity extends QuestionActivity {
    private TextView questionView;
    private TextView answerView;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.currentIndex = 0;
    }

    @Override
    protected void setViewsAndLayout() {
        setContentView(R.layout.math_operations);
        questionView = findViewById(R.id.operationsQuestionId);
        answerView = findViewById(R.id.answerinputid);
        answerView.setText("");
    }

    @Override
    protected void setQuestion() {
        question = (MathOperationQuestion) getIntent().getSerializableExtra("question");
        if (question == null || question.getQuestion() == null) {
            Log.e("Math Operation", "Question Text not set.");
        } else {
            questionView.setText(question.getQuestion());
        }
        if(question.getAudio() != null){
            playSound(QuestionActivity.getResId(question.getAudio(), R.raw.class));
        }else{
            Log.e("Math Operation", "Question Audio not set.");
        }
        node = question.generateAnswerTree();
    }

    @Override
    protected QuestionActivity getThis() {
        return this;
    }

    @Override
    protected boolean isCorrectCategory(BaseValue value) {
        return (value instanceof Number || value instanceof Symbol);
    }

    @Override
    protected void updateAnswer() {
        String html = generateAnswerString();
        Log.d("UPDATE ANS", html);
        answerView.setText(Html.fromHtml(html));
    }


}