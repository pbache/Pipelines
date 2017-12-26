def testservice() {
  sh 'echo inservices'
  def serv = load 'packages/lambdas/services.groovy'
    serv.service1()
}
return this
