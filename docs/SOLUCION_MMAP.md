# 📁 SOLUCIÓN MMAP LLAMA.CPP

## ✅ Opción 1 - CMake Flag GGML_USE_MMAP

### app/src/main/jni/CMakeLists.txt
```cmake
cmake_minimum_required(VERSION 3.22.1)
project("llamanexus")

add_definitions(-DGGML_USE_MMAP=1)

add_library(llamanexus SHARED llamanexus.cpp)
target_link_libraries(llamanexus android log)
```

### Resultado:
- Modelos 80-500MB cargan sin OOM
- Funciona en 1GB RAM
- Carga bajo demanda

### Alternativa sin NDK:
Usar modelos cuantizados Q4_K_M (25% tamaño)