def prep(){
  def name = sh ( script: 'git show --name-only origin/master' , returnStdout:true).trim().split('/')
  println("----------output-----------")
  for (i = 0; i < name.length; i++)
    printlln(name[i])
  if(name[i] == 'ui-web'){
    def pipeui=load 'ui-web.groovy'
    pipeui.testui()
  }
  else if(name[i] == 'lambdas'){
    def pipeserv=load 'lambdas.groovy'
    pipeserv.testservices()
  }
}
return this



