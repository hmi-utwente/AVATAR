# AVATAR

## Setup
after cloning, run:

git submodule update --init --recursive

to get external modules (currently the hmibuild system).

## Components

Required middleware: Stomp ActiveMQ

NL speech TTS by Fluency (www.fluency.nl)

EN speech TTS by MaryTTS (included with ASAP)

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
