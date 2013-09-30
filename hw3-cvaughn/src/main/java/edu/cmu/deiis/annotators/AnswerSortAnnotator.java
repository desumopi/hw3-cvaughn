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
public class AnswerSortAnnotator extends JCasAnnotator_ImplBase {
  
  public void process(JCas aJCas) {
    System.out.println("CURRENTLY RUNNING AnswerSortAnnotator.java");
    
    String docText = aJCas.getDocumentText();
    
    // get the question:
    FSIndex questIndex = aJCas.getAnnotationIndex(Question.type);
    Iterator questIter = questIndex.iterator();
    Question question = (Question) questIter.next();
        
    // get the answers from the CAS (and store in ArrayList):
    ArrayList<Answer> ansArray = new ArrayList<Answer>();
    
    FSIndex ansIndex = aJCas.getAnnotationIndex(Answer.type);
    Iterator ansIter = ansIndex.iterator();

    while (ansIter.hasNext()) {
      Answer currAns = (Answer) ansIter.next();
      ansArray.add(currAns);
    }
    
    // get all the NGrams from the CAS (and store in ArrayList):
    ArrayList<NGram> ngArray = new ArrayList<NGram>();
    
    FSIndex ngIndex = aJCas.getAnnotationIndex(NGram.type);
    Iterator ngIter = ngIndex.iterator();

    while (ngIter.hasNext()) {
      NGram currNG = (NGram) ngIter.next();
      ngArray.add(currNG);
    }
    
    // get the question's associated NGrams (and those NGs' Strings):
    int qBegin = question.getBegin();
    int qEnd = question.getEnd();
    ArrayList<NGram> qArrayNG = new ArrayList<NGram>();
    ArrayList<String> qArrayStr = new ArrayList<String>();
    
    for (int i=0; i<ngArray.size(); i++) {
      NGram curr = ngArray.get(i);
      if (curr.getBegin() >= qBegin && curr.getEnd() <= qEnd) {
        // the NGram curr is in the question
        qArrayNG.add(curr);
        qArrayStr.add(docText.substring(curr.getBegin(), curr.getEnd()));
      }
    }
    
    double[] ansScores = new double[ansArray.size()];
    // for each answer, compare its NGrams to the question's:
    for (int j=0; j<ansArray.size(); j++) {
      Answer answer = ansArray.get(j);
      int aBegin = answer.getBegin();
      int aEnd = answer.getEnd();
      ArrayList<NGram> aArrayNG = new ArrayList<NGram>();
      ArrayList<String> aArrayStr = new ArrayList<String>();
      
      // store the NGrams for this answer (and their Strings) in an ArrayList:
      for (int k=0; k<ngArray.size(); k++) {
        NGram curr = ngArray.get(k);
        if (curr.getBegin() >= aBegin && curr.getEnd() <= aEnd) {
          // the NGram curr is in the answer at hand
          aArrayNG.add(curr);
          aArrayStr.add(docText.substring(curr.getBegin(), curr.getEnd()));
        }
      }
      
      // check how many of the NGram Strings from this answer are in the question:
      double score = 0.0;
      int total = 0;
      boolean inQuest = false;
      for (int x=0; x < aArrayStr.size(); x++) {
        for (int y=0; y < qArrayStr.size(); y++) {
          if (aArrayStr.get(x).equals(qArrayStr.get(y))) {
            inQuest = true;
          }
        }
        if (inQuest) {
          score += 1.0;
          inQuest = false;
        }
        total++;
      }
      
      score = score/(double)total;
      ansScores[j] = score;
      
      AnswerScore as = new AnswerScore(aJCas);
      as.setAnswer(answer);
      as.setScore(score);
      as.setBegin(answer.getBegin());
      as.setEnd(answer.getEnd());
      as.addToIndexes();
      
    }
    
  }
}
