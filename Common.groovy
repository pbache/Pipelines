def prep(){
  def name = sh ( script: 'git show --name-only origin/master' , returnStdout:true).trim().split('/')
  println("----------output-----------")
  for (i = 0; i < name.length; i++){
    println(name[i])
  if(name[i] == 'ui-web'){
    def pipeui=load 'Pipelines/ui-web.groovy'
    pipeui.testui()
  }
  else if(name[i] == 'lambdas'){
    def pipeserv=load 'Pipelines/lambdas.groovy'
    pipeserv.testservices()
  }
}
}
return this



