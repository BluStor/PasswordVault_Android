#!/usr/bin/env bash

RESULTS=()

# change upper bound to 23 when robolectric supports that API version
for targetSdkVersion in `seq 19 21`; do
    echo "************* Running test for SDK version $targetSdkVersion *************"
    ./bin/test.sh testDebugUnitTest -PtestedSdkVersionOverride=testedSdkVersion "$@"
    if [[ $? == 0 ]];
    then RESULTS+=("SDK VERSION $targetSdkVersion: \033[0;32mSUCCESS\033[0;39m");
    else RESULTS+=("SDK VERSION $targetSdkVersion: \033[0;31mFAILED\033[0;39m");
    fi
done

echo "************* RESULTS *************"
for ((i = 0; i < ${#RESULTS[@]}; i++)) do
    echo -e "${RESULTS[$i]}"
    tput sgr0
done