def prep(){
  def name = sh ( script: 'git show --name-only origin/master' , returnStdout:true).trim().split("/")[0]
  println("----------output-----------")
  for (i = 0; i < name.length; i++){
    println(name[i])
  if(name[i] == 'ui-web'){
    def pipeui=load 'Pipelines/ui-web.groovy'
    pipeui.testui()
    currentBuild.displayName = name[i]+currentBuild.displayName
  }
  if(name[i] == 'lambdas'){
    def pipeserv=load 'Pipelines/lambdas.groovy'
    pipeserv.testservice()
    currentBuild.displayName = name[i]+currentBuild.displayName
  }
}
}
return this



