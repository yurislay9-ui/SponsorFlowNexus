//! Hashing - BLAKE3 y SHA-256

use blake3::Hasher;
use sha2::{Sha256, Digest};

/// Hash BLAKE3 (32 bytes) - más rápido que SHA-256
pub fn blake3(data: &[u8]) -> [u8; 32] {
    let mut h = Hasher::new();
    h.update(data);
    *h.finalize().as_bytes()
}

/// Hash BLAKE3 como hex string
pub fn blake3_hex(data: &[u8]) -> String { to_hex(&blake3(data)) }

/// Hash SHA-256 (32 bytes)
pub fn sha256(data: &[u8]) -> [u8; 32] {
    let mut h = Sha256::new();
    h.update(data);
    let r = h.finalize();
    let mut out = [0u8; 32];
    out.copy_from_slice(&r);
    out
}

/// Hash SHA-256 como hex string
pub fn sha256_hex(data: &[u8]) -> String { to_hex(&sha256(data)) }

/// Double SHA-256 (Bitcoin/TRON)
pub fn double_sha256(data: &[u8]) -> [u8; 32] { sha256(&sha256(data)) }

/// Convierte bytes a hex
pub fn to_hex(bytes: &[u8]) -> String {
    bytes.iter().map(|b| format!("{:02x}", b)).collect()
}

/// Convierte hex a bytes
pub fn from_hex(hex: &str) -> Option<Vec<u8>> {
    (0..hex.len()).step_by(2)
        .map(|i| u8::from_str_radix(&hex[i..i+2], 16).ok())
        .collect()
}