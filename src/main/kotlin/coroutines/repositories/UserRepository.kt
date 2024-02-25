package coroutines.repositories

import coroutines.CoroutineObserver
import enums.Status
import kotlinx.coroutines.delay
import user.User
import wallet.Wallet
import kotlin.random.Random

class UserRepository {
    private val users: MutableList<User> = mutableListOf()

    companion object {
        val DEFAULT_USERS = listOf(
            User("1@gmail.com", "firstUser"),
            User("2@gmail.com", "secondUser"),
            User("3@gmail.com", "thirdUser")
        )
    }

    suspend fun init() {
        repeat(100) {
            val randomUser = DEFAULT_USERS.random()
            val randomDelay = Random.nextLong(100, 500)
            delay(randomDelay)
            users.add(randomUser)
            println("UserRepo size is $users.size")
        }
    }

    suspend fun saveUser(user: User): User {
        users.add(user)
        delay(Random.nextLong(100, 500))
        return user
    }

    private val userObservers = mutableListOf<CoroutineObserver>()

    fun addObserver(observer: CoroutineObserver) {
        userObservers.add(observer)
    }

    private fun notifyObservers(wallet: Wallet) {
        userObservers.forEach { it.notify(wallet.owner) }
    }

    fun changeUserStatus(wallet: Wallet) {
        wallet.owner.status = Status.APPROVED
        notifyObservers(wallet)
    }
}