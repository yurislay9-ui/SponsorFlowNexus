//! JNI Payment Functions
//! CORREGIDO: Manejo de errores sin unwrap() para evitar panics en JNI

use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jstring, jlong};

use crate::payment::{validate_address, generate_qr, validate_trc20};

/// Valida dirección TRON
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_validateTronAddress(
    mut env: JNIEnv, _class: JClass, address: JString) -> jboolean {
    let addr: String = match env.get_string(&address) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };
    if validate_address(&addr) { 1 } else { 0 }
}

/// Genera QR de pago TRON
/// CORREGIDO: Manejar error sin unwrap()
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_generatePaymentQr(
    mut env: JNIEnv, _class: JClass, wallet: JString, amount: jlong) -> jstring {
    
    let wallet_str: String = match env.get_string(&wallet) {
        Ok(s) => s.into(),
        Err(_) => return std::ptr::null_mut(),
    };
    
    let qr = generate_qr(&wallet_str, amount as u64);
    
    match env.new_string(&qr) {
        Ok(s) => s.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// Valida transacción TRC20
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_verifyTrc20Transaction(
    mut env: JNIEnv, _class: JClass, tx_hex: JString, expected_amount: jlong) -> jboolean {
    let hex: String = match env.get_string(&tx_hex) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };
    let result = validate_trc20(&hex, expected_amount as u64);
    if result.valid { 1 } else { 0 }
}