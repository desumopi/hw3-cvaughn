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
  
  private static double THRESHOLD = 0.3;
  
  public void process(JCas aJCas) {
    System.out.println("CURRENTLY RUNNING PrecisionAnnotator.java");
    
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
    
    ArrayList<Integer> posAnsBegins = new ArrayList<Integer>();
    ArrayList<Integer> negAnsBegins = new ArrayList<Integer>();
    
    
    // for the first N AnswerScores, are they right?
    for (int x=0; x<N; x++) {
      //System.out.print(asArray.get(x).getScore()+" + ");
      if (asArray.get(x).getScore() >= THRESHOLD) {
        posAnsBegins.add(asArray.get(x).getBegin());
      } else {
        negAnsBegins.add(asArray.get(x).getBegin());
      }
    }
    //System.out.println();
    
    int tp = 0, fp = 0;
    
    for (int i=0; i < posAnsBegins.size(); i++) {
      for (int j=0; j < ansArray.size(); j++) {
        if (posAnsBegins.get(i) == ansArray.get(j).getBegin()) {
          // this is the right answer
          if (ansArray.get(j).getIsCorrect()) {
            tp++;
          } else {
            fp++;
          }
        }
      }
    }
    
    double precision = ((double)tp)/(double)(tp+fp);
    
    System.out.println("Precision at a threshold of " + THRESHOLD + " is " + precision + " = " + tp + "/" + (tp + fp));
    
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