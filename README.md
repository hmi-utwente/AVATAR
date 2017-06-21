# AVATAR

## Setup
after cloning, run:

git submodule update --init --recursive

to get external modules (currently the hmibuild system).

## Components
Some prerequisites are needed before installing the project.

Apache ActiveMQ is an open source messaging server used in communication between the ASAP realiser and the Unity AVATAR.
ActiveMQ can be installed from the Apache website (http://activemq.apache.org/getting-started.html).

Fluency is a multilingual Text-to-Speech Synthesis platform for Dutch. Fluency is optional and only used for Dutch TTS synthesis, if English TTS synthesis is used, Fluency is NOT needed.
Fluency can be installed from the Fluency website (www.fluency.nl).

MaryTTS is an open-source, multilingual Text-to-Speech Synthesis platform. MaryTTS should be installed by default on your system.
Otherwise MaryTTS can be installed from the MaryTTS website (http://mary.dfki.de/index.html).


### ASAPRealizer
Compile & run the starter by runnig these commands in ./java/AVATARStarters/:

ant resolve
ant compile
ant main (select AsapAvatarStarter class)
ant run

### Agent Binaries
Contact Rieks or Merijn for a copy.
Place in \binary
(run \binary\World\CleVR.PoP.Unity.exe)

### ASR
In folder \java\Spraak

### QAMatcher
ActiveMQ QAMatcher: \java\qamatcher\testResponder.bat
The QA template: \java\qamatcherNL\resources\qamatcher\vragen.xml

### WoZ Panel
In \java\AvatarPolitieWoz
