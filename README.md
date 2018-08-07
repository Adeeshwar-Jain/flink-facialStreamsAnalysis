#REAL TIME ANALYTICS USING FLINK AND MQTT CONNECTOR

#FOR FACIAL STREAMS

Download Flink:- https://flink.apache.org/downloads.html

Download maven:- https://maven.apache.org/download.cgi

Installing maven:-

https://maven.apache.org/install.html				
https://www.howtoforge.com/tutorial/ubuntu-apache-maven-installation   // specific for ubuntu

STEPS FOR CREATING JAR:-

1. Clone the project:- git clone https://Adeesh96@bitbucket.org/hemant0601/flink.git .
2. Switch to project directory in terminal.
3. Run "mvn package" or "mvn clean package" to make the jar.

To run the jar from command-line,run the following commands from the directory where flink is installed:-

	./bin/start-cluster.sh  //Start Flink

	bin/flink run -c <name of the main class to run> <name of the jar with path>.jar // run the jar
	
To run the jar from flink-dashboard:-

1. Switch to locahost:8081.
2. Select to option Submit new job.
3. Add new jar from the local directory.
3. Upload the jar.
4. Select the checkmark against uploaded jar and add Entry class name(name of class to run) and the other necessary parameters.
5. Click Submit to run the class.

#Note:- 
#For Saving data in DB:-
	run MetrixAnalysis class
	
#For seeing saved data of current date based on different parameters
	run OnFlyParamBasedAnalysis class


#EXAMPLE STREAMS:-

#Live stream:-

1. {
	  "version" : "2",
	  "event_type" : "no_match",
	  "name" : "",
	  "timestamp" : "1532691662",
	  "faceid" : "",
	  "device_id" : "cam_entry",
	  "type" : "face",
	  "model" : "aws_rekog",
	  "face" : "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"
	}

2. {
	  "version" : "2",
	  "event_type" : "match",
	  "name" : "Kshitij 1532681766292",
	  "timestamp" : "1532691671",
	  "faceid" : "8cb5d044-7c7c-4f8d-ba9d-a72cd3716c62",
	  "device_id" : "cam_entry",
	  "type" : "face",
	  "model" : "aws_rekog",
	  "face" : "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"
	}


#Test Streams:-

#snapshot:-
{
  "face" : "",
  "event_type" : "match",
  "device_id" : "cam_exit",
  "name" : "Renu Verma",
  "model" : "aws_recok",
  "faceid" : "57198e4b-2ae9-4b51-af25-fb3fee5a7a95",
  "type" : "face",
  "version" : "2",
  "timestamp" : 1532692241126
}

#facedetected:-
{
  "face" : "HexCodedString",
  "event_type" : "match",
  "device_id" : "cam_exit",
  "name" : "Renu Verma",
  "model" : "aws_recok",
  "faceid" : "57198e4b-2ae9-4b51-af25-fb3fee5a7a95",
  "type" : "face",
  "version" : "2",
  "timestamp" : 1532692260044
}

#falsepositive:-
{
  "device_id" : "cam_entry",
  "model" : "aws_recok",
  "faceid" : "57198e4b-2ae9-4b51-af25-fb3fee5a7a95",
  "type" : "falsepositive",
  "version" : "v1.01",
  "timestamp" : 1532692280392
}

#falsenegative:-
{
  "device_id" : "cam_entry",
  "model" : "aws_recok",
  "faceid" : "57198e4b-2ae9-4b51-af25-fb3fee5a7a95",
  "type" : "falsenegative",
  "version" : "v1.01",
  "timestamp" : 1532692282695
}

#Note:- Maven 3.0.4 (or higher),Java 8.x,flink-1.5 should be installed
