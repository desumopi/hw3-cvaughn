package edu.cmu.deiis.annotators;

import java.util.ArrayList;
import java.util.Iterator;

import edu.cmu.deiis.types.*;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;


/**
 * Annotator that sorts answers by the number of NGrams they 
 * share with the question.
 */
public class PrecisionAnnotator extends JCasAnnotator_ImplBase {
  
  public void process(JCas aJCas) {
    
    String docText = aJCas.getDocumentText();
    
    // get the answers from the CAS (and store in ArrayList):
    int N=0;
    ArrayList<Answer> ansArray = new ArrayList<Answer>();
    
    FSIndex ansIndex = aJCas.getAnnotationIndex(Answer.type);
    Iterator ansIter = ansIndex.iterator();

    while (ansIter.hasNext()) {
      Answer currAns = (Answer) ansIter.next();
      ansArray.add(currAns);
    
      if (currAns.getIsCorrect()) {
        N++;
      }
    }
    
    // get an ArrayList of all the AnswerScores from the CAS
    ArrayList<AnswerScore> asArray = new ArrayList<AnswerScore>();
    
    FSIndex asIndex = aJCas.getAnnotationIndex(AnswerScore.type);
    Iterator asIter = asIndex.iterator();

    while (asIter.hasNext()) {
      AnswerScore currAS = (AnswerScore) asIter.next();
      asArray.add(currAS);
    }
    /*
    System.out.println("Before sorting:");
    for (int blah=0; blah < asArray.size(); blah++) {
      System.out.print(asArray.get(blah).getScore()+"   ");
    }
    System.out.println();*/
    
    // sort the AnswerScores into descending order:
    sortArray(asArray);
    
    /*
    //print array (troubleshooting):
    System.out.println("After sorting:");
    for (int blah=0; blah < asArray.size(); blah++) {
      System.out.print(asArray.get(blah).getScore()+"   ");
    }
    System.out.println();*/
    
    ArrayList<Integer> highAnsBegins = new ArrayList<Integer>();
    String label = "!";
    int tp = 0;
    
    // for the first N AnswerScores, how many are right?
    for (int x=0; x<asArray.size(); x++) {
      // highAnsBegins.add(asArray.get(x).getBegin());
      if (asArray.get(x).getAnswer().getIsCorrect()) {
        label = "+";
        if (x < N) {
          tp++;
        }
      } else {
        label = "-";
      }
      System.out.println(label + " " + doubleToString(asArray.get(x).getScore()) + " \"" + docText.substring(asArray.get(x).getBegin(), asArray.get(x).getEnd()) + "\"");
    }
    
    double precision = ((double)tp)/(double)(N);
    
    System.out.println("Precision at " + N + ": " + doubleToString(precision));
    
  }
  
  private String doubleToString(double doub) {
    doub = doub*100;
    int temp = (int) doub;
    double better = (double) temp;
    better = better/100.0;
    String ret = ""+better+"";
    return ret;
  }
  
  
  private void sortArray(ArrayList<AnswerScore> asArray) {
    int maxInd;
    AnswerScore temp;
    
    for (int i=0; i<asArray.size(); i++) {
      maxInd = i;
      for (int j=i+1; j<asArray.size(); j++) {
        if (asArray.get(j).getScore() > asArray.get(maxInd).getScore()) {
          maxInd = j;
        }
      }
      if (i != maxInd) {
        temp = asArray.get(i);
        asArray.set(i, asArray.get(maxInd));
        asArray.set(maxInd, temp);
      }
    }
  }
  
}