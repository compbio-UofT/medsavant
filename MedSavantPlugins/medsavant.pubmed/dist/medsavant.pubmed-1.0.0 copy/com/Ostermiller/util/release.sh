#!/bin/bash

hr[0]='b'
hr[1]='k'
hr[2]='M'
hr[3]='G'
hr[4]='T'

humanreadable () {
  s=$1;
  i=0;
  while [[ $s -ge 1024 ]]
  do
    let s=s/1024
    let i=i+1
  done
  echo $s${hr[$i]}
}

size=`wc -c utils.jar`
size=`humanreadable $size`

if [ -z "`grep -i $size download.html`" ]
then
    echo "utils.jar size is $size but download.html does not show that."
    #exit 1
fi

latestversion=`grep -oE 'ostermillerutils_[0-9]_[0-9]{2}_[0-9]{2}\.jar' download.bte | sort | tail -1`
cp utils.jar "$latestversion"

FILES="$@ $latestversion"
if [ "$FILES" ]
then
	echo Make: Uploading to web site: $FILES
    chmod -x install.sh
	scp -r $FILES deadsea@ostermiller.org:www/utils
    chmod +x install.sh
fi

rm "$latestversion"
