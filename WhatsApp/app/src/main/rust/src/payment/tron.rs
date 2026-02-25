//! TRON Address Validation

use crate::crypto::hash::sha256;

/// Valida dirección TRON (Base58Check)
/// Las direcciones TRON tienen 34 caracteres y empiezan con 'T'
pub fn validate_address(address: &str) -> bool {
    if address.len() != 34 || !address.starts_with('T') {
        return false;
    }
    // Decodificar Base58 y verificar checksum
    match decode_base58check(address) {
        Some(bytes) => bytes.len() == 21 && bytes[0] == 0x41,
        None => false,
    }
}

/// Decodifica Base58Check
fn decode_base58check(s: &str) -> Option<Vec<u8>> {
    const ALPHABET: &[u8] = b"123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    let mut bytes = vec![0u8; 50];
    
    for c in s.bytes() {
        let idx = ALPHABET.iter().position(|&b| b == c)?;
        let mut carry = idx as u64;
        for b in bytes.iter_mut().rev() {
            carry += (*b as u64) * 58;
            *b = (carry % 256) as u8;
            carry /= 256;
        }
    }
    
    // Skip leading zeros
    let start = bytes.iter().position(|&b| b != 0)?;
    let decoded = bytes[start..].to_vec();
    
    // Verify checksum (last 4 bytes)
    if decoded.len() < 5 { return None; }
    let (payload, checksum) = decoded.split_at(decoded.len() - 4);
    let expected = &sha256(&sha256(payload))[..4];
    if checksum != expected { return None; }
    
    Some(payload.to_vec())
}

/// Convierte dirección TRON a hex
pub fn address_to_hex(address: &str) -> Option<String> {
    let bytes = decode_base58check(address)?;
    Some(crate::crypto::hash::to_hex(&bytes))
}