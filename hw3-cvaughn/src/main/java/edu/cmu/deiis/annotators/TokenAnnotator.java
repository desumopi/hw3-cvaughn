package edu.cmu.deiis.annotators;

import edu.cmu.deiis.types.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;


/**
 * Annotator that detects individual tokens using 
 * Java 1.4 regular expressions.
 */
public class TokenAnnotator extends JCasAnnotator_ImplBase {
  private Pattern tokenPattern = Pattern.compile("[a-zA-Z0-9']+");

  public void process(JCas aJCas) {
    System.out.println("CURRENTLY RUNNING TokenAnnotator.java");
    // get document text
    String docText = aJCas.getDocumentText();
    // search for tokens
    Matcher matcher = tokenPattern.matcher(docText);
    int pos = 0;
    while (matcher.find(pos)) {
      // found one - create annotation
      Token annotation = new Token(aJCas);
      annotation.setBegin(matcher.start());
      annotation.setEnd(matcher.end());
      annotation.addToIndexes();
 
      pos = matcher.end();
    }
  }
}