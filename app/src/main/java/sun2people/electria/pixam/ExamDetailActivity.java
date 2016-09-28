package sun2people.electria.pixam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ExamDetailActivity extends Activity {

    private static final int STATE_DISPLAY_SCORE = 1;
    private static final int STATE_SCAN_IMAGE = 0;

    private TextView scanText, retBtn;
    private RelativeLayout scanView;
    private ArrayList<Integer> mUserAnsers;
    private Test mTest;
    private int mState;
    private float mTestScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_detail);

        mState = STATE_SCAN_IMAGE;
        int[] arr = {2, 4, 0, 2, 3, 1, 1, 4, 4, 3};
        ArrayList<Integer> answers = new ArrayList<>();
        for(int i = 0; i < 10; i++)
            answers.add((arr[i]));
        mTest = new Test(10, 5, answers);

        scanText = (TextView) findViewById(R.id.start_scan);
        retBtn = (TextView) findViewById(R.id.ret_btn);
        scanView = (RelativeLayout) findViewById(R.id.scan_view);
        scanView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExamDetailActivity.this, CameraActivity.class);
                intent.putExtra(CameraActivity.IMAGE_PARAMETERS, mTest.getNumberOfOptions()+
                "-"+mTest.getNumberOfQuestions());
                startActivityForResult(intent, CameraActivity.SCAN_IMAGE_REQUEST);
            }
        });

        retBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CameraActivity.SCAN_IMAGE_REQUEST ){
            if(resultCode == RESULT_OK){
                mUserAnsers = data.getIntegerArrayListExtra(CameraActivity.EXTRA_USER_ANSWERS);
                mTestScore = scoreUser(mTest.getAnswers(), mUserAnsers);
                String scoreStr = mTestScore + "% of answers are correct";
                scanText.setText(scoreStr);
                retBtn.setVisibility(View.VISIBLE);
                scanView.setClickable(false);
                mState = STATE_DISPLAY_SCORE;
            }
        }
    }

    private float scoreUser(ArrayList<Integer> testAnswers, ArrayList<Integer> userAnswers){
        if(testAnswers.size() != userAnswers.size())
            return 0;
        float score = 0;
        for(int i = 0; i < testAnswers.size(); i++){
            if(testAnswers.get(i).equals(userAnswers.get(i))){
                score++;
            }
        }
        score = score*100/testAnswers.size();
        return score;
    }

    private void returnToScan(){
        mState = STATE_SCAN_IMAGE;
        scanText.setText(R.string.scan_image);
        scanView.setClickable(true);
        retBtn.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if(mState == STATE_DISPLAY_SCORE){
            returnToScan();
        }
        else
            super.onBackPressed();
    }
}
