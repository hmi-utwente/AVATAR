package speechdemo;
import java.io.*;
import java.net.*;
import qamatcher.DialogStore;
import qamatcher.DomDialogsParser;
 
public class QAMatcher {
    
    String filename=null; // the specification of the matching Q and A's in directory resources
    DialogStore store;
    
    public QAMatcher(String fname) {

	if (fname==null) 
		filename = "vragen.xml";
	DomDialogsParser ddp = new DomDialogsParser(filename);
	store= ddp.getDialogStore();
     }
 
    public String send(String query, String attName, String attValue){
    	String answer = "Ik kannie verstaan";
	answer = store.bestMatch(query, attName, attValue);
	System.out.println("Best answer :"+ answer);
        return answer;
    }
    
}