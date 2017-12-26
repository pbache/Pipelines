def testservice() {
  sh 'echo inservices'
  sh '/bin/bash packages/lambdas/testcode.sh'
}
return this
