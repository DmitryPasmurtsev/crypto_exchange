package coroutines.repositories

import enums.Currency
import kotlinx.coroutines.delay
import wallet.Wallet
import java.math.BigDecimal
import kotlin.random.Random

class WalletRepository {
    private val wallets: MutableList<Wallet> = mutableListOf()

    companion object {
        val DEFAULT_WALLETS = listOf(
            Wallet("firstFile", "pass1", UserRepository.DEFAULT_USERS[0]).apply {
                currencies = mutableMapOf(
                    Currency.BITCOIN to BigDecimal(200),
                    Currency.ETHEREUM to BigDecimal(500),
                    Currency.TON to BigDecimal(100)
                )
            },
            Wallet("firstSecond", "pass2", UserRepository.DEFAULT_USERS[1]).apply {
                currencies = mutableMapOf(
                    Currency.BITCOIN to BigDecimal(300),
                )
            }
        )
    }

    suspend fun init() {
        repeat(100) {
            val randomWallet = DEFAULT_WALLETS.random()
            val randomDelay = Random.nextLong(100, 500)
            delay(randomDelay)
            wallets.add(randomWallet)
            println("WalletRepo size is $wallets.size")
        }
    }

    fun getWallets(from: Int, to: Int): List<Wallet> {
        val walletsList = mutableListOf<Wallet>()
        for (i in from..to) {
            walletsList += wallets[i]
        }
        return walletsList
    }

    suspend fun saveWallet(wallet: Wallet): Wallet {
        wallets.add(wallet)
        delay(Random.nextLong(100, 500))
        return wallet
    }
}