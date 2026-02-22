//! AES-256-GCM Encryption

use aes_gcm::{aead::{Aead, KeyInit, OsRng}, Aes256Gcm, Nonce};
use thiserror::Error;

#[derive(Error, Debug)]
pub enum AesError {
    #[error("Invalid key size: {0}")]
    InvalidKeySize(usize),
    #[error("Encryption failed")]
    EncryptionFailed,
    #[error("Decryption failed")]
    DecryptionFailed,
}

/// Encripta datos con AES-256-GCM
pub fn encrypt(plaintext: &[u8], key: &[u8]) -> Result<Vec<u8>, AesError> {
    if key.len() != 32 { return Err(AesError::InvalidKeySize(key.len())); }
    let cipher = Aes256Gcm::new_from_slice(key).map_err(|_| AesError::EncryptionFailed)?;
    let nonce_bytes = rand_12();
    let nonce = Nonce::from_slice(&nonce_bytes);
    let ciphertext = cipher.encrypt(nonce, plaintext).map_err(|_| AesError::EncryptionFailed)?;
    let mut result = Vec::with_capacity(12 + ciphertext.len());
    result.extend_from_slice(&nonce_bytes);
    result.extend(ciphertext);
    Ok(result)
}

/// Desencripta datos con AES-256-GCM
pub fn decrypt(encrypted: &[u8], key: &[u8]) -> Result<Vec<u8>, AesError> {
    if key.len() != 32 { return Err(AesError::InvalidKeySize(key.len())); }
    if encrypted.len() < 12 { return Err(AesError::DecryptionFailed); }
    let nonce = Nonce::from_slice(&encrypted[..12]);
    let cipher = Aes256Gcm::new_from_slice(key).map_err(|_| AesError::DecryptionFailed)?;
    cipher.decrypt(nonce, &encrypted[12..]).map_err(|_| AesError::DecryptionFailed)
}

/// Genera clave aleatoria de 32 bytes
pub fn generate_key() -> [u8; 32] { rand_32() }

fn rand_12() -> [u8; 12] { use rand::RngCore; let mut b = [0u8; 12]; OsRng.fill_bytes(&mut b); b }
fn rand_32() -> [u8; 32] { use rand::RngCore; let mut b = [0u8; 32]; OsRng.fill_bytes(&mut b); b }