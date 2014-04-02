#!/bin/bash
# Script to run all parser tests

WHERE=.
COMPILER=../../dist/compiler488.jar
COMPILER_SYMBOL_TABLE=../../../dist/compiler488.jar

runtests () {
for file in `ls ./*.488`
do
    echo "Running $file"
    java -jar $COMPILER -X $file
    echo
done
}

runtests_codegen () {
for file in `ls ./*.488`
do
    echo "Running $file"
    java -jar $COMPILER $file
    echo
done
}

pushd $WHERE/testing/fail > /dev/null
runtests_codegen
popd > /dev/null
pushd $WHERE/testing/pass > /dev/null
runtests_codegen
popd > /dev/null

echo "All tests run!"

exit 0
