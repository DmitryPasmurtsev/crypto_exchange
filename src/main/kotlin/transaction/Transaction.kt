package transaction

import enums.Currency
import wallet.Wallet
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

open class Transaction(
    val initiator: Wallet,
    val fromCurrency: Currency,
    val fromAmount: BigDecimal
) {
    val id: UUID = UUID.randomUUID()
    var date: LocalDateTime = LocalDateTime.now()

    constructor(initiator: Wallet, fromCurrency: Currency, amount: BigDecimal, date: LocalDateTime) : this(
        initiator,
        fromCurrency,
        amount
    ) {
        this.date = date
    }
}

