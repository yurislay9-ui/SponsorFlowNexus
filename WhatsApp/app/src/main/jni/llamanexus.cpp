/*
 * SponsorFlow Nexus v2.3 - Llama.cpp JNI Bridge with mmap
 */
#include <jni.h>
#include <android/log.h>
#include <string>
#include <cstring>

#define LOG_TAG "LlamaNexus"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Declaraciones de llama.cpp (se linkará la librería)
extern "C" {
    struct llama_model;
    struct llama_context;
    
    struct llama_model_params {
        int n_gpu_layers;
        int main_gpu;
        bool vocab_only;
        bool use_mmap;
        bool use_mlock;
        bool check_tensors;
    };
    
    struct llama_context_params {
        uint32_t n_ctx;
        uint32_t n_batch;
        uint32_t n_ubatch;
        uint32_t n_seq_max;
        int32_t n_threads;
        int32_t n_threads_batch;
    };
    
    llama_model_params llama_model_default_params();
    llama_context_params llama_context_default_params();
    llama_model* llama_load_model_from_file(const char* path, llama_model_params params);
    void llama_free_model(llama_model* model);
    llama_context* llama_new_context_with_model(llama_model* model, llama_context_params params);
    void llama_free(llama_context* ctx);
}

// Handle del modelo
static llama_model* g_model = nullptr;
static llama_context* g_ctx = nullptr;

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_sponsorflow_nexus_ai_LlamaBridge_loadModelNative(
    JNIEnv* env,
    jobject thiz,
    jstring modelPath
) {
    if (g_model != nullptr) {
        LOGI("Modelo ya cargado, liberando...");
        llama_free_model(g_model);
        g_model = nullptr;
    }
    
    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    LOGI("Cargando modelo con mmap: %s", path);
    
    // Configurar parámetros con mmap habilitado
    llama_model_params model_params = llama_model_default_params();
    model_params.use_mmap = true;  // CRUCIAL: Habilitar mmap
    model_params.use_mlock = false;
    model_params.vocab_only = false;
    
    // Cargar modelo
    g_model = llama_load_model_from_file(path, model_params);
    env->ReleaseStringUTFChars(modelPath, path);
    
    if (g_model == nullptr) {
        LOGE("Error al cargar modelo");
        return 0L;
    }
    
    LOGI("Modelo cargado exitosamente con mmap");
    return reinterpret_cast<jlong>(g_model);
}

JNIEXPORT jstring JNICALL
Java_com_sponsorflow_nexus_ai_LlamaBridge_runInferenceNative(
    JNIEnv* env,
    jobject thiz,
    jlong modelHandle,
    jstring prompt,
    jint maxTokens,
    jfloat temperature
) {
    llama_model* model = reinterpret_cast<llama_model*>(modelHandle);
    if (model == nullptr) {
        return env->NewStringUTF("Error: Modelo no cargado");
    }
    
    const char* promptStr = env->GetStringUTFChars(prompt, nullptr);
    
    // Crear contexto si no existe
    if (g_ctx == nullptr) {
        llama_context_params ctx_params = llama_context_default_params();
        ctx_params.n_ctx = 2048;
        ctx_params.n_threads = 4;
        ctx_params.n_batch = 512;
        g_ctx = llama_new_context_with_model(model, ctx_params);
    }
    
    // TODO: Implementar inferencia real
    // Por ahora retornar placeholder
    std::string response = "Respuesta generada (mmap activo)";
    
    env->ReleaseStringUTFChars(prompt, promptStr);
    return env->NewStringUTF(response.c_str());
}

JNIEXPORT void JNICALL
Java_com_sponsorflow_nexus_ai_LlamaBridge_unloadModelNative(
    JNIEnv* env,
    jobject thiz,
    jlong modelHandle
) {
    if (g_ctx != nullptr) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    
    if (g_model != nullptr) {
        llama_free_model(g_model);
        g_model = nullptr;
        LOGI("Modelo liberado");
    }
}

JNIEXPORT jint JNICALL
Java_com_sponsorflow_nexus_ai_LlamaBridge_getTokenCountNative(
    JNIEnv* env,
    jobject thiz,
    jlong modelHandle,
    jstring text
) {
    // TODO: Implementar tokenización
    const char* textStr = env->GetStringUTFChars(text, nullptr);
    int count = strlen(textStr) / 4; // Estimación aproximada
    env->ReleaseStringUTFChars(text, textStr);
    return count;
}

}