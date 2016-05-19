#!/bin/bash

username="dylanjay"
reponame="cs183proj"
repo="git://github.com/$username/$reponame.git"
branch="master"
path="/home/vagrant/Desktop/projTest/test"
git clone -b $branch $repo $path -q
forPath="/home/vagrant/Desktop/projTest/test/*.sh"

for f in $forPath
do
    sh $f >>/home/vagrant/shared/out.txt 2>&1
done