#!/bin/bash

# vm test script - run every time the machine is brought up

# demo of same red/green coloring as the init script.
# output coloring can be configured in the Vagrantfile
echo "Running tests..."
echo "whoops" >&2

# put results into /vagrant_data, and they will populate the
# host's "output" folder



# This script would do something along the lines of pull a repo,
# run tests, and put the results in /vagrant_data



# poewroff to avoid having to run "vagrant halt" after every
# "vagrant up"
# the -f flag is there to make it shutdown as quickly as possible.
# Otherwise, vagrant may be invoked too quickly, and next jobs would
# not be run (becuase "vagrant up" would return upon seeing an existing
# instance)
/sbin/poweroff -f


