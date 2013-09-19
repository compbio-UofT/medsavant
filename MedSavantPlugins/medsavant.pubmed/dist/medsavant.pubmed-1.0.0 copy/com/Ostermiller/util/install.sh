#!/bin/bash --noprofile

directory=/usr/local/bin
utils=utils.jar
RandPass=RandPass
LineEnds=LineEnds
MD5=MD5Sum
Tabs=Tabs
Base64=Base64

if [ ! -z $1 ]
    then 
    if [ "$1" == "--help" ]
    then
        echo "Install the ostermiller.org Java utilities."
        echo "-f force"
        exit 0
    elif [ "$1" != "-f" ]
    then
        echo "Unknown option: $1" 
        exit 1
    fi
fi

workingdir=`pwd`

if [ ! -e $workingdir/$utils ]
then
    echo "Could not find '$utils'."
    echo "Make sure you execute this script from"
    echo "the directory that contains '$utils'."
    exit 1
fi

if [ ! -w "$directory" ]
then
    directory=~/bin
fi

if [ ! -w "$directory" ]
then
    echo "You do not have permission to write in"
    echo "$directory"
    exit 1
fi

if [ ! -e $directory/$RandPass ] || [ ! -z $1 ]
then
    echo "#!/bin/bash --noprofile" > $directory/$RandPass
    echo "java -classpath $workingdir/$utils com.Ostermiller.util.RandPass \"\$@\"" >> $directory/$RandPass
    chmod 755 $directory/$RandPass
    echo "$RandPass installed in $directory."
else
    echo "$directory/$RandPass already exists.  Use -f to overwrite."
fi

if [ ! -e $directory/$LineEnds ] || [ ! -z $1 ]
then
    echo "#!/bin/bash --noprofile" > $directory/$LineEnds
    echo "java -classpath $workingdir/$utils com.Ostermiller.util.LineEnds \"\$@\"" >> $directory/$LineEnds
    chmod 755 $directory/$LineEnds
    echo "$LineEnds installed in $directory."
else
    echo "$directory/$LineEnds already exists.  Use -f to overwrite."
fi

if [ ! -e $directory/$MD5 ] || [ ! -z $1 ]
then
    echo "#!/bin/bash --noprofile" > $directory/$MD5
    echo "java -classpath $workingdir/$utils com.Ostermiller.util.MD5 \"\$@\"" >> $directory/$MD5
    chmod 755 $directory/$MD5
    echo "$MD5 installed in $directory."
else
    echo "$directory/$MD5 already exists.  Use -f to overwrite."
fi

if [ ! -e $directory/$Tabs ] || [ ! -z $1 ]
then
    echo "#!/bin/bash --noprofile" > $directory/$Tabs
    echo "java -classpath $workingdir/$utils com.Ostermiller.util.Tabs \"\$@\"" >> $directory/$Tabs
    chmod 755 $directory/$Tabs
    echo "$Tabs installed in $directory."
else
    echo "$directory/$Tabs already exists.  Use -f to overwrite."
fi

if [ ! -e $directory/$Base64 ] || [ ! -z $1 ]
then
    echo "#!/bin/bash --noprofile" > $directory/$Base64
    echo "java -classpath $workingdir/$utils com.Ostermiller.util.Base64 \"\$@\"" >> $directory/$Base64
    chmod 755 $directory/$Base64
    echo "$Base64 installed in $directory."
else
    echo "$directory/$Base64 already exists.  Use -f to overwrite."
fi


