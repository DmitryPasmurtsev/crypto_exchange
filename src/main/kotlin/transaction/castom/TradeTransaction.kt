package transaction.castom

import enums.Currency
import transaction.Transaction
import wallet.Wallet
import java.math.BigDecimal

class TradeTransaction(
    initiator: Wallet,
    fromCurrency: Currency,
    fromAmount: BigDecimal,
    val receiver: Wallet,
    toCurrency: Currency,
    toAmount: BigDecimal
) : Transaction(initiator, fromCurrency, fromAmount)