package sun2people.electria.pixam;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A model for tests
 */
public class Test {

    private int numberOfQuestions;
    private int numberOfOptions;
    private ArrayList<Integer> answers;

    public Test(){
        numberOfQuestions = 0;
        numberOfOptions = 0;
        answers = new ArrayList<>();
    }

    public Test(int numberOfQuestions, int numberOfOptions, ArrayList<Integer>answers){
        this.numberOfQuestions = numberOfQuestions;
        this.numberOfOptions = numberOfOptions;
        setAnswers(answers);
    }

    public void setNumberOfQuestions(int numQuestions){
        numberOfQuestions = numQuestions;
    }
    public void setNumberOfOptions(int numOptions){
        numberOfOptions = numOptions;
    }
    public void setAnswers(ArrayList<Integer> answers){
        this.answers = new ArrayList<>(answers);
    }
    public int getNumberOfQuestions(){
        return numberOfQuestions;
    }
    public int getNumberOfOptions(){
        return numberOfOptions;
    }
    public ArrayList<Integer> getAnswers(){
        return answers;
    }
}
