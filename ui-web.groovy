def testui() {
  sh 'echo inui-web'
  sh '/bin/bash packages/ui-web/testuicode.sh'
}
return this
