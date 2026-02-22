//! JNI Basic Functions - Prueba y utilidades

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
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_getRustVersion(
    mut env: JNIEnv, _class: JClass) -> jstring {
    env.new_string(VERSION).unwrap().into_raw()
}

/// Saludo desde Rust
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_greet(
    mut env: JNIEnv, _class: JClass, name: JString) -> jstring {
    let name_str: String = env.get_string(&name).unwrap().into();
    let greeting = format!("¡Hola desde Rust, {}! 🦀", name_str);
    env.new_string(&greeting).unwrap().into_raw()
}

/// Health check
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_healthCheck(
    _env: JNIEnv, _class: JClass) -> jboolean { 1 }