package сoroutines

import coroutines.CoroutineObserver
import coroutines.repositories.UserRepository
import coroutines.repositories.WalletRepository
import enums.Status
import exception.CoroutinesException
import kotlin.random.Random
import kotlin.random.nextInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import user.User
import wallet.Wallet

class CoroutinesTest {
    private val userRepository = UserRepository()
    private val walletRepository = WalletRepository()
    private val user = UserRepository.DEFAULT_USERS[0]
    private val wallet = WalletRepository.DEFAULT_WALLETS[0]
    private val supervisor = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("$exception was handled")
    }

    private suspend fun saveUser(user: User): User {
        return withContext(NonCancellable + exceptionHandler) {
            delay(1000)
            println(user)
            userRepository.saveUser(user)
        }
    }

    private suspend fun saveWallet(wallet: Wallet): Wallet {
        return withContext(NonCancellable + exceptionHandler) {
            val num = Random.nextInt(0..50)
            require(num in 0..25) {
                throw CoroutinesException("Random exception was thrown")
            }
            delay(1000)
            println(wallet)
            walletRepository.saveWallet(wallet)
        }
    }

    private suspend fun saveUserAndWallet(user: User, wallet: Wallet): Pair<User, Wallet> {
        val userDeferred = saveUser(user)
        val walletDeferred = saveWallet(wallet)
        userDeferred.wallets += walletDeferred
        return Pair(userDeferred, walletDeferred)
    }

    @Test
    fun testJobCancellation(): Unit = runBlocking {
        val job = coroutineScope.async {
            try {
                saveUserAndWallet(user, wallet)
            } catch (e: CancellationException) {
                println(e.message)
            } catch (e: CoroutinesException) {
                println(e.message)
            }
        }
        job.start()
        delay(1000)

        //job.cancel()
        //coroutineScope.cancel()
        //coroutineContext.cancelChildren()

        job.await()
    }

    private fun processWallets(count: Int) {
        val wallets = walletRepository.getWallets(count * 50, 50 * (count + 1))
        for (wallet in wallets) {
            if (wallet.currencies.size > 2) {
                val user = wallet.owner
                user.status = Status.APPROVED
                userRepository.changeUserStatus(wallet)
            }
        }
    }

    private suspend fun firstJob() = runBlocking {
        launch(Dispatchers.Unconfined + supervisor) {
            var count = 0
            if (Random.nextInt(0..50) < 30) {
                throw RuntimeException("First job threw an exception")
            }
            while (count < 2) {
                try {

                    processWallets(count)
                    count++
                } catch (e: IndexOutOfBoundsException) {
                    println("First job waiting")
                    delay(1000)
                }
            }
        }

    }

    private fun secondJob() = runBlocking {
        launch(Dispatchers.Unconfined + supervisor) {
            ensureActive()
            val observer = object : CoroutineObserver {
                override fun notify(user: User) {
                    println("User ${user.id} status changed to ${user.status}")
                }
            }
            userRepository.addObserver(observer)
        }
    }

    @Test
    fun testParallelTasks() = runBlocking {
        val userRepoInit = launch(Dispatchers.IO) { userRepository.init() }
        val walletRepoInit = launch(Dispatchers.IO) { walletRepository.init() }
        val job1 = firstJob()
        val job2 = secondJob()

        userRepoInit.join()
        walletRepoInit.join()
        job1.join()
        job2.join()
    }
}