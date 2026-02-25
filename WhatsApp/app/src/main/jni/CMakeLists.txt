cmake_minimum_required(VERSION 3.22.1)
project("llamanexus")

# Habilitar mmap para cargar modelos grandes sin OOM
add_definitions(-DGGML_USE_MMAP=1)
add_definitions(-DGGML_USE_ALLOCATOR=1)

# Optimizaciones
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -fPIC")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -fPIC")

# Incluir headers de llama.cpp
include_directories(${CMAKE_SOURCE_DIR}/llama)

# Librería principal
add_library(llamanexus SHARED
    llamanexus.cpp
)

# Linkar con libs de Android
target_link_libraries(llamanexus
    android
    log
    z
)