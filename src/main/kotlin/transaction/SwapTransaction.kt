package transaction

import enums.Currency
import transaction.Transaction
import wallet.Wallet
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


data class SwapTransaction(
    override val initiator: Wallet,
    override val fromCurrency: Currency,
    override val fromAmount: BigDecimal,
    val toCurrency: Currency,
    val toAmount: BigDecimal
) : Transaction {
    override val id: UUID = UUID.randomUUID()
    override var date: LocalDateTime = LocalDateTime.now()

    constructor(
        initiator: Wallet,
        fromCurrency: Currency,
        fromAmount: BigDecimal,
        toCurrency: Currency,
        toAmount: BigDecimal,
        date: LocalDateTime
    ) : this (initiator, fromCurrency, fromAmount, toCurrency, toAmount) {
        this.date = date;
    }
}