#! /bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

javac $DIR/../src/GitScrape.java -classpath $DIR/../src:$DIR/../lib/\*
