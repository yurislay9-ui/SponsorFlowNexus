//! Key Derivation - Argon2id

use argon2::{Algorithm, Argon2, Params, Version};
use rand::RngCore;
use rand::rngs::OsRng;

/// Genera salt aleatorio de 16 bytes
pub fn generate_salt() -> [u8; 16] {
    let mut salt = [0u8; 16];
    OsRng.fill_bytes(&mut salt);
    salt
}

/// Deriva clave de 32 bytes desde password
pub fn derive_key(password: &str, salt: &[u8; 16]) -> [u8; 32] {
    let params = Params::new(65536, 3, 4, Some(32)).unwrap();
    let argon2 = Argon2::new(Algorithm::Argon2id, Version::V0x13, params);
    let mut key = [0u8; 32];
    argon2.hash_password_into(password.as_bytes(), salt, &mut key).unwrap();
    key
}