package sun2people.electria.pixam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class EvaluateExam extends Activity {

    private static final int STATE_DISPLAY_SCORE = 0;
    private static final int STATE_SCAN_IMAGE = 1;
    private static final int STATE_DISPLAY_STUDENT_LIST = 2;

    private TextView scanText, studentNameTv, examTitleTv, scoreTv;
    private RelativeLayout scanView;
    private LinearLayout reportView;
    private ListView studentList;
    private ArrayList<Integer> mUserAnsers;
    private Test mTest;
    private int mState;
    private float mTestScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate_exam);

        mState = STATE_DISPLAY_STUDENT_LIST;
        ArrayList<Integer> answers = new ArrayList<>();
        for(int i = 0; i < 10; i++)
            answers.add(0);
        mTest = new Test(10, 5, answers);

        studentList = (ListView) findViewById(R.id.student_list);
        scanView = (RelativeLayout) findViewById(R.id.scan_view);
        reportView = (LinearLayout) findViewById(R.id.report_view);
        studentNameTv = (TextView) findViewById(R.id.student_name_tv);
        examTitleTv = (TextView) findViewById(R.id.exam_title_tv);
        scoreTv = (TextView) findViewById(R.id.score_tv);
        scanText = (TextView) findViewById(R.id.start_scan);
        scanView.setVisibility(View.GONE);
        reportView.setVisibility(View.GONE);

        Intent intent = getIntent();
        if(intent != null){
            String courseTitle = intent.getStringExtra(Intent.EXTRA_TEXT);
            examTitleTv.setText(courseTitle);
        }

        scanView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EvaluateExam.this, CameraActivity.class);
                intent.putExtra(CameraActivity.IMAGE_PARAMETERS, mTest.getNumberOfOptions()+
                        "-"+mTest.getNumberOfQuestions());
                startActivityForResult(intent, CameraActivity.SCAN_IMAGE_REQUEST);
            }
        });

        final String students[] = new String[10];
        for(int i = 0; i < students.length; i++)
            students[i] = "Student_" + i;

        ArrayAdapter<String> studentListAdapter =
                new ArrayAdapter<>(this, R.layout.student_view, students);

        studentList.setAdapter(studentListAdapter);
        studentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                studentNameTv.setText(students[i]);
                studentList.setVisibility(View.GONE);
                reportView.setVisibility(View.GONE);
                mState = STATE_SCAN_IMAGE;
                scanView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CameraActivity.SCAN_IMAGE_REQUEST ){
            if(resultCode == RESULT_OK){
                mUserAnsers = data.getIntegerArrayListExtra(CameraActivity.EXTRA_USER_ANSWERS);
                mTestScore = scoreUser(mTest.getAnswers(), mUserAnsers);
                String scoreStr = mTestScore + "%";
                scanView.setVisibility(View.GONE);
                studentList.setVisibility(View.GONE);
                scoreTv.setText(scoreStr);
                mState = STATE_DISPLAY_SCORE;
                reportView.setVisibility(View.VISIBLE);
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

    @Override
    public void onBackPressed() {
        switch (mState){
            case STATE_DISPLAY_SCORE:
            case STATE_SCAN_IMAGE:
                reportView.setVisibility(View.GONE);
                scanView.setVisibility(View.GONE);
                studentList.setVisibility(View.VISIBLE);
                mState = STATE_DISPLAY_STUDENT_LIST;
                break;
            case STATE_DISPLAY_STUDENT_LIST:
                super.onBackPressed();
        }
    }
}