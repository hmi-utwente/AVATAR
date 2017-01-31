package nl.hmi.middleware;

import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

import nl.hmi.component.SpraakNLComponent;
import pk.aamir.stompj.Connection;
import pk.aamir.stompj.MessageHandler;
import pk.aamir.stompj.Message;
import pk.aamir.stompj.ErrorHandler;
import pk.aamir.stompj.ErrorMessage;
import pk.aamir.stompj.StompJException;

/**
 * Simple implementation of using the stompj library to communicate bml with ASAP
 * only documentation I found for stompj is here:
 * https://code.google.com/archive/p/stompj/wikis/GettingStarted.wiki
 *
 * @author jkolkmeier
 * @author jlinssen
 */
public class StompMiddleware implements ErrorHandler
{

  private Connection con;
  private int bmlId;

  public StompMiddleware(String apolloIP, int apolloPort, MessageHandler mh)
  {
    bmlId = 0;

    try
    {
      con = new Connection(apolloIP, apolloPort, "admin", "password");
      con.setErrorHandler(this);
      con.connect();
    }
    catch (StompJException e)
    {
      System.out.println("Error while initialising STOMP connection: " + e.getMessage());
      e.printStackTrace();
      return;
    }
  }

  public void subscribe(String topic, MessageHandler mh)
  {
    con.subscribe(topic, true);
    con.addMessageHandler(topic, mh);
  }

  /**
   * Sends a string to a certain topic.
   *
   * @param str
   * @param outTopic
   */
  public void sendString(String str, String outTopic)
  {
    String prefix = "{ \"sentence\": \"";
    String suffix = "\" } }";

    con.send(prefix + str + suffix, outTopic);
  }

  public void sendBml(String bml, String outTopic)
  {
    String prefix = "{ \"bml\": { \"content\": \"";
    String suffix = "\" } }";
    try
    {
      con.send(prefix + URLEncoder.encode(bml, "UTF-8") + suffix, outTopic);
    }
    catch (UnsupportedEncodingException e)
    {
      System.out.println("[sendBml] Encoding failed.");
    }
  }

  public void say(String text, String outTopic)
  {
    String prefix = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml" + (++bmlId) + "\"><speech id=\"speech1\" start=\"0\"><text>";
    String suffix = "</text></speech></bml>";
    sendBml(prefix + text + suffix, outTopic);
  }

  public void onError(ErrorMessage err)
  {
    System.out.println("[StompMiddleware.onError] " + err.getMessage());
  }

}
