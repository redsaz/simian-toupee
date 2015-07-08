#!/bin/sh

CLASSPATH=$CLASSPATH:./:./WEB-INF:./WEB-INF/classes:/WEB-INF/lib

echo "Starting Server..."

java com.redsaz.embeddedrest.EmbeddedRest

