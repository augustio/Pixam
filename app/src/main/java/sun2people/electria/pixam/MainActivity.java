package sun2people.electria.pixam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String subjects[] = {"English", "Maths", "Physics", "Chemistry", "Biology", "Literature",
                "Economics", "Geography"};

        ArrayAdapter<String> subjectListAdapter =
                new ArrayAdapter<>(this, R.layout.subject_view, subjects);

        final GridView subjectsView = (GridView) findViewById(R.id.subject_list);
        subjectsView.setAdapter(subjectListAdapter);

        subjectsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, ExamActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, subjects[i]);
                startActivity(intent);
            }
        });
    }
}