package exchange

import enums.Currency
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import transaction.Transaction
import java.math.BigDecimal

data class Exchange(var name: String) {

    var exchangeRates = mutableMapOf<Pair<Currency, Currency>, BigDecimal>()

    var transactionHistory = mutableListOf<Transaction>()

    val description: String by DelegatedName()

    private inner class DelegatedName : ReadOnlyProperty<Exchange, String> {
        override fun getValue(thisRef: Exchange, property: KProperty<*>): String =
            "This is exchange with name ${thisRef.name}"
    }
}
