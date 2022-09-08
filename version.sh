#!/bin/bash

function getVersion() {
  echo "cat //*[local-name()='project']/*[local-name()='version']" | xmllint --shell pom.xml | sed '/^\/ >/d' | sed 's/<[^>]*.//g'
}
# shellcheck disable=SC2005
echo "$(getVersion)"
