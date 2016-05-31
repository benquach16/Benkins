@ECHO OFF

set PATH=%cd%;C:\"Program Files"\Java\jdk1.8.0_77\bin;C:\HashiCorp\Vagrant\bin

java.exe -classpath ../src;../lib/\* GitScrape
