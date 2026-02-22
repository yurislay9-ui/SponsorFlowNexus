//! SponsorFlow Nexus - Rust Native Library v0.1.0
//!
//! Módulos nativos para Android:
//! - crypto: AES-256-GCM, Argon2, BLAKE3
//! - payment: Validación TRC20, TRON
//! - jni: Puente JNI para Kotlin

pub mod crypto;
pub mod payment;
pub mod jni;

/// Versión de la librería
pub const VERSION: &str = env!("CARGO_PKG_VERSION");

/// Función simple de prueba
pub fn add(a: i32, b: i32) -> i32 {
    a + b
}

#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn test_add() {
        assert_eq!(add(2, 3), 5);
    }
}