package service.impl

import enums.Currency
import enums.Status
import exception.NoSuitableExchangeException
import exception.TransactionException
import exception.UserStatusException
import exception.WalletException
import exception.WrongPassphraseException
import exchange.Exchange
import service.TradingService
import transaction.SwapTransaction
import transaction.TradeTransaction
import transaction.Transaction
import user.User
import user.walletsAmount
import wallet.Wallet
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class TradingServiceImpl(val exchanges: MutableSet<Exchange>) : TradingService {

    companion object {
        private const val WRONG_PASSPHRASE = "Wrong passphrase"
        private const val INVALID_USER_STATUS_MESSAGE = "User status is %s"
        private const val NO_SUCH_CURRENCY_MESSAGE = "No such currency in wallet"
        private const val NOT_ENOUGH_MONEY_MESSAGE = "Not enough money on balance"
        private const val TRANSACTION_FAILED_MESSAGE = "Transaction failed"
        private const val NO_SUITABLE_EXCHANGE_MESSAGE =
            "The exchange does not carry out transactions for the selected currencies"
        private const val MIN_WALLETS_AMOUNT = 2
    }

    override fun tradeTransaction(
        initiator: Wallet,
        receiver: Wallet,
        fromCurrency: Currency,
        fromAmount: BigDecimal,
        toCurrency: Currency,
        exchange: Exchange
    ): Transaction {
        checkUserStatus(initiator.owner)
        checkUserStatus(receiver.owner)

        checkBalance(initiator, fromCurrency, fromAmount)

        val rate = getExchangeRate(exchange, fromCurrency, toCurrency)
        val transaction = TradeTransaction(
            initiator = initiator,
            receiver = receiver,
            fromAmount = fromAmount,
            fromCurrency = fromCurrency,
            toCurrency = toCurrency,
            toAmount = fromAmount.divide(rate, 2, RoundingMode.HALF_UP)
        )

        moneyTransfer(fromCurrency, fromAmount, toCurrency, initiator, receiver, rate)

        exchange.transactionHistory.add(transaction)

        return transaction
    }

    override fun swapTransaction(
        initiator: Wallet,
        passphrase: String,
        fromCurrency: Currency,
        fromAmount: BigDecimal,
        toCurrency: Currency,
        exchange: Exchange,
        randomNumber: Int
    ): Transaction {
        checkBalance(initiator, fromCurrency, fromAmount)
        checkPassphrase(passphrase, initiator)
        checkRandom(randomNumber)
        val rate = getExchangeRate(exchange, fromCurrency, toCurrency)

        moneyTransfer(fromCurrency, fromAmount, toCurrency, initiator, initiator, rate)

        val transaction = SwapTransaction(
            initiator = initiator,
            fromCurrency = fromCurrency,
            fromAmount = fromAmount,
            toCurrency = toCurrency,
            toAmount = fromAmount.divide(rate, 2, RoundingMode.HALF_UP)
        )
        exchange.transactionHistory.add(transaction)

        return transaction
    }

    override fun addExchange(exchange: Exchange) {
        exchanges.add(exchange)
    }

    override fun getAvailableExchanges(toCurrency: Currency, fromCurrency: Currency): Set<Exchange> {
        return exchanges
            .filter { exchange: Exchange -> exchange.exchangeRates.containsKey(Pair(toCurrency, fromCurrency)) }
            .toSet()
    }

    private fun getExchangeRate(exchange: Exchange, fromCurrency: Currency, toCurrency: Currency): BigDecimal {
        try {
            return exchange.exchangeRates.getValue(Pair(toCurrency, fromCurrency))
        } catch (e: NoSuchElementException) {
            throw NoSuitableExchangeException(NO_SUITABLE_EXCHANGE_MESSAGE)
        }
    }

    private fun moneyTransfer(
        fromCurrency: Currency,
        fromAmount: BigDecimal,
        toCurrency: Currency,
        initiator: Wallet,
        receiver: Wallet,
        rate: BigDecimal
    ) {
        initiator.currencies[fromCurrency] = initiator
            .currencies[fromCurrency]!!
            .minus(fromAmount)
        val toAmount = fromAmount.divide(rate, 2, RoundingMode.HALF_UP)
        if (initiator.currencies.containsKey(toCurrency)) {
            receiver.currencies[toCurrency] = initiator.currencies.getValue(toCurrency) + toAmount
        } else {
            receiver.currencies[toCurrency] = toAmount
        }
    }

    private fun checkPassphrase(passphrase: String, wallet: Wallet) {
        if (passphrase != wallet.passphrase)
            throw WrongPassphraseException(WRONG_PASSPHRASE)
    }

    private fun checkUserStatus(user: User) {
        if (user.status == Status.NEW || user.status == Status.BLOCKED)
            throw UserStatusException(INVALID_USER_STATUS_MESSAGE.format(user.status))
    }

    private fun checkBalance(wallet: Wallet, currency: Currency, amount: BigDecimal) {
        val balance: BigDecimal
        try {
            balance = wallet.currencies.getValue(currency)
        } catch (e: NoSuchElementException) {
            throw WalletException(NO_SUCH_CURRENCY_MESSAGE)
        }
        if (balance < amount) {
            throw WalletException(NOT_ENOUGH_MONEY_MESSAGE)
        }
    }

    private fun checkRandom(randomNumber: Int) {
        if (randomNumber !in createRange())
            throw TransactionException(TRANSACTION_FAILED_MESSAGE)
    }

    fun createRange(
        min: Int = 0,
        max: Int = 25
    ): IntRange {
        return min..max
    }

    fun getUsersFilteredByWalletAmount(users: List<User>) : List<User> {
        return users.filter { user: User -> user.walletsAmount > MIN_WALLETS_AMOUNT }
    }

    fun createMapFromListAssociatedById(users: List<User>) : Map<UUID, User> {
        return users.associateBy { it.id }
    }

    fun createMapFromListAssociatedWithStatus(users: List<User>) : Map<User, Status> {
        return users.associateWith { it.status }
    }

    fun getWalletFromTransaction(transaction: Transaction) =
        when (transaction) {
            is SwapTransaction -> transaction.initiator
            is TradeTransaction -> transaction.receiver
        }

    fun fibonacci(n: Int): Long {
        tailrec fun fibonacci(a: Long, b: Long, count: Int): Long {
            return if (count == 0) a
            else fibonacci(b, a + b, count - 1)
        }
        return fibonacci(0, 1, n)
    }
}

fun Pair<Currency, Currency>.swapCurrenciesInRate(): Pair<Currency, Currency> {
    return second to first
}
