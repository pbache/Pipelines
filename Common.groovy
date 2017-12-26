def prep(){
  def name = sh ( script: 'git whatchanged -n 1 --pretty=format: --name-only' , returnStdout:true).trim().split('/')
  println("----------output-----------")
  for (i = 0; i < name.length; i++){
    println(name[i])
  if(name[i] == 'ui-web'){
    def pipeui=load 'Pipelines/ui-web.groovy'
    pipeui.testui()
  }
  if(name[i] == 'lambdas'){
    def pipeserv=load 'Pipelines/lambdas.groovy'
    pipeserv.testservice()
  }
}
}
return this



