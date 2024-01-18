package service.impl

import enums.Currency
import enums.Status
import exception.UserStatusException
import exchange.Exchange
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import user.User
import wallet.Wallet
import java.math.BigDecimal

class TradingServiceImplTest {
    private lateinit var tradingService: TradingServiceImpl
    private val user1 = User("111@gmail.com", "user1", Status.APPROVED)
    private val user2 = User("222@gmail.com", "user2", Status.APPROVED)
    private val wallet11 = Wallet("wallet11", "passphrase", user1)
    private val wallet12 = Wallet("wallet12", "passphrase", user1)
    private val wallet21 = Wallet("wallet21", "passphrase", user2)
    private val exchange = Exchange("TestExchange")


    @BeforeEach
    fun setUp() {
        tradingService = TradingServiceImpl(mutableSetOf(exchange))
        wallet11.currencies = mutableMapOf(
            Currency.BITCOIN to BigDecimal(1000)
        )
        exchange.exchangeRates = mutableMapOf(
            Pair(Currency.BITCOIN, Currency.ETHEREUM) to BigDecimal(0.8),
            Pair(Currency.ETHEREUM, Currency.BITCOIN) to BigDecimal(1.25),
            Pair(Currency.BITCOIN, Currency.TON) to BigDecimal(0.7),
            Pair(Currency.TON, Currency.BITCOIN) to BigDecimal(1.43)
        )
    }

    @Test
    fun trade_shouldPerformTradeTransactionAndAddToTransactionHistory() {
        val amount = BigDecimal("100")

        val transaction = tradingService.tradeTransaction(wallet11, wallet21, Currency.BITCOIN , amount, Currency.TON, exchange)
        assert(exchange.transactionHistory.contains(transaction))
    }

    @Test
    fun swap_shouldPerformSwapTransactionAndAddToTransactionHistory() {
        val amount = BigDecimal(100)
        val randomNumber = 0

        val transaction = tradingService.swapTransaction(wallet11, "passphrase", Currency.BITCOIN, amount, Currency.ETHEREUM, exchange, randomNumber)
        assert(exchange.transactionHistory.contains(transaction))
    }


    @Test
    fun addExchange_shouldAddAnExchange() {
        val newExchange = Exchange("NewExchange")
        tradingService.addExchange(newExchange)
        assert(tradingService.exchanges.contains(newExchange))
    }

    @Test
    fun getAllExchanges_shouldReturnAllExchanges() {
        assertEquals(tradingService.getAvailableExchanges(Currency.BITCOIN, Currency.ETHEREUM).size, 1)
    }

    @Test
    fun trade_shouldThrowInvalidUserStatusExceptionIfSenderStatusIsNew() {
        user1.status = Status.NEW
        val amount = BigDecimal(100)

        assertThrows<UserStatusException> {
            tradingService.tradeTransaction(wallet11, wallet21, Currency.BITCOIN, amount, Currency.TON, exchange)
        }
    }

}
