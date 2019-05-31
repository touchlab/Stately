cmake_minimum_required(VERSION 3.14)
project(cpp)

set(CMAKE_CXX_STANDARD 14)

include_directories(code/src/main/cpp)
include_directories(build/kndist/runtime/src/main/cpp)

add_executable(cpp
        code/src/main/cpp/StatelyCollections.cpp)
