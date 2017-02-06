package speechdemo;

import java.io.*;
import java.net.*;
 
public class QAMatcherClient {
    PrintWriter out=null;
    BufferedReader in=null;
    String hostName =null;
    
    public QAMatcherClient(String host, int port) throws IOException {
        hostName = host;
        int portNumber = port;
 
        try {
            Socket kkSocket = new Socket(hostName, portNumber);
            out = new PrintWriter(kkSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }
    
    
    public String send(String fromUser){
    	String fromServer = "NO ANSWER"; 
    	try{
    		if (fromUser != null && in !=null && out!=null) {
           		out.println(fromUser);
           		if ((fromServer = in.readLine()) != null) {
                		System.out.println("FROM QAMATCHER: "+ fromServer);    
           		}
        	}
        } catch (IOException e){
            System.err.println("Couldn't send to the connection to " + hostName);
            System.exit(1);
        }
        return fromServer;
    }
}