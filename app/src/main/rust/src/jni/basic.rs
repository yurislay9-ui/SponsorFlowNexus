//! JNI Basic Functions - Prueba y utilidades
//! CORREGIDO: Manejo de errores sin unwrap() para evitar panics en JNI

use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jint, jstring, jboolean};

use crate::{add, VERSION};

/// Suma dos números
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_addNumbers(
    _env: JNIEnv, _class: JClass, a: jint, b: jint) -> jint {
    add(a, b)
}

/// Obtiene versión de Rust
/// CORREGIDO: Manejar error sin unwrap()
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_getRustVersion(
    mut env: JNIEnv, _class: JClass) -> jstring {
    match env.new_string(VERSION) {
        Ok(s) => s.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// Saludo desde Rust
/// CORREGIDO: Manejar error sin unwrap()
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_greet(
    mut env: JNIEnv, _class: JClass, name: JString) -> jstring {
    
    let name_str: String = match env.get_string(&name) {
        Ok(s) => s.into(),
        Err(_) => return std::ptr::null_mut(),
    };
    
    let greeting = format!("¡Hola desde Rust, {}! 🦀", name_str);
    
    match env.new_string(&greeting) {
        Ok(s) => s.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// Health check
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_healthCheck(
    _env: JNIEnv, _class: JClass) -> jboolean { 1 }
