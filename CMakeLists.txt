find_package(Java)
include(UseJava)

file(GLOB_RECURSE source
    "${CMAKE_CURRENT_SOURCE_DIR}/*.java")

add_jar(Expressioner ${source} ENTRY_POINT com/iamsubhranil/expressioner2/Expressioner)
