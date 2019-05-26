cmake_minimum_required(VERSION 3.14)
project(cpp)

set(CMAKE_CXX_STANDARD 14)

include_directories(build/kndist/runtime/src/main/cpp)
include_directories(code/src/main/cpp)

add_executable(cpp
        code/src/main/cpp/KonanHelper.cpp
        code/src/main/cpp/KonanHelper.h
        code/src/main/cpp/SQLiteCommon.cpp
        code/src/main/cpp/SQLiteCommon.h
        code/src/main/cpp/SQLiteConnection.cpp
        code/src/main/cpp/UtilsErrors.h)
