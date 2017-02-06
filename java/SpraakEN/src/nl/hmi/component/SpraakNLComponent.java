package nl.hmi.component;

import nl.hmi.SpraakNL.SpeechAPI;
import nl.hmi.middleware.StompMiddleware;
import ee.ioc.phon.netspeechapi.duplex.RecognitionEvent;
import ee.ioc.phon.netspeechapi.duplex.RecognitionEventListener;
import pk.aamir.stompj.MessageHandler;
import pk.aamir.stompj.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpraakNLComponent implements RecognitionEventListener, MessageHandler
{

  public StompMiddleware stomp;
  public SpeechAPI sapi;
  public String liveTopic = "/topic/speechLive";
  public String lastTopic = "/topic/speechFinal";


  private boolean closed = false;

  public SpraakNLComponent()
  {
    String apolloIP = "127.0.0.1";
    int apolloPort = 61613;
    this.stomp = new StompMiddleware(apolloIP, apolloPort, this);
    this.sapi = new SpeechAPI(this);
    //this.stomp.subscribe("/topic/bmlFeedback", this);
  }

  public static void main(String[] args)
  {
    SpraakNLComponent snlc = new SpraakNLComponent();
  }


  public void onRecognitionEvent(RecognitionEvent event)
  {
    String trans = event.getResult().getHypotheses().get(0).getTranscript();

    System.out.println("COMP " + trans);

    if (event.getResult().isFinal())
    {
      stomp.sendString(trans, lastTopic);
    } else
    {
      stomp.sendString(trans, liveTopic);
    }
  }

  public void onClose()
  {
    closed = true;
    this.notifyAll();
  }

  public boolean isClosed()
  {
    return closed;
  }

  public void onMessage(Message m)
  {
    // todo: ctrl component?
  }
}
