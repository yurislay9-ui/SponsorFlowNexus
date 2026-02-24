//! Key Derivation - Argon2id
//! CORREGIDO: Manejo de errores sin unwrap() para evitar panics en JNI

use argon2::{Algorithm, Argon2, Params, Version};
use rand::RngCore;
use rand::rngs::OsRng;

/// Error en derivación de clave
#[derive(Debug)]
pub enum DeriveError {
    InvalidParams(String),
    HashFailed(String),
}

/// Genera salt aleatorio de 16 bytes
pub fn generate_salt() -> [u8; 16] {
    let mut salt = [0u8; 16];
    OsRng.fill_bytes(&mut salt);
    salt
}

/// Deriva clave de 32 bytes desde password
/// CORREGIDO: Retorna Result en lugar de usar unwrap()
pub fn derive_key(password: &str, salt: &[u8; 16]) -> Result<[u8; 32], DeriveError> {
    let params = Params::new(65536, 3, 4, Some(32))
        .map_err(|e| DeriveError::InvalidParams(e.to_string()))?;
    
    let argon2 = Argon2::new(Algorithm::Argon2id, Version::V0x13, params);
    let mut key = [0u8; 32];
    
    argon2.hash_password_into(password.as_bytes(), salt, &mut key)
        .map_err(|e| DeriveError::HashFailed(e.to_string()))?;
    
    Ok(key)
}

/// Versión segura que retorna clave por defecto en caso de error
/// Usar solo cuando un error no es crítico
pub fn derive_key_or_default(password: &str, salt: &[u8; 16]) -> [u8; 32] {
    derive_key(password, salt).unwrap_or_else(|_| {
        // Retornar clave vacía en caso de error
        // El llamador debe verificar que no sea todo ceros
        [0u8; 32]
    })
}
