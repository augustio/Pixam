package sun2people.electria.pixam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ExamDetailActivity extends Activity {

    private static final int QUESTION_DISPLAY = 0;
    private static final int INSTRUCTION_DISPLAY = 1;

    private int mState;
    private LinearLayout questionView, instructionView;
    private TextView questionItem, questionNum, startButton, nextButton;
    private String mExamTitle, mQuestions[];
    private int mNextQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_detail);

        questionView = (LinearLayout) findViewById(R.id.question);
        instructionView = (LinearLayout) findViewById(R.id.instruction);
        questionItem = (TextView) findViewById(R.id.question_item);
        questionNum = (TextView) findViewById(R.id.question_num);
        startButton = (TextView) findViewById(R.id.start_exam);
        nextButton = (TextView) findViewById(R.id.next_btn);
        questionView.setVisibility(View.GONE);

        mNextQuestion = 0;

        mState = INSTRUCTION_DISPLAY;

        mQuestions = new String[10];
        for(int i = 0; i < mQuestions.length; i++){
            String q = "\nWhich of the following is not a discipline in" +
                    " Chemistry? \n\n a) organic \n\n b)biochemistry \n\n" +
                    "c) molecular \n\n d)analytical \n\n e) none of the above?";
            mQuestions[i] = q;
        }

        Intent intent = getIntent();
        mExamTitle = intent.getStringExtra(Intent.EXTRA_TEXT);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                instructionView.setVisibility(View.GONE);
                String str = (mNextQuestion +1) + "/" + mQuestions.length;
                questionNum.setText(str);
                questionItem.setText(mQuestions[mNextQuestion]);
                mNextQuestion++;
                mState = QUESTION_DISPLAY;
                instructionView.setVisibility(View.GONE);
                questionView.setVisibility(View.VISIBLE);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mNextQuestion < mQuestions.length){
                    String str = (mNextQuestion +1) + "/" + mQuestions.length;
                    questionNum.setText(str);
                    questionItem.setText(mQuestions[mNextQuestion]);
                    mNextQuestion++;
                }else{
                    mNextQuestion = 0;
                    Intent intent = new Intent(ExamDetailActivity.this, EvaluateExam.class);
                    intent.putExtra(Intent.EXTRA_TEXT, mExamTitle);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(mState == QUESTION_DISPLAY){
            mState = INSTRUCTION_DISPLAY;
            questionView.setVisibility(View.GONE);
            instructionView.setVisibility(View.VISIBLE);
        }else
            super.onBackPressed();
    }
}
