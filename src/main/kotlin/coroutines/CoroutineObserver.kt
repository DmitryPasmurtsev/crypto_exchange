package coroutines

import user.User

interface CoroutineObserver {
    fun notify(user: User)
}