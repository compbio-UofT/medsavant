#! /bin/sh
id=ps aux | grep java | grep MedSavantServerEngine | grep -v "\-l" | awk  \' {print $2} 
kill -kill ''
