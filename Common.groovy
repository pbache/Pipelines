def prep(){
  def name = sh (script: 'git whatchanged -n 1 --pretty=format: --name-only',returnStdout:true).trim().split("/")
  //def pckg=name.length>1?name[1]:'full'
  //currentBuild.displayName = pckg+currentBuild.displayName
  println("----------output-----------")
  sh 'git log --pretty=online:"%h" --graph -<n>'
  switch (name){
    case 'ui-web':
      me = 'ui-web'
      break
    case 'lambdas':
      me = 'lambdas'
      break
    case 'default':
      break
  }
  //for (i = 0; i < name.length; i++){
  //    println(name[i])
   if(me == 'ui-web'){
     def pipeui=load 'Pipelines/ui-web.groovy'
     pipeui.testui()
     currentBuild.displayName = name[i]+currentBuild.displayName
   }
   if(me == 'lambdas'){
     def pipeserv=load 'Pipelines/lambdas.groovy'
     pipeserv.testservice()
     currentBuild.displayName = name[i]+currentBuild.displayName
    }
  }
return this



