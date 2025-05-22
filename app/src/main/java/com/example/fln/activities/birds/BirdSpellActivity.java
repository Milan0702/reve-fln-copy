package com.example.fln.activities.birds;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.fln.activities.QuestionActivity;
import com.example.fln.R;
import com.example.fln.questions.birds.BirdSpellQuestion;
import com.example.fln.tokens.BaseValue;
import com.example.fln.tokens.Alphabet;

@RequiresApi(api = Build.VERSION_CODES.S)
public class BirdSpellActivity extends QuestionActivity {
    private ImageView questionView;
    private TextView answerView;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.currentIndex = 0;
    }

    @Override
    protected void setViewsAndLayout() {
        setContentView(R.layout.birds_spell);
        questionView = findViewById(R.id.mainImageView);
        answerView = findViewById(R.id.answerinputid);
        answerView.setText("");
    }

    @Override
    protected void setQuestion() {
        question = (BirdSpellQuestion) getIntent().getSerializableExtra("question");
        if (question == null || question.getImage() == null) {
            Log.e("Bird Spell", "Question Image not set.");
        } else {
            questionView.setImageResource(getResources().getIdentifier(question.getImage(), "drawable", getPackageName()));
        }
        node = question.generateAnswerTree();
    }

    @Override
    protected QuestionActivity getThis() {
        return this;
    }

    @Override
    protected boolean isCorrectCategory(BaseValue value) {
        return (value instanceof Alphabet);
    }

    @Override
    protected void updateAnswer() {
        String html = generateAnswerString();
        Log.d("UPDATE ANS", html);
        answerView.setText(Html.fromHtml(html));
    }
}