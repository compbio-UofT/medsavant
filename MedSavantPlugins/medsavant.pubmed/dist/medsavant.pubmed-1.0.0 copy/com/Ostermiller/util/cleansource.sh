for file in *.*.html
do
  baseFile=${file%.*.html}
  prob=`egrep -l "<a.*$baseFile.*$baseFile\.[a-z]*.*a>" $file`
  if [ ! -e "../$baseFile.html" ]
  then
	if [ "a$prob" != "a" ]
	then
      mv  "$file" temp
      sed "s/<a.*$baseFile.*\($baseFile\.[a-z]*\).*a>/\1/g" temp > $file
	  mv  "$file" temp      
	  sed "s/<small.*Download.*small>//g" temp > $file
	fi
  fi
  prob=`egrep -l "<small.*JavaDoc.*small>" $file`
  if [ ! -e "../doc/com/Ostermiller/util/$baseFile.html" ]
  then
	if [ "a$prob" != "a" ]
	then
      mv  "$file" temp
	  sed "s/<small.*JavaDoc.*small>//g" temp > $file
	fi
  fi
done
