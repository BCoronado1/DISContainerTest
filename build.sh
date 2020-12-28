#!/bin/bash
mkdir -p bin
mvn clean install -X
cp target/DISContainerTest-1.0-SNAPSHOT-jar-with-dependencies.jar bin/DISContainerTest.jar
