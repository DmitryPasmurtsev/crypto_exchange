package service.impl

import enums.Currency
import enums.Status
import exception.TransactionException
import exception.UserStatusException
import exception.WalletException
import exception.WrongPassphraseException
import exchange.Exchange
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import transaction.SwapTransaction
import transaction.TradeTransaction
import user.User
import wallet.Wallet
import java.math.BigDecimal

class TradingServiceImplTest {
    private lateinit var tradingService: TradingServiceImpl
    private val user1 = User("111@gmail.com", "user1", Status.APPROVED)
    private val user2 = User("222@gmail.com", "user2", Status.APPROVED)
    private val wallet11 = Wallet("wallet11", "passphrase", user1)
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
        val amount = BigDecimal(100)

        val transaction =
            tradingService.tradeTransaction(wallet11, wallet21, Currency.BITCOIN, amount, Currency.TON, exchange)
        assert(exchange.transactionHistory.contains(transaction))
    }

    @Test
    fun swap_shouldPerformSwapTransactionAndAddToTransactionHistory() {
        val amount = BigDecimal(100)
        val randomNumber = 0

        val transaction = tradingService.swapTransaction(
            wallet11,
            "passphrase",
            Currency.BITCOIN,
            amount,
            Currency.ETHEREUM,
            exchange,
            randomNumber
        )
        assert(exchange.transactionHistory.contains(transaction))
    }

    @Test
    fun swap_shouldThrowTransactionException_whenRandomNumberNotInRange() {
        val amount = BigDecimal(100)
        val randomNumber = 50

        assertThrows<TransactionException> {
            tradingService.swapTransaction(
                wallet11,
                "passphrase",
                Currency.BITCOIN,
                amount,
                Currency.ETHEREUM,
                exchange,
                randomNumber
            )
        }
    }

    @Test
    fun swap_shouldThrowPassphraseException_whenPassphraseIsWrong() {
        val amount = BigDecimal(100)
        val randomNumber = 0

        assertThrows<WrongPassphraseException> {
            tradingService.swapTransaction(
                wallet11,
                "wrong_passphrase",
                Currency.BITCOIN,
                amount,
                Currency.ETHEREUM,
                exchange,
                randomNumber
            )
        }
    }

    @Test
    fun addExchange_shouldAddAnExchange() {
        val newExchange = Exchange("NewExchange")
        tradingService.addExchange(newExchange)
        assert(tradingService.exchanges.contains(newExchange))
    }

    @Test
    fun getAvailableExchanges_whenAvailableExchangeExists() {
        assertEquals(1, tradingService.getAvailableExchanges(Currency.BITCOIN, Currency.ETHEREUM).size)
    }

    @Test
    fun getAvailableExchanges_whenAvailableExchangeNotExists() {
        assertEquals(0, tradingService.getAvailableExchanges(Currency.TON, Currency.ETHEREUM).size)
    }

    @Test
    fun trade_shouldThrowInvalidUserStatusExceptionIfSenderStatusIsNew() {
        user1.status = Status.NEW
        val amount = BigDecimal(100)

        assertThrows<UserStatusException> {
            tradingService.tradeTransaction(wallet11, wallet21, Currency.BITCOIN, amount, Currency.TON, exchange)
        }
    }

    @Test
    fun trade_shouldThrowUserWalletException_whenNotEnoughMoney() {
        val amount = BigDecimal(2000)

        assertThrows<WalletException> {
            tradingService.tradeTransaction(wallet11, wallet21, Currency.BITCOIN, amount, Currency.TON, exchange)
        }
    }

    @Test
    fun `test range creation with default size`() {
        val expected = 0..25
        assertEquals(expected, tradingService.createRange())
    }

    @Test
    fun `test range creation with given size`() {
        val expected = 3..10
        assertEquals(expected, tradingService.createRange(3, 10))
    }

    @Test
    fun `test filter users list by wallet amount`() {
        user1.wallets =
            mutableSetOf(
                Wallet("name1", "passphrase", user1),
                Wallet("name2", "passphrase", user1),
                Wallet("name3", "passphrase", user1)
            )
        user2.wallets =
            mutableSetOf(
                Wallet("name1", "passphrase", user2)
            )
        val expected: List<User> = listOf(user1)

        val actual: List<User> = tradingService.getUsersFilteredByWalletAmount(listOf(user1, user2))

        assertEquals(expected, actual)
    }

    @Test
    fun `test create map from list associated by id`() {
        val list = listOf(user1)
        val expected = mapOf(user1.id to user1)

        val actual = tradingService.createMapFromListAssociatedById(list)

        assertEquals(expected, actual)
    }

    @Test
    fun `test create map from list associated with status`() {
        val list = listOf(user1)
        val expected = mapOf(user1 to user1.status)

        val actual = tradingService.createMapFromListAssociatedWithStatus(list)

        assertEquals(expected, actual)
    }

    @Test
    fun `test get wallet from transaction when swap transaction`() {
        val transaction = SwapTransaction(
            wallet11,
            Currency.BITCOIN,
            BigDecimal(50),
            Currency.TON,
            BigDecimal(60),
        )

        val actual = tradingService.getWalletFromTransaction(transaction)

        assertEquals(transaction.initiator, actual)
    }

    @Test
    fun `test get wallet from transaction when trade transaction`() {
        val transaction = TradeTransaction(
            wallet11,
            Currency.BITCOIN,
            BigDecimal(50),
            wallet21,
            Currency.ETHEREUM,
            BigDecimal(60)
        )

        val actual = tradingService.getWalletFromTransaction(transaction)

        assertEquals(transaction.receiver, actual)
    }

    @Test
    fun `test fibonacci`() {
        val expected: Long = 55

        val actual = tradingService.fibonacci(10)

        assertEquals(expected, actual)
    }

    @Test
    fun `swap currencies test`() {
        val pair = Currency.BITCOIN to Currency.ETHEREUM
        val expected = Currency.ETHEREUM to Currency.BITCOIN

        val actual = pair.swapCurrenciesInRate()

        assertEquals(expected, actual)
    }
}
