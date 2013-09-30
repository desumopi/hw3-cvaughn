package edu.cmu.deiis.annotators;

import edu.cmu.deiis.types.*;

import java.util.*;
import java.util.regex.*;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;


/**
 * Annotator that detects unigrams, bigrams, and trigrams 
 * of Tokens that have been extracted from a given text.
 */
public class NgramAnnotator extends JCasAnnotator_ImplBase {

  public void process(JCas aJCas) {
    System.out.println("CURRENTLY RUNNING NgramAnnotator.java");
    // get document text
    String docText = aJCas.getDocumentText();

    ArrayList<Token> tokArray = new ArrayList<Token>();

    FSIndex tokIndex = aJCas.getAnnotationIndex(Token.type);
    Iterator tokIter = tokIndex.iterator();

    while (tokIter.hasNext()) {
      Token currTok = (Token) tokIter.next();
      tokArray.add(currTok);
    }

    int temp = 0;

    int N = tokArray.size();

    for (int i = 0; i < N; i++) {

      temp++;
      Token curr = tokArray.get(i);
      
      if (!isNewLine(docText, curr) && !isScore(docText, curr)) {
        
        FSArray uniArray = new FSArray(aJCas, 1);
        uniArray.set(0, curr);
        
        NGram unigram = new NGram(aJCas);
        unigram.setElements(uniArray);
        unigram.setBegin(curr.getBegin());
        unigram.setEnd(curr.getEnd());
        unigram.addToIndexes();

        if (i > 0) {
          Token prev = tokArray.get(i - 1);
          if (!isScore(docText, prev) && !isNewLine(docText, prev)) {
            // we can make a bigram!
            FSArray biArray = new FSArray(aJCas, 2);
            biArray.set(0, prev);
            biArray.set(1, curr);
  
            NGram bigram = new NGram(aJCas);
            bigram.setElements(biArray);
            bigram.setBegin(prev.getBegin());
            bigram.setEnd(curr.getEnd());
            bigram.addToIndexes();
  
            if (i > 1) {
              Token penult = tokArray.get(i - 2);
              if (!isScore(docText, penult) && !isNewLine(docText, penult)) {
                // we can make a trigram!
                FSArray triArray = new FSArray(aJCas, 3);
                triArray.set(0, penult);
                triArray.set(1, prev);
                triArray.set(2, curr);
    
                NGram trigram = new NGram(aJCas);
                trigram.setElements(triArray);
                trigram.setBegin(penult.getBegin());
                trigram.setEnd(curr.getEnd());
                trigram.addToIndexes();
              }
            }
          }
        }
      } else {

      }
    }
  }

  private boolean isNewLine(String docText, Token tok) {

    int begin = tok.getBegin();
    int end = tok.getEnd();

    String tokString = docText.substring(begin, end);

    if (tokString.equals("A") || tokString.equals("Q")) {
      return true;
    } else {
      return false;
    }
  }
  
  private boolean isScore(String docText, Token tok) {
    int begin = tok.getBegin();
    int end = tok.getEnd();

    String tokString = docText.substring(begin, end);
    if (tokString.equals("0") || tokString.equals("1")) {
      return true;
    } else {
      return false;
    } 
  }
  
}
