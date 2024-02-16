package service.impl

import enums.Currency
import enums.Status
import exchange.Exchange
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import service.PersonalAccountService
import transaction.SwapTransaction
import transaction.Transaction
import user.User
import wallet.Wallet
import java.math.BigDecimal
import java.time.LocalDateTime

class PersonalAccountServiceImplTest {
    private val USER_EMAIL = "test@test.com"
    private val USER_FULL_NAME = "Amazing User"
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

        personalAccountService = PersonalAccountServiceImpl
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
        wallet1.currencies[Currency.TON] = BigDecimal(100)
        wallet1.currencies[Currency.BITCOIN] = BigDecimal(200)

        wallet2.currencies[Currency.TON] = BigDecimal(500)
        wallet2.currencies[Currency.ETHEREUM] = BigDecimal(300)

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
        val transaction1 = SwapTransaction(
            wallet1,
            Currency.BITCOIN,
            BigDecimal(50),
            Currency.TON,
            BigDecimal(60),
            LocalDateTime.now().minusDays(2)
        )
        val transaction2 = SwapTransaction(
            wallet1,
            Currency.TON,
            BigDecimal(50),
            Currency.ETHEREUM,
            BigDecimal(60),
            LocalDateTime.now().minusDays(1)
        )
        val transaction3 = SwapTransaction(
            wallet1,
            Currency.BITCOIN,
            BigDecimal(20),
            Currency.ETHEREUM,
            BigDecimal(40),
            LocalDateTime.now()
        )

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

        assertEquals(2, user.wallets.size)
        assertTrue(user.wallets.contains(wallet1))
    }

    @Test
    fun `compare speed of sequence and stream`() {
        var list1 = createUsers()
        var list2 = createUsers()

        val timeOfSequenceOperation = getTimeOfSequenceOperation(list1)
        val timeOfSimpleListOperation = getTimeOfSimpleListOperation(list2);

        assertTrue(timeOfSimpleListOperation > timeOfSequenceOperation)
    }

    private fun createUsers(): MutableList<User> {
        var list = mutableListOf<User>()
        //Начиная примерно со 100 тысяч элементов метод с использованием sequence
        //начинает значительно выигрывать в скорости.
        //Чем больше количество элементов в списке, тем больше становится разница в скорости.
        for (i in 0..1000000) {
            list += User(i.toString(), i.toString())
            if (i % 3 == 0) {
                list[i].fullName = USER_FULL_NAME
                list[i].status = Status.APPROVED
            }
        }
        return list
    }

    private fun getTimeOfSequenceOperation(list: MutableList<User>): Long {
        val startTimeSequence = System.currentTimeMillis()
        list.asSequence()
            .filter { it.status == Status.APPROVED }
            .map { it.fullName }
            .any { it.startsWith("A", ignoreCase = true) }
        val endTimeSequence = System.currentTimeMillis()
        println("sequence time" + (endTimeSequence - startTimeSequence))
        return endTimeSequence - startTimeSequence
    }

    private fun getTimeOfSimpleListOperation(list: MutableList<User>): Long {
        val startTimeList = System.currentTimeMillis()
        list.filter { it.status == Status.APPROVED }
            .map { it.fullName }
            .any { it.startsWith("A", ignoreCase = true) }
        val endTimeList = System.currentTimeMillis()
        println("list time" + (endTimeList - startTimeList))
        return endTimeList - startTimeList
    }

    @Test
    fun `infix convert test`() {
        val swapTransaction = SwapTransaction(
            wallet1,
            Currency.BITCOIN,
            BigDecimal(50),
            Currency.TON,
            BigDecimal(60),
            LocalDateTime.now().minusDays(2)
        )
        val convertedTransaction: Transaction? = swapTransaction convert Transaction::class.java
        val convertedString: String? = swapTransaction convert String::class.java

        assertEquals(swapTransaction.id, convertedTransaction?.id)
        assertNull(convertedString)
    }

    private infix fun <T> Any?.convert(clazz: Class<T>): T? {
        return try {
            clazz.cast(this)
        } catch (e: ClassCastException) {
            null
        }
    }
}
