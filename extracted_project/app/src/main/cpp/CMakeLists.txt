cmake_minimum_required(VERSION 3.10)
project(JoyConVR_Native)

add_library( native-lib SHARED
             main.cpp
             Madgwick.cpp )

find_library( log-lib log )

target_link_libraries( native-lib ${log-lib} )