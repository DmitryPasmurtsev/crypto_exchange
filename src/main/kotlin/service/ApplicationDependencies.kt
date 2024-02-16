package service

interface ApplicationDependencies : AutoCloseable{
    fun printFirstMessage()
    fun printSecondMessage()
    override fun close() {
        println("Closing ApplicationDependencies")
    }
}