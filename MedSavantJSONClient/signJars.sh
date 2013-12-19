# run following function to remove carriage returns in Windows:
# d2u signJars.sh

shopt -s nullglob

# cd dist
for f in *.jar
do
	jarsigner -keystore jnlpKey -storepass abcdef $f jdc
	echo "signed dist/$f"
done

cd lib
for f in *.jar
do
	jarsigner -keystore ../jnlpKey -storepass abcdef $f jdc
	echo "signed dist/lib/$f"
done
