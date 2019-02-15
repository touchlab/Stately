# memtest

AtomicReference may leak memory in Kotlin Native. The only way to test if memory is leaked is to 
run a debug executable on the command line. This will return non-zero if there is an error, including
memory leaks. Running `build` on this project is sufficient, as it builds and runs the application, and
fails if the app fails.