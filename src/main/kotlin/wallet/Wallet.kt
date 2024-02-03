package wallet

import enums.Currency
import user.User
import java.math.BigDecimal
import java.util.UUID

data class Wallet(
    val id: UUID,
    var name: String,
    var isCold: Boolean,
    var passphrase: String,
    val owner: User
) {
    var currencies = mutableMapOf<Currency, BigDecimal>()

    constructor(name: String, passphrase: String, owner: User) : this(
        UUID.randomUUID(),
        name,
        false,
        passphrase,
        owner
    )
}