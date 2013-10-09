package edu.cmu.deiis.annotators;

import java.util.*;
import java.util.regex.Matcher;

import org.apache.uima.aae.client.UimaASProcessStatus;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.aae.monitor.statistics.AnalysisEnginePerformanceMetrics;
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.deiis.types.Token;

/**
 * Annotator that uses the Stanford CoreNLP to find Name Entities in a given text file.
 */
public class ScnlpClientAnnotator extends JCasAnnotator_ImplBase {
  
  UimaAsynchronousEngine uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();

  public void process(JCas aJCas) {

    // send aJCas to SCNLP

    // create Asynchronous Client API
    uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();
    // uimaAsEngine.addStatusCallbackListener(new MyStatusCallbackListener());

    // create Map to pass server URI and Endpoint parameters
    Map<String, Object> appCtx = new HashMap<String, Object>();
    // Add Broker URI on local machine
    appCtx.put(UimaAsynchronousEngine.ServerUri, "tcp://mu.lti.cs.cmu.edu:61616");
    // Add Queue Name
    appCtx.put(UimaAsynchronousEngine.Endpoint, "ScnlpQueue");
    // Add the Cas Pool Size
    appCtx.put(UimaAsynchronousEngine.CasPoolSize, 2);

    // initialize
    try {
      uimaAsEngine.initialize(appCtx);
    } catch (ResourceInitializationException e) {
      System.out
              .println("Tried to initialize uimaAsEngine, but encountered a ResourceInitializationException.");
      e.printStackTrace();
    }
    
    // get aJCas back from SCNLP


    
  }

  // Callback Listener. Receives event notifications from UIMA-AS.
  private class MyStatusCallbackListener extends UimaAsBaseCallbackListener {

    // Method called when the processing of a Document is completed.
    public void entityProcessComplete(CAS aCas, EntityProcessStatus aStatus) {
      if (aStatus != null && aStatus.isException()) {
        List exceptions = aStatus.getExceptions();
        for (int i = 0; i < exceptions.size(); i++) {
          ((Throwable) exceptions.get(i)).printStackTrace();
        }
        try {
          uimaAsEngine.stop();
        } catch (Exception e) {
          System.out.println("Couldn't stop uimaAsEngine. There was an exception.");
          e.printStackTrace();
        }
        return;
      }

      // Process the retrieved Cas here
      if (aStatus instanceof UimaASProcessStatus) {
        String casReferenceId = ((UimaASProcessStatus) aStatus).getCasReferenceId();
        List<AnalysisEnginePerformanceMetrics> metrics = ((UimaASProcessStatus) aStatus)
                .getPerformanceMetricsList();
      }
      // ...
    }

  }

  // Add other required callback methods below...
}