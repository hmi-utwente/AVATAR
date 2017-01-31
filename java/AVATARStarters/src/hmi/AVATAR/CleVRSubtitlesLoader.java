package hmi.AVATAR;

import java.io.IOException;

import asap.middlewareengine.embodiment.MiddlewareEmbodiment;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.xml.XMLTokenizer;

public class CleVRSubtitlesLoader implements EmbodimentLoader {

	private String id = "";
    private MiddlewareEmbodiment mwe = null;;
    private AsapRealizerEmbodiment are = null;
    private Embodiment embodiment = null;
    
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
			Loader... requiredLoaders) throws IOException {
        id = loaderId;
        
        for (EmbodimentLoader e : ArrayUtils.getClassesOfType(requiredLoaders, EmbodimentLoader.class))
        {
            if (e.getEmbodiment() instanceof MiddlewareEmbodiment)
            {
                mwe = (MiddlewareEmbodiment) e.getEmbodiment();
            }
            if (e.getEmbodiment() instanceof AsapRealizerEmbodiment)
            {
                are = (AsapRealizerEmbodiment) e.getEmbodiment();
            }
        }
        if (mwe == null)
        {
            throw new RuntimeException("CleVRSubtitlesLoader requires an EmbodimentLoader containing a MiddlewareEmbodiment");
        }
        if (are == null)
        {
            throw new RuntimeException("CleVRSubtitlesLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }
        while (!tokenizer.atETag("Loader"))
        {
            readSection(tokenizer);
        }
        
        embodiment = new CleVRSubtitles(mwe.getMiddleware(), id);
	}
	
    protected void readSection(XMLTokenizer tokenizer) throws IOException
    {	
    	throw tokenizer.getXMLScanException("Unknown tag in CleVRSubtitlesLoader content");
    	/*
        HashMap<String, String> attrMap = null;
        if (tokenizer.atSTag("MiddlewareBinding"))
        {
            attrMap = tokenizer.getAttributes();
            middlewareBinding = new MiddlewareBinding(mwe.getEmbodiment());
            try
            {
                middlewareBinding.readXML(new Resources(adapter.getOptionalAttribute("resources", attrMap, "")).getReader(adapter
                        .getRequiredAttribute("filename", attrMap, tokenizer)));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new RuntimeException("Cannnot load MiddlewareBinding: " + e);
            }
            tokenizer.takeEmptyElement("MiddlewareBinding");
        }
        else
        {
            throw tokenizer.getXMLScanException("Unknown tag in Loader content");
        }*/
    }

	@Override
	public void unload() {
	}

	@Override
	public Embodiment getEmbodiment() {
		return embodiment;
	}

}
