package sun2people.electria.pixam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class ExamActivity extends Activity {

    private String mSubject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        final String exams[] = {"Rehearsal Exam 1", "Rehearsal Exam 2", "Rehearsal Exam 3", "Rehearsal Exam 4"};

        ArrayAdapter<String> subjectListAdapter =
                new ArrayAdapter<>(this, R.layout.exam_view,exams);

        TextView examsTitle = (TextView)findViewById(R.id.exams_title);
        Intent intent = getIntent();
        mSubject = intent.getStringExtra(Intent.EXTRA_TEXT);
        examsTitle.setText(mSubject);

        GridView examsView = (GridView) findViewById(R.id.exam_list);
        examsView.setAdapter(subjectListAdapter);
        examsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0) {
                    Intent intent = new Intent(ExamActivity.this, ExamDetailActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, mSubject + "_" + exams[i]);
                    startActivity(intent);
                }
            }
        });
    }
}
