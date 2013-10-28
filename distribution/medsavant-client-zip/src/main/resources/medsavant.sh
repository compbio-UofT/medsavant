#!/bin/bash
#
# See the NOTICE file distributed with this work for additional
# information regarding copyright ownership.
#
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
#
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.
#


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