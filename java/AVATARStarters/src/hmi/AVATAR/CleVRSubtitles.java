package hmi.AVATAR;

import static nl.utwente.hmi.middleware.helpers.JsonNodeBuilders.object;

import java.util.Timer;
import java.util.TimerTask;

import com.fasterxml.jackson.databind.JsonNode;

import hmi.textembodiments.TextEmbodiment;
import hmi.unityembodiments.UnityEmbodiment;
import nl.utwente.hmi.middleware.Middleware;

public class CleVRSubtitles implements TextEmbodiment {
	
	private Middleware middleware;
	private String id;
	private Timer timer;
	public long lastUpdate;
	
	public CleVRSubtitles(Middleware middleware, String id) {
		this.middleware = middleware;
		this.id = id;
		timer = new Timer();
		timer.schedule(new ClearSubs(this), 1000, 1000);
	}
	
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public void setText(String text) {
		/*
		{
		    "msgType": "ShowSubtitle",
		    "subtitle": "The subtitle to depict on the GUI."
		}
		*/
    	JsonNode msg = object(
    			UnityEmbodiment.AUPROT_PROP_MSGTYPE, UnityEmbodiment.AUPROT_MSGTYPE_SHOWSUBTITLES,
    			UnityEmbodiment.AUPROT_PROP_SUBTITLE, text).end();
		middleware.sendData(msg);
		lastUpdate = System.currentTimeMillis();
		/*
		timer.cancel();
		timer.purge();
		timer = new Timer();*/
	}
	
	public void hideText() {
		/*
		{
		    "msgType": "HideSubtitle",
		}
		*/
    	JsonNode msg = object(
    			UnityEmbodiment.AUPROT_PROP_MSGTYPE, UnityEmbodiment.AUPROT_MSGTYPE_HIDESUBTITLES).end();

		middleware.sendData(msg);
	}
	
}

class ClearSubs extends TimerTask {
	CleVRSubtitles subs;
	public ClearSubs(CleVRSubtitles subs) {
		this.subs = subs;
	}
    public void run() {
    	if (System.currentTimeMillis() - subs.lastUpdate > 3000) {
    		subs.hideText();
    	}
    }
}
