@echo off

FOR /R %%F IN (medsavant-client-*.jar) DO (
  java -Xmx4G -jar "%%F"
)
