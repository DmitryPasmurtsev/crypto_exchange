package config

class Config {
    lateinit var firstProperty: String
    lateinit var secondProperty: String

    fun isProperty1Initialized(): Boolean = this::firstProperty.isInitialized
    fun isProperty2Initialized(): Boolean = this::secondProperty.isInitialized

    fun initConfig(block: Config.() -> Unit) {
        block()
    }
}