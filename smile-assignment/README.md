## To run the program:
Build the jar file using 
> mvn package

After which, in target folder a file named `smile-assignment-1.0-SNAPSHOT.jar`
 would be created. Paste the required xdsl file in target folder and run the program using: 

> java -Djava.library.path=../lib -cp smile-assignment-1.0-SNAPSHOT.jar:../lib/jsmile-1.5.0.jar myk.assignment.Assignment3 "xdsl file to use"

When running from intellij, remember to add in java options 
> -Djava.library.path=./lib  