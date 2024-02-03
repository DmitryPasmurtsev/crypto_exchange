package service.impl

import service.ApplicationDependencies

class ApplicationDependenciesSecondImpl(
    private val applicationDependenciesFirstImpl: ApplicationDependenciesFirstImpl = ApplicationDependenciesFirstImpl()
) : ApplicationDependencies by applicationDependenciesFirstImpl {

    override fun printSecondMessage() {
        println("message 2 from second impl")
    }
}