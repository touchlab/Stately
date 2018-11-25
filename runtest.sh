#Runs specific test. Pass in test name.
./gradlew linkTestDebugExecutableMacos
./build/bin/macos/test/debug/executable/test.kexe --ktest_regex_filter=.*$1.*
