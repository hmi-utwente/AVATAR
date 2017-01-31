# SpraakNL HMI Middleware Tools

This publishes output of the Speech API to two topics, one for the live output, one for the "final" output (i.e. a complete sentence after a long pause. Note that this pause has to be pretty long, otherwise it keeps on adding to a very very long "final" sentence that is been transcribed).
It is probably more practical to look at the live topic (and pick the last ~10-20 words oid).

To fix/modify, start at main class src/nl/hmi/component/SpraakNLComponent.java

Topics:
/topic/speechLive
/topic/speechFinal

To compile & run just

ant compile 
ant run

There is a shellscript "runrun.sh" that can be used to start the application and that will restart itself if the application crashed.
It works on OS X and probably linux, but I haven't looked at how to make it in Windows.
