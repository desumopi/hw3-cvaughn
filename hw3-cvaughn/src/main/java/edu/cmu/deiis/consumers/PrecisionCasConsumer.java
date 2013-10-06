/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package edu.cmu.deiis.consumers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.XMLSerializer;
import org.xml.sax.SAXException;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.AnswerScore;

/**
 * A simple CAS consumer that writes the CAS to XMI format.
 * <p>
 * This CAS Consumer takes one parameter:
 * <ul>
 * <li><code>OutputDirectory</code> - path to directory into which output files will be written</li>
 * </ul>
 */
public class PrecisionCasConsumer extends CasConsumer_ImplBase {
  /**
   * Name of configuration parameter that must be set to the path of a directory into which the
   * output files will be written.
   */
  public static final String PARAM_OUTPUTDIR = "OutputDirectory";

  private File mOutputDir;

  private int mDocNum;

  public void initialize() throws ResourceInitializationException {
    mDocNum = 0;
    mOutputDir = new File((String) getConfigParameterValue(PARAM_OUTPUTDIR));
    if (!mOutputDir.exists()) {
      mOutputDir.mkdirs();
    }
  }

  /**
   * Processes the CAS which was populated by the TextAnalysisEngines. <br>
   * In this case, the CAS is evaluated and then written to the XMI output file .
   * 
   * @param aCAS
   *          a CAS which has been populated by the TAEs
   * 
   * @throws ResourceProcessException
   *           if there is an error in processing the Resource
   * 
   * @see org.apache.uima.collection.base_cpm.CasObjectProcessor#processCas(org.apache.uima.cas.CAS)
   */
  public void processCas(CAS aCAS) throws ResourceProcessException {
    String modelFileName = null;

    JCas aJCas;
    try {
      aJCas = aCAS.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }
    
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
    
    sortArray(asArray);

    ArrayList<Integer> highAnsBegins = new ArrayList<Integer>();
    String label = "!";
    int tp = 0;
    
    String content = "";
    
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
      content = content + "\n" + label + " " + doubleToString(asArray.get(x).getScore()) +  " \"" + docText.substring(asArray.get(x).getBegin(), asArray.get(x).getEnd()) + "\"";
//      System.out.println(label + " " + doubleToString(asArray.get(x).getScore()) + " \"" + docText.substring(asArray.get(x).getBegin(), asArray.get(x).getEnd()) + "\"");
    }
    
    double precision = ((double)tp)/(double)(N);
    
    File outFile = new File(mOutputDir, "CPE_output.xmi");
    FileOutputStream out = null;
    
    content = content+"\nPrecision at "+N+": "+doubleToString(precision);
    
    try {
      // write the output to XMI:
      out = new FileOutputStream(outFile);
      byte[] bcont = content.getBytes();
      out.write(bcont);
      out.close();
    } catch (IOException e) {
      System.out.println("There was an IOException when writing to XMI.");
      System.out.println("The file "+outFile.toString()+" doesn't exist");
    }
    
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
