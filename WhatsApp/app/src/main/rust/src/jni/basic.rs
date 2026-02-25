//! JNI Basic Functions - Prueba y utilidades
//! CORREGIDO: Manejo de errores sin unwrap() para evitar panics en JNI

use jni::JNIEnv;
use jni::objects::{JClass, JString, JByteArray};
use jni::sys::{jint, jstring, jboolean, jbyteArray};

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

/// Hashea una contraseña usando Argon2
/// Implementación JNI para la función hashPassword
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_hashPassword(
    mut env: JNIEnv, _class: JClass, password: JString, salt: JByteArray) -> jbyteArray {
    
    // Obtener la contraseña como String
    let password_str: String = match env.get_string(&password) {
        Ok(s) => s.into(),
        Err(_) => return std::ptr::null_mut(),
    };
    
    // Obtener el salt como Vec<u8>
    let salt_bytes: Vec<u8> = match env.convert_byte_array(&salt) {
        Ok(bytes) => bytes,
        Err(_) => return std::ptr::null_mut(),
    };
    
    // Llamar a la función de hashing
    match crate::derive_key(&password_str, &salt_bytes) {
        Ok(hash) => {
            // Convertir el hash a jbyteArray
            match env.byte_array_from_slice(&hash) {
                Ok(array) => array.into_raw(),
                Err(_) => std::ptr::null_mut(),
            }
        },
        Err(_) => std::ptr::null_mut(),
    }
}
