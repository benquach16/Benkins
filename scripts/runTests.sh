#!/bin/bash

# vm test script - run every time the machine is brought up


## SETUP ##

# git parameters
HANDLE="dylanjay" #__HANDLE__
REPO="cs183proj" #__REPO__
BRANCH="PullRequestBranch" #__BRANCH__
PULL_REQ="1" #__PULL_REQ__

# vagrant stuff
OUT_DIR="/vagrant_data/$HANDLE/$REPO/$PULL_REQ"

mkdir -p /vagrant_data/$HANDLE/$REPO/$PULL_REQ
touch /vagrant_data/$HANDLE/$REPO/$PULL_REQ/stdout.txt
touch /vagrant_data/$HANDLE/$REPO/$PULL_REQ/stderr.txt
touch /vagrant_data/$HANDLE/$REPO/$PULL_REQ/stat.txt


STDOUT="$OUT_DIR/stdout.txt"
STDERR="$OUT_DIR/stderr.txt"
STAT="$OUT_DIR/stat.txt"

# temp files
TMP_STDOUT=`mktemp`
TMP_STDERR=`mktemp`

# files
[ -f $STDOUT ] && rm $STDOUT
[ -f $STDERR ] && rm $STDERR
[ -f $STAT ] && rm $STAT

# commands to run on exit
STOP_CMD() {
    rm $TMP_STDOUT $TMP_STDERR
    poweroff -f
}

# safe cd
SAFE_CD() {
    cd $1 || {
        echo "ERROR: could not change directory to '$1' from '$(pwd)'" >&2
        STOP_CMD
    }
}


## REPO ##

# check if it exists
[ -d $REPO ] && {
    SAFE_CD $REPO
    # pull changes
    git pull origin $BRANCH || {
        echo "ERROR: pulling '$BRANCH' from $REPO's origin failed" >&2
    }
} || {
    # clone
    git clone -b $BRANCH https://github.com/$HANDLE/$REPO.git || {
        echo "ERROR: cloning branch '$BRANCH' from $REPO failed" >&2
        STOP_CMD
    }
    SAFE_CD $REPO
}

# check for tests directory
[ ! -d tests ] && {
    echo "ERROR: no tests directory found in $REPO on branch '$BRANCH'" >&2
    STOP_CMD
}


## RUN TESTS ##
for f in tests/*
do
    # if the file is executable
    if [ -x "$f" ]
    then
        # execute and save stdout and stderr
        "$f" 1>$TMP_STDOUT 2>$TMP_STDERR ||
            {
                echo "[$(basename $f)] returned error code $? during execution" >> $STAT
            }
        # add '[$(basename f)] ' to the beginning of each line of the current
        # test's stdout and stderr
        sed "s/\(.*\)/[$(basename $f)] \1/" $TMP_STDOUT >>$STDOUT
        sed "s/\(.*\)/[$(basename $f)] \1/" $TMP_STDERR >>$STDERR
    fi
done


## CLEANUP ##
STOP_CMD


