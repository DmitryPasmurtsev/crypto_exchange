package service

import enums.Currency
import exchange.Exchange
import transaction.Transaction
import user.User
import wallet.Wallet
import java.math.BigDecimal
import java.time.LocalDateTime

interface PersonalAccountService {
    fun getBalance(vararg wallets: Wallet) : MutableMap<Currency, BigDecimal>
    fun getTransactionsForPeriod(
        user: User,
        exchange: Exchange,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<Transaction>

    fun addWallet(user: User, wallet: Wallet)
}