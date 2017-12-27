def prep(){
  def name = sh (script: 'git whatchanged -n 1 --pretty=format: --name-only',returnStdout:true().trim().split('/')
  println("----------output-----------")
  for (i = 0; i < name.length; i++){
     println(name[i])
   if(name == 'ui-web'){
     def pipeui=load 'Pipelines/ui-web.groovy'
     pipeui.testui()
     currentBuild.displayName = name[i]+currentBuild.displayName
   }
   if(name == 'lambdas'){
     def pipeserv=load 'Pipelines/lambdas.groovy'
     pipeserv.testservice()
     currentBuild.displayName = name[i]+currentBuild.displayName
    }
  }
}
return this



