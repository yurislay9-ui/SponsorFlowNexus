//! JNI Crypto Functions

use jni::JNIEnv;
use jni::objects::{JClass, JByteArray};
use jni::sys::jbyteArray;

use crate::crypto::{encrypt, decrypt, generate_key};

/// Encripta con AES-256-GCM
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_encryptAesGcm(
    mut env: JNIEnv, _class: JClass, plaintext: JByteArray, key: JByteArray) -> jbyteArray {
    let pt = env.convert_byte_array(plaintext).unwrap_or_default();
    let k = env.convert_byte_array(key).unwrap_or_default();
    match encrypt(&pt, &k) {
        Ok(enc) => env.byte_array_from_slice(&enc).unwrap().into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// Desencripta con AES-256-GCM
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_decryptAesGcm(
    mut env: JNIEnv, _class: JClass, encrypted: JByteArray, key: JByteArray) -> jbyteArray {
    let enc = env.convert_byte_array(encrypted).unwrap_or_default();
    let k = env.convert_byte_array(key).unwrap_or_default();
    match decrypt(&enc, &k) {
        Ok(dec) => env.byte_array_from_slice(&dec).unwrap().into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// Genera clave AES aleatoria
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_generateAesKey(
    mut env: JNIEnv, _class: JClass) -> jbyteArray {
    let key = generate_key();
    env.byte_array_from_slice(&key).unwrap().into_raw()
}

/// Hash BLAKE3
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_blake3Hash(
    mut env: JNIEnv, _class: JClass, data: JByteArray) -> jbyteArray {
    let d = env.convert_byte_array(data).unwrap_or_default();
    let hash = crate::crypto::blake3(&d);
    env.byte_array_from_slice(&hash).unwrap().into_raw()
}