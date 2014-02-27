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

runtests_AST () {
for file in `ls ./*.488`
do
    echo "Running $file"
    java -jar $COMPILER -X -D a $file
    echo
done
}

runtests_symbol_table () {
for file in `ls ./*.488`
do
    echo "Running $file"
    java -jar $COMPILER_SYMBOL_TABLE -X -D y $file
    echo
done
}

pushd $WHERE/tests/failing > /dev/null
runtests
popd > /dev/null
pushd $WHERE/tests/passing > /dev/null
runtests
popd > /dev/null
pushd $WHERE/tests/ast > /dev/null
runtests_AST
popd > /dev/null
pushd $WHERE/tests/symbol_table/passing > /dev/null
runtests_symbol_table
popd > /dev/null
pushd $WHERE/tests/symbol_table/failing > /dev/null
runtests_symbol_table
popd > /dev/null

echo "All tests run!"

exit 0
