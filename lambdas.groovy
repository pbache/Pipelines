def testservice() {
  sh 'echo inservices'
  sh '../packages/lambdas/testcode.sh'
}
return this
