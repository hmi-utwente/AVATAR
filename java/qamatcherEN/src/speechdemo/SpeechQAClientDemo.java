package speechdemo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ee.ioc.phon.netspeechapi.duplex.DuplexRecognitionSession;
import ee.ioc.phon.netspeechapi.duplex.RecognitionEvent;
import ee.ioc.phon.netspeechapi.duplex.RecognitionEventListener;
import ee.ioc.phon.netspeechapi.duplex.WsDuplexRecognitionSession;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

interface WorkerCountInterface {
	public abstract void notifyWorkerCount(int count);
}

class WorkerCountClient extends WebSocketClient {

	private WorkerCountInterface handler;
	
	public WorkerCountClient(URI serverURI, WorkerCountInterface handler) {
		super(serverURI);
		this.handler = handler;
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
	}

	@Override
	public void onError(Exception arg0) {
			System.err.println("****** Exception: "+arg0);
	}

	@Override
	public void onMessage(String msg) {
		Object obj = JSONValue.parse(msg);

		if ( obj != null ) {
			JSONObject jsonObj = (JSONObject) obj;
			if (jsonObj.containsKey("num_workers_available")) {
				Object lo = jsonObj.get("num_workers_available");
				int n_workers = ((Long)lo).intValue();
				this.handler.notifyWorkerCount( n_workers );
			}
		}
	}
	
	@Override
	public void onOpen(ServerHandshake arg0) {
	}

}
public class SpeechQAClientDemo extends JFrame {

	private static QAMatcher qam;
	// the stance attribute value that of the responding agent.
	static String attName = "stance";
	static String attValue= "aggressive";
	 
	private static final int FONTSIZE = 14;
	
	private static final long serialVersionUID = 1L;
	
	private static JTextArea textArea;
	private static JTextArea resultArea;
	
	boolean stopCapture = false;
	static boolean bufferFilled = false;
	static boolean workersAvailable = false;
	
	final static JButton filechooseBtn = new JButton("Select File");
	final static JButton captureBtn = new JButton("Capture");
	final static JButton stopBtn = new JButton("Stop");
	final static JButton playBtn = new JButton("Recognize");
	final static JCheckBox liveBox = new JCheckBox("Live Recognition");
	
	private static JLabel statusLabel = new JLabel("Available Slots: ");
	
	static ByteArrayOutputStream byteArrayOutputStream;
	AudioFormat audioFormat;
	TargetDataLine targetDataLine;
	static AudioInputStream audioInputStream;
	
	public static final String DEFAULT_WS_URL = "ws://nlspraak.ewi.utwente.nl:8888/client/ws/speech";
	public static final String DEFAULT_WS_STATUS_URL = "ws://nlspraak.ewi.utwente.nl:8888/client/ws/status";
	
	static class RecognitionEventAccumulator implements RecognitionEventListener, WorkerCountInterface {

		private List<RecognitionEvent> events = new ArrayList<RecognitionEvent>();
		private boolean closed = false;
		
		public void notifyWorkerCount(int count) {
			if (count>0) {
				workersAvailable = true;
				filechooseBtn.setEnabled(true);
				captureBtn.setEnabled(true);
				if(bufferFilled) {
					playBtn.setEnabled(true);
				}
			} else {
				workersAvailable = false;
				filechooseBtn.setEnabled(false);
				playBtn.setEnabled(false);
				if (liveBox.isSelected()) {
					captureBtn.setEnabled(false);
				}
			}
			statusLabel.setText("Available slots: "+count);
			System.err.println("****** N_WORKERS = "+count);
		}
		
		public void onClose() {
			closed = true;
			this.notifyAll();
		}

		public void onRecognitionEvent(RecognitionEvent event) {
			events.add(event);
			String finalresult;
//			System.err.println("Got event:" + event);
//			System.err.println("Position: " + textArea.getCaretPosition());
			textArea.setText(event.getResult().getHypotheses().get(0).getTranscript());
			textArea.update(textArea.getGraphics());

// ===== Handle final recognition result from speech recognizer =====================

			if (event.getResult().isFinal()) {
				finalresult = event.getResult().getHypotheses().get(0).getTranscript();
//				String answer = qam.send(finalresult,attName,attValue);
//				System.out.println("QUESTION: "+finalresult +"\n" + "ANSWER:  " + answer);
				resultArea.append(finalresult + " (" + String.format("%.3f", event.getResult().getHypotheses().get(0).getConfidence()) + ")\n");
				resultArea.update(resultArea.getGraphics());
			}	
		}

		public List<RecognitionEvent> getEvents() {
			return events;
		}

		public boolean isClosed() {
			return closed;
		}
	}
	
	public static String testRecognition(File SpeechFile) throws MalformedURLException, IOException, URISyntaxException, InterruptedException {
		RecognitionEventAccumulator eventAccumulator = new RecognitionEventAccumulator();
		WsDuplexRecognitionSession session = new WsDuplexRecognitionSession(DEFAULT_WS_URL);
		session.addRecognitionEventListener(eventAccumulator);
		
		String extension=SpeechFile.getName().substring(SpeechFile.getName().lastIndexOf(".") + 1);
		if (extension.equals("flac")) {
			session.setContentType("audio/x-flac, layout=(string)interleaved, rate=(int)16000, format=(string)S16LE, channels=(int)1");
		}
		
		session.setUserId("laurensw");
		session.setContentId("HelloWorld");
		
		session.connect();
		
		sendFile(session, SpeechFile, 16000*2);
		while (!eventAccumulator.isClosed()) {
			synchronized (eventAccumulator) {
				eventAccumulator.wait(1000);
			}
		}
		
		RecognitionEvent lastEvent = eventAccumulator.getEvents().get(eventAccumulator.getEvents().size() - 2);
		String result="";
		result=lastEvent.getResult().getHypotheses().get(0).getTranscript();
		return result;
	}	
	
	private static void sendFile(DuplexRecognitionSession session, File file, int bytesPerSecond) throws IOException, InterruptedException {
		InputStream in = new FileInputStream(file);
		int chunksPerSecond = 4;
		
		byte buf[] = new byte[bytesPerSecond / chunksPerSecond];
		
		while (true) {
			long millisWithinChunkSecond = System.currentTimeMillis() % (1000 / chunksPerSecond);
			int size = in.read(buf);
			System.err.println("File size:" + size);
			if (size < 0) {
				byte buf2[] = new byte[0];
				session.sendChunk(buf2, true);
				break;
			}
			
			if (size == bytesPerSecond / chunksPerSecond) {
				session.sendChunk(buf, false);
			} else {
				byte buf2[] = Arrays.copyOf(buf, size);
				session.sendChunk(buf2, true);
				break;
			}
			Thread.sleep(1000/chunksPerSecond - millisWithinChunkSecond);
		}
		
		in.close();
	}
	
	private static void sendStream(DuplexRecognitionSession session, ByteArrayOutputStream bAOS) throws IOException, InterruptedException {
		int chunksPerSecond = 4;
		
		byte audioData[] = bAOS.toByteArray();
		InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
		AudioFormat audioFormat = getAudioFormat();
		int bytesPerSecond = (int) (audioFormat.getSampleRate() * 2);
		
		byte buf[] = new byte[bytesPerSecond / chunksPerSecond];
		
		while (true) {
			long millisWithinChunkSecond = System.currentTimeMillis() % (1000 / chunksPerSecond);
			
			int size = byteArrayInputStream.read(buf, 0, buf.length);
			
			System.err.println("Chunk size:" + size);
			if (size < 0) {
				byte buf2[] = new byte[0];
				session.sendChunk(buf2, true);
				break;
			}
			
			if (size == bytesPerSecond / chunksPerSecond) {
				session.sendChunk(buf, false);
			} else {
				byte buf2[] = Arrays.copyOf(buf, size);
				session.sendChunk(buf2, true);
				break;
			}
			Thread.sleep(1000/chunksPerSecond - millisWithinChunkSecond);
		}
	}
	
	public static void main(String[] args) { 
		
		new SpeechQAClientDemo();
		
	}
	
	private static void RecognizeAudio() throws MalformedURLException, IOException, URISyntaxException, InterruptedException {
		RecognitionEventAccumulator eventAccumulator = new RecognitionEventAccumulator();
		WsDuplexRecognitionSession session = new WsDuplexRecognitionSession(DEFAULT_WS_URL);
		session.addRecognitionEventListener(eventAccumulator);
		session.setUserId("laurensw");
		session.setContentId("SpeechAPIDemo");
		
		session.connect();
		
		sendStream(session, byteArrayOutputStream);
	}
	
	public SpeechQAClientDemo() {

		try {
			RecognitionEventAccumulator statusEventAccumulator = new RecognitionEventAccumulator();
			URI statusUri = new URI(DEFAULT_WS_STATUS_URL);
			WorkerCountClient status_session = new WorkerCountClient(statusUri,statusEventAccumulator);
			status_session.connect();
		} catch (Exception e3){
			System.err.println("Caught Exception: " + e3.getMessage());
		}	
        
		filechooseBtn.setFont(new Font("Arial", Font.PLAIN, FONTSIZE));
		captureBtn.setFont(new Font("Arial", Font.PLAIN, FONTSIZE));
		stopBtn.setFont(new Font("Arial", Font.PLAIN, FONTSIZE));
		playBtn.setFont(new Font("Arial", Font.PLAIN, FONTSIZE));
		liveBox.setFont(new Font("Arial", Font.PLAIN, FONTSIZE));
		
		statusLabel.setFont(new Font("Arial", Font.PLAIN, FONTSIZE));
		
		filechooseBtn.setEnabled(true);
		captureBtn.setEnabled(true);
		stopBtn.setEnabled(false);
		playBtn.setEnabled(false);
		
		getContentPane().add(filechooseBtn);
		filechooseBtn.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e){
					JFileChooser fc = new JFileChooser();
					int returnVal = fc.showOpenDialog(getParent());
					if(returnVal == JFileChooser.APPROVE_OPTION) {
						try { 
							testRecognition(fc.getSelectedFile().getAbsoluteFile());
						} catch (IOException|URISyntaxException|InterruptedException e2 ) {
							System.err.println("Caught Exception: " + e2.getMessage());
						}
					}
				}
			}
		);
		
		//Register anonymous listeners
		captureBtn.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e){
					captureBtn.setEnabled(false);
					stopBtn.setEnabled(true);
					playBtn.setEnabled(false);
					//Capture input data from the microphone until the Stop button is clicked.
					WsDuplexRecognitionSession session = null;
					if (liveBox.isSelected()) {
						try {
							RecognitionEventAccumulator eventAccumulator = new RecognitionEventAccumulator();
							session = new WsDuplexRecognitionSession(DEFAULT_WS_URL);
							session.addRecognitionEventListener(eventAccumulator);
							session.setUserId("laurensw");
							session.setContentId("SpeechAPIDemo");
							session.connect();
						} catch (Exception e2){
							System.err.println("Caught Exception: " + e2.getMessage());
						}
					}
					captureAudio(session);
				}
			}
		);
		getContentPane().add(captureBtn);
		
		stopBtn.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e){
					captureBtn.setEnabled(true);
					stopBtn.setEnabled(false);
					playBtn.setEnabled(true);
					bufferFilled = true;
					//Terminate the capturing of input data from the microphone.
					stopCapture = true;
					targetDataLine.close();
				}
			}
		);
		getContentPane().add(stopBtn);
		
		playBtn.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e){
					// Perform recognition on captures audio
					try {
						RecognizeAudio();
					} catch (IOException|URISyntaxException|InterruptedException e2 ) {
						System.err.println("Caught Exception: " + e2.getMessage());
					}
				}
			}
		);
		getContentPane().add(playBtn);
		
		getContentPane().add(liveBox);
		
		textArea = new JTextArea(3,30);
		resultArea = new JTextArea(10,30);
		JScrollPane scrollPane = new JScrollPane(resultArea);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setFont(new Font("Arial", Font.PLAIN, FONTSIZE));
		resultArea.setEditable(false);
		resultArea.setLineWrap(true);
		resultArea.setFont(new Font("Arial", Font.PLAIN, FONTSIZE));
		
		getContentPane().add(textArea);
		getContentPane().add(scrollPane);
		
		getContentPane().add(statusLabel);
		
		getContentPane().setLayout(new FlowLayout());
		setTitle("Capture/Recognize Demo");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600,600);
		setVisible(true);
		pack();
	}
	
	//This method captures audio input from a microphone and saves it in a ByteArrayOutputStream object.
	private void captureAudio(DuplexRecognitionSession session){
		try{
			//Get everything set up for capture
			audioFormat = getAudioFormat();
			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			targetDataLine.open(audioFormat);
			targetDataLine.start();

			// Create a thread to capture the microphone data and start it running.
			// It will run until the Stop button is clicked.
			Thread captureThread = new Thread(new CaptureThread(session));
			captureThread.start();
		} catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}
	}
	
	private static AudioFormat getAudioFormat(){
		float sampleRate = 16000.0F;	//8000,11025,16000,22050,44100
		int sampleSizeInBits = 16;		//8,16
		int channels = 1;				//1,2
		boolean signed = true;			//true,false
		boolean bigEndian = false;		//true,false
		return new AudioFormat(
				sampleRate,
				sampleSizeInBits,
				channels,
				signed,
				bigEndian);
	}
	
	//This thread puts the captured audio in the ByteArrayOutputStream object, and optionally sends it
	//to the speech server for live recognition.
	class CaptureThread extends Thread{
		private DuplexRecognitionSession session;
		//An arbitrary-size temporary holding buffer
		CaptureThread (DuplexRecognitionSession session) {
			this.session=session;
		}
		
		byte tempBuffer[] = new byte[8000];
		public void run(){
			byteArrayOutputStream = new ByteArrayOutputStream();
			stopCapture = false;
			try {		
				//Loop until stopCapture is set by another thread that services the Stop button.
				while(!stopCapture){
					//Read data from the internal buffer of the data line.
					int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
					if(cnt > 0){
						byteArrayOutputStream.write(tempBuffer, 0, cnt);
						if (liveBox.isSelected()) {
							session.sendChunk(tempBuffer, false);
						}
					}
				}
				byteArrayOutputStream.close();
				if (liveBox.isSelected()) {
					byte tmp[] = new byte[0];
					session.sendChunk(tmp,  true);
				}
			} catch (Exception e) {
				System.out.println(e);
				System.exit(0);
			}
		}
	}
}
