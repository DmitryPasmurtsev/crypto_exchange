package wallet

import enums.Currency
import enums.Status
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import user.User
import java.math.BigDecimal


internal class WalletTest {
    private val USER_EMAIL = "test@test.com"
    private val USER_FULL_NAME = "New User"
    private val USER_STATUS = Status.APPROVED

    private lateinit var user: User
    private lateinit var wallet1: Wallet
    private lateinit var wallet2: Wallet

    @BeforeEach
    fun setUp() {
        user = User(USER_EMAIL, USER_FULL_NAME, USER_STATUS)
        wallet1 = Wallet("TestWallet1", "passphrase1", user).apply {
            currencies = mutableMapOf(
                Currency.BITCOIN to BigDecimal(1000),
                Currency.ETHEREUM to BigDecimal(500)
            )
        }
        wallet2 = Wallet("TestWallet2", "passphrase2", user).apply {
            currencies = mutableMapOf(
                Currency.BITCOIN to BigDecimal(1000)
            )
        }
    }

    @Test
    fun `plus`() {
        val expectedCurrencies = mutableMapOf(
            Currency.BITCOIN to BigDecimal(2000),
            Currency.ETHEREUM to BigDecimal(500)
        )
        val newWallet = wallet1 + wallet2

        assertEquals(newWallet.name, wallet1.name)
        assertEquals(newWallet.passphrase, wallet1.passphrase)
        assertEquals(newWallet.owner, wallet1.owner)
        assertEquals(expectedCurrencies, newWallet.currencies)
    }

    @Test
    fun testDelegationObservable() {
        wallet1.description = "Description 1"
        wallet1.description = "Description 2"
        assertEquals(wallet1.msg, "default to Description 1 Description 1 to Description 2 ")
    }
}