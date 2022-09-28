#!/bin/bash
TIME1=$(date +%s)
./gradlew clean
TIME2=$(date +%s)
let ELAPSED1=TIME2-TIME1
echo "Cleaned build in $ELAPSED1"
./gradlew bundlePlayStoreRelease
TIME3=$(date +%s)
let ELAPSED2=TIME3-TIME2
echo "Assembled bundle in $ELAPSED2"