#!/bin/bash
# Script to run all failing parser tests

WHERE=.
COMPILER=../../dist/compiler488.jar

runtests () {
for file in `ls ./*.488`
do
    echo "Running $file"
    java -jar $COMPILER $file
    echo
done
}

pushd $WHERE/tests/failing > /dev/null
runtests
popd > /dev/null
pushd $WHERE/tests/passing > /dev/null
runtests
popd > /dev/null

echo "All tests run!"

exit 0
