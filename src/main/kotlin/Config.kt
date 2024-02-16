class Config {
    lateinit var property1: String
    lateinit var property2: String

    fun isProperty1Initialized(): Boolean = this::property1.isInitialized
    fun isProperty2Initialized(): Boolean = this::property2.isInitialized

    fun initConfig(block: Config.() -> Unit) {
        block()
    }
}