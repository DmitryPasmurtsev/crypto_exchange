package config

class ApplicationDependencies(config: Config.() -> Unit) {
    private val config = Config().apply(config)
    fun printConfigProperties(): String {
        val property1Value = if (config.isProperty1Initialized()) config.firstProperty else "FirstProperty is not initialized"
        val property2Value = if (config.isProperty2Initialized()) config.secondProperty else "SecondProperty is not initialized"
        return "$property1Value, $property2Value"
    }
}