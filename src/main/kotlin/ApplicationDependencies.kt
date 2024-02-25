class ApplicationDependencies(config: Config.() -> Unit) {
    private val config = Config().apply(config)

    fun printConfigProperties(): String {
        val property1Value = if (config.isProperty1Initialized()) config.property1 else "Property1 is not initialized"
        val property2Value = if (config.isProperty2Initialized()) config.property2 else "Property2 is not initialized"
        return "$property1Value, $property2Value"
    }
}