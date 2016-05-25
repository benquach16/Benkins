#!/bin/bash

# vm test script - run every time the machine is brought up

HANDLE="dylanjay"
REPO="cs183proj"
BRANCH="PullRequestBranch"

OUT_DIR="/vagrant_data"

STDOUT="$OUT_DIR/stdout.txt"
STDERR="$OUT_DIR/stderr.txt"
STAT="$OUT_DIR/stat.txt"

TMP_STDOUT=`mktemp`
TMP_STDERR=`mktemp`

git clone -b $BRANCH https://github.com/$HANDLE/$REPO.git
cd $REPO

[ ! -d tests ] && {
    echo "ERROR: no tests directory found in $REPO on branch $BRANCH" >&2
    exit 1;
}

[ -f $STDOUT ] && rm $STDOUT
[ -f $STDERR ] && rm $STDERR
[ -f $STAT ] && rm $STAT

for f in tests/*
do
    if [ -e "$f" ]
    then
        "$f" 1>$TMP_STDOUT 2>$TMP_STDERR ||
            {
                echo `basename $f`" returned error code [$?] during execution" >> $STAT
            }
        sed "s/\(.*\)/[$(basename $f)] \1/" $TMP_STDOUT >>$STDOUT
        sed "s/\(.*\)/[$(basename $f)] \1/" $TMP_STDERR >>$STDERR
    fi
done


# poewroff to avoid having to run "vagrant halt" after every
# "vagrant up"
# the -f flag is there to make it shutdown as quickly as possible.
# Otherwise, vagrant may be invoked too quickly, and next jobs would
# not be run (becuase "vagrant up" would return upon seeing an existing
# instance)
/sbin/poweroff -f


