# drift-plots
A proof-of-concept implementation of drift plots as a research tool in evolutionary computation.

To run it, you need Java 8 installed and any recent SBT available. 
The following command will run the only so far existing application:

`sbt runMain ru.ifmo.drift.Main`

This will download the dependencies, compile the sources and run the application. 
It will display a UI window with plots. You can also control some of the parameters, e.g.:

`sbt runMain ru.ifmo.drift.Main --from=100 --to=1000 --step=100 --times=25`

(The example parameter values are the default ones, you can omit some or all of them).

The dependencies are as follows:

* JUnit through `"com.novocode" % "junit-interface" % "0.11"` for running unit tests.
* JFreeChart (`"org.jfree" % "jfreechart" % "1.5.0"`) for displaying the plots. 
