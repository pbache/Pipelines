//def me =sh (script:'git whatchanged -n 1 --pretty=format: --name-only',returnStdout:true).trim().split('/')
//def pckg= me.length>1?me[1]:'.'
//currentBuild.displayName = pckg+currentBuild.displayName
/*if(pckg == 'ui-web'){
  def pipeui=load 'pipeline/ui-web.groovy'
  pipeui.testui()
}
else if(pckg == 'lambdas'){
  def pipeserv=load 'pipeline/ui-web.groovy'
  pipeserv.testservices()
}
*/







def name = sh ( script: 'git show --name-only origin/master' , returnStdout:true).trim()/split('/')
println("----------output-----------")
for (i = 0; i < name.length; i++)
  printlln(name[i])
if(name[i] == 'ui-web'){
  def pipeui=load 'pipeline/ui-web.groovy'
  pipeui.testui()
}
else if(name[i] == 'lambdas'){
  def pipeserv=load 'pipeline/ui-web.groovy'
  pipeserv.testservices()
}

