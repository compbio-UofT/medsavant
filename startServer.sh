echo "Killing server"
ids=`ps aux | grep java | grep MedSavantServerEngine | grep mfiume | awk '{print \$2}'`
for id in $ids
do 
    echo "Killing "$id
    kill -kill $id 2> /dev/null
done

echo "Starting server"
cd $DIR
java -jar -Djava.rmi.server.hostname=medsavant-dev.cs.toronto.edu MedSavantServerEngine.jar -c config &




Nameservers
Nameservers:  (Last update 6/4/2013)
NS31.DOMAINCONTROL.COM 
NS32.DOMAINCONTROL.COM 
Set Nameservers
DNS Manager
DNS Manager: Available 
A	@	68.178.232.100
CNAME	calendar	login.secureserver.net
CNAME	email	email.secureserver.net
CNAME	fax	login.secureserver.net
MX	@	mailstore1.secureserver.net
MX	@	smtp.secureserver.net

Launch



Preview DNS: 
Pending  

Nameservers: 
ns31.domaincontrol.com
ns32.domaincontrol.com
A Record(s): 
68.178.232.100 
