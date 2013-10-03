#!/bin/bash

# Try to find the actual directory where the script is
SCRIPT="$0"

# Follow symlinks
while [ -h "$SCRIPT" ]; do
  ls=`ls -ld "$SCRIPT"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    SCRIPT="$link"
  else
    SCRIPT=`dirname "$PRG"`/"$link"
  fi
done

# Extract the directory from the script name; that is the program directory
PRGDIR=`dirname "$SCRIPT"`

# Go to the program directory
cd "$PRGDIR"

# Run the medsavant client jar
java -Xmx4g -jar medsavant-client-*.jar