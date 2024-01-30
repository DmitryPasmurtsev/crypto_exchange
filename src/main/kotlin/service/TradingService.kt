package service

import enums.Currency
import exchange.Exchange
import transaction.Transaction
import wallet.Wallet
import java.math.BigDecimal

interface TradingService {
    fun swapTransaction(
        initiator: Wallet,
        passphrase: String,
        fromCurrency: Currency,
        fromAmount: BigDecimal,
        toCurrency: Currency,
        exchange: Exchange,
        randomNumber: Int
    ): Transaction

    fun addExchange(exchange: Exchange)
    fun tradeTransaction(
        initiator: Wallet,
        receiver: Wallet,
        fromCurrency: Currency,
        fromAmount: BigDecimal,
        toCurrency: Currency,
        exchange: Exchange
    ): Transaction

    fun getAvailableExchanges(toCurrency: Currency, fromCurrency: Currency): Set<Exchange>
}