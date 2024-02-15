package service.impl

import service.ApplicationDependencies

class ApplicationDependenciesFirstImpl : ApplicationDependencies {
    override fun printFirstMessage() {
        println("message 1 from first impl")
    }

    override fun printSecondMessage() {
        println("message 2 from first impl")
    }
}