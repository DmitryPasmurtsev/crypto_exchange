package service.impl

import enums.Currency
import enums.Status
import exchange.Exchange
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import service.PersonalAccountService
import transaction.Transaction
import user.User
import wallet.Wallet
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PersonalAccountServiceImplTest {
    private val USER_ID = UUID.randomUUID()
    private val USER_EMAIL = "test@test.com"
    private val USER_FULL_NAME = "Test User"
    private val USER_STATUS = Status.APPROVED

    private lateinit var personalAccountService: PersonalAccountService
    private lateinit var user: User
    private lateinit var exchange: Exchange
    private lateinit var wallet1: Wallet
    private lateinit var wallet2: Wallet

    @BeforeEach
    fun setUp() {
        user = User(USER_EMAIL, USER_FULL_NAME, USER_STATUS)
        exchange = Exchange("TestExchange")
        wallet1 = Wallet("TestWallet1", "passphrase1", user)
        wallet2 = Wallet("TestWallet2", "passphrase2", user)

        personalAccountService = PersonalAccountServiceImpl()
    }

    @Test
    fun testGetBalanceForOneWallet() {
        wallet1.currencies[Currency.TON] = BigDecimal(500)
        wallet1.currencies[Currency.BITCOIN] = BigDecimal(200)

        val expected = mapOf(
            Currency.TON to BigDecimal(500),
            Currency.BITCOIN to BigDecimal(200)
        )
        val actual = personalAccountService.getBalance(wallet1)
        assertEquals(expected, actual)
    }

    @Test
    fun testGetBalanceForTwoWallets() {
        wallet1.currencies[Currency.TON] = BigDecimal("100")
        wallet1.currencies[Currency.BITCOIN] = BigDecimal("200")

        wallet2.currencies[Currency.TON] = BigDecimal("500")
        wallet2.currencies[Currency.ETHEREUM] = BigDecimal("300")

        val expected = mapOf(
            Currency.TON to BigDecimal(600),
            Currency.BITCOIN to BigDecimal(200),
            Currency.ETHEREUM to BigDecimal(300)
        )
        val actual = personalAccountService.getBalance(wallet1, wallet2)
        assertEquals(expected, actual)
    }

    @Test
    fun testGetTransactionsForPeriod() {
        val transaction1 = Transaction(wallet1, Currency.BITCOIN, BigDecimal("50"), LocalDateTime.now().minusDays(2))
        val transaction2 = Transaction(wallet1, Currency.TON, BigDecimal("70"), LocalDateTime.now().minusDays(1))
        val transaction3 = Transaction(wallet1, Currency.ETHEREUM, BigDecimal("100"), LocalDateTime.now())

        exchange.transactionHistory.addAll(listOf(transaction1, transaction2, transaction3))

        val from = LocalDateTime.now().minusDays(2)
        val to = LocalDateTime.now().plusDays(1)

        val transactions = personalAccountService.getTransactionsForPeriod(user, exchange, from, to)

        assertEquals(2, transactions.size)
        assertTrue(transactions.all { it.initiator == wallet1 })
        assertTrue(transactions.all { it.date.isAfter(from) && it.date.isBefore(to) })
    }

    @Test
    fun testAddWallet() {
        personalAccountService.addWallet(user, wallet1)

        assertEquals(1, user.wallets.size)
        assertTrue(user.wallets.contains(wallet1))
    }
}
