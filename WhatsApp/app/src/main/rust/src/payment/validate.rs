//! Payment Validation - TRC20

use crate::crypto::hash::from_hex;

/// Resultado de validación de transacción
pub struct TxValidation {
    pub valid: bool,
    pub amount: u64,
    pub from: Option<String>,
    pub to: Option<String>,
}

/// Valida transacción TRC20 básica
pub fn validate_trc20(tx_hex: &str, expected_amount: u64) -> TxValidation {
    let tx_bytes = match from_hex(tx_hex) {
        Some(b) => b,
        None => return TxValidation { valid: false, amount: 0, from: None, to: None },
    };
    
    // Validación básica de estructura
    if tx_bytes.len() < 100 {
        return TxValidation { valid: false, amount: 0, from: None, to: None };
    }
    
    // Extraer monto de la transacción (simplificado)
    let amount = extract_amount(&tx_bytes).unwrap_or(0);
    
    TxValidation {
        valid: amount >= expected_amount,
        amount,
        from: None,
        to: None,
    }
}

/// Extrae el monto de una transacción TRC20
/// CORREGIDO: Buscar selector en cualquier posición, no solo al inicio
fn extract_amount(data: &[u8]) -> Option<u64> {
    // Buscar patrón de transferencia TRC20
    // Transfer(address,uint256) selector: 0xa9059cbb
    let selector = [0xa9, 0x05, 0x9c, 0xbb];
    
    if data.len() < 68 { return None; }
    
    // CORREGIDO: Buscar el selector en cualquier posición de los datos
    // En transacciones TRON, el calldata puede no estar al inicio
    let selector_pos = data.windows(4).position(|w| w == selector)?;
    
    // El monto empieza 36 bytes después del selector (4 selector + 32 address)
    let amount_start = selector_pos + 36;
    let amount_end = amount_start + 32;
    
    if data.len() < amount_end { return None; }
    
    // Leer los últimos 8 bytes del uint256 (asumiendo que el monto cabe en u64)
    let amount_bytes: [u8; 8] = data[amount_end - 8..amount_end].try_into().ok()?;
    Some(u64::from_be_bytes(amount_bytes))
}

/// Genera QR de pago TRON
pub fn generate_qr(wallet: &str, amount: u64) -> String {
    format!("tron:{}?amount={}&token=USDT", wallet, amount)
}