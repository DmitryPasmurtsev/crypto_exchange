package exchange

import enums.Currency
import transaction.Transaction
import java.math.BigDecimal

class Exchange(var name: String) {

    var exchangeRates = mutableMapOf<Pair<Currency, Currency>, BigDecimal>()

    var transactionHistory = mutableListOf<Transaction>()
}
