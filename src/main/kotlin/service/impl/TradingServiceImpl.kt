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
import transaction.Transaction
import transaction.castom.SwapTransaction
import transaction.castom.TradeTransaction
import user.User
import wallet.Wallet
import java.math.BigDecimal
import java.math.RoundingMode

class TradingServiceImpl(val exchanges: MutableSet<Exchange>) : TradingService {
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
            throw NoSuitableExchangeException("The exchange does not carry out transactions for the selected currencies")
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
            throw WrongPassphraseException("Wrong passphrase")
    }

    private fun checkUserStatus(user: User) {
        if (user.status == Status.NEW || user.status == Status.BLOCKED)
            throw UserStatusException("User has ${user.status} status")
    }

    private fun checkBalance(wallet: Wallet, currency: Currency, amount: BigDecimal) {
        val balance: BigDecimal
        try {
            balance = wallet.currencies.getValue(currency)
        } catch (e: NoSuchElementException) {
            throw WalletException("No such currency in wallet")
        }
        if (balance < amount) {
            throw WalletException("Not enough money on balance")
        }
    }

    private fun checkRandom(randomNumber: Int) {
        if (randomNumber !in 0..25)
            throw TransactionException("Transaction failed")
    }
}
