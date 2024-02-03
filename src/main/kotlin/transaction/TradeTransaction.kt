package transaction

import enums.Currency
import transaction.Transaction
import wallet.Wallet
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class TradeTransaction(
    override val initiator: Wallet,
    override val fromCurrency: Currency,
    override val fromAmount: BigDecimal,
    val receiver: Wallet,
    val toCurrency: Currency,
    val toAmount: BigDecimal
) : Transaction {
    override val id: UUID = UUID.randomUUID()
    override val date: LocalDateTime = LocalDateTime.now()
}