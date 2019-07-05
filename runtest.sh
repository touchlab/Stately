#Runs specific test. Pass in test name.
set -e
./gradlew linkTestDebugExecutableMacos
./stately/build/bin/macosX64/testDebugExecutable/test.kexe --ktest_regex_filter=.*$1.*
