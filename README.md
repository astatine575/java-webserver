# README #

### What is this repository for? ###

* Quick summary:
	This is a multi-threaded webserver written in java as a pet project
* Version
	Latest Version is 1.0

### How do I get set up? ###

* Summary of set up : 
	The main file is ServerDriver.java. This file will accept arguments "<portNumber> <threadNum> <mode>", then run the server thread as a daemon. Look at launchServer.bat or launchServer.sh for reference.
* Configuration : 
        You can give a port number and a concurrent thread count, as well as the mode it will operate in (curently just http). You should place server data in a directory "/serverdata" in the root directory of the repository, and place the names of those files that you want the server to be able to access in a file called "validFiles.ini", with one line per file name. So if you want the server to access "/serverdata/index.html", place "index.html" in "/serverdata", and add the line "/index.html" to "validFiles.ini".
* Dependencies : 
		None.
* Database configuration : 
	Just a bunch of files for now
* How to run tests: 
	Not yet implemented.
* Admins:
	* Ehab Mahran - ehabmahr@buffalo.edu
