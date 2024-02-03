package user

import enums.Currency
import wallet.Wallet
import enums.Status
import java.math.BigDecimal
import java.util.*

data class User(
    val id: UUID,
    var email: String?,
    var fullName: String,
    var status: Status
) {
    private val walletMutableSet = mutableSetOf<Wallet>()
    var wallets
        get() = walletMutableSet.filter { !it.isCold }.toMutableSet()
        set(value) {
            walletMutableSet.addAll(value)
        }

    init {
        val wallet= Wallet("newWallet","newPass",this)
        wallet.currencies+= mapOf(Currency.BITCOIN to BigDecimal.TEN)
        wallets= mutableSetOf(wallet)
    }

    constructor(email: String, fullName: String) : this(
        UUID.randomUUID(),
        email,
        fullName,
        Status.NEW
    )

    constructor(email: String, fullName: String, status: Status) : this(
        UUID.randomUUID(),
        email,
        fullName,
        status
    )

    fun destruction(user: User): String {
        val (id, email, _, status) = user
        return "user with id $id have status $status"
    }

    fun getEmailInUpperCaseAndWithoutDomain(user: User): User {
        user.email = user.email?.substringBefore('@')?.uppercase() ?: ""
        return user
    }

}

val User.walletsAmount: Int
    get() = this.wallets.size


