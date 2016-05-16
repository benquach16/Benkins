#! /bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export JAVA_HOME=/usr/csshare/pkgs/jdk1.7.0_17
export PATH=$JAVA_HOME/bin:$PATH

javac -d $DIR/../classes/ ./../src/Interface.java ./../src/Main.java
