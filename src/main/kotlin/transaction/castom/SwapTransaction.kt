package transaction.castom

import enums.Currency
import transaction.Transaction
import wallet.Wallet
import java.math.BigDecimal


class SwapTransaction(
    initiator: Wallet,
    fromCurrency: Currency,
    fromAmount: BigDecimal,
    val toCurrency: Currency,
    val toAmount: BigDecimal
) : Transaction(initiator, fromCurrency, fromAmount)