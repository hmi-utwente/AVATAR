README qamatcher

HOME directory is qamatcher

RESOURCES used in  qamatcher/resources/qamatcher

xml files can be read by the responder class
txt files can be read by the matcher class
Both have a test class that you can run using the .bat files
 
COMPILE

(To compile all java src files in src directory and store class files in classes directory)

Open command box 
Goto the home directory
qamatcher
execute compile.bat

TEST QAMATCHER

In the home directory
execute
testQAMatcher

You can type a sentence then <enter>
it returns the best matching sentence in the file questions.txt (in the resources directory)

TEST QARESPONDER

In the home directory
execute
testQAResponder

You can type a sentence then <enter>
it give the best matching sentence in the file ....xml file given as argument (in the resources directory)

JAVADOC (poor)
In the doc directory

See also the comments in the src files.

WANT TO MAKE CHANGES?

1) change the similarity method used (see Matcher and Responder class for similarity method

2) Add new sentences/dialogs to the input files
The structure of the xml file read by the DomDialogsParser class should be kept as is.

3) Adapt the xml structure, the xml reader in the Parser class and adjust the Dialog class accordingly.
   Note that currently only the answer elements have an attribute list
   The idea is that this is the stance of the responding agent (the Virtual Witness/Suspect)
   A ResponseModel should be used to determine the agents stance/affect state in response
   to the interviewers questions/stance.  

Rieks op den Akker
April 2016
August 2016




 