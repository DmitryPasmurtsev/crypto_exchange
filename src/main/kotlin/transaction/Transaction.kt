package transaction

import enums.Currency
import wallet.Wallet
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

sealed interface Transaction {
    val initiator: Wallet
    val fromCurrency: Currency
    val fromAmount: BigDecimal
    val id: UUID
    val date: LocalDateTime
}

