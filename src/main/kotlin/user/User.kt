package user

import wallet.Wallet
import enums.Status
import java.util.UUID

class User(
    val id: UUID,
    var email: String,
    var fullName: String,
    var status: Status
) {
    private val walletMutableSet = mutableSetOf<Wallet>()
    var wallets
        get() = walletMutableSet.filter { !it.isCold }.toMutableSet()
        set(value) {
            walletMutableSet.addAll(value)
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

}


