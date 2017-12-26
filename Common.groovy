//def me =sh (script:'git whatchanged -n 1 --pretty=format: --name-only',returnStdout:true).trim().split('/')
//def pckg= me.length>1?me[1]:'.'
//currentBuild.displayName = pckg+currentBuild.displayName
/*if(pckg == 'ui-web'){
  def pipeui=load 'pipeline/ui-web.groovy'
  pipeui.testui()
}
else if(pckg == 'lambdas'){
  def pipeserv=load 'pipeline/ui-web.groovy'
  pipeserv.testservice()
}
*/


def name = sh ( script: 'git show --name-only origin/master' , returnStdout:true).trim().split('/')
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




node{
  checkout scm
  concurrency : 2
  print 'something'
  print 'something else'
  def me =sh (script:'git whatchanged -n 1 --pretty=format: --name-only',returnStdout:true).trim().split('/')
  def pckg= me.length>1?me[1]:'.'
  currentBuild.displayName = pckg+currentBuild.displayName
  sh 'git log --pretty=oneline --abbrev-commit --graph --decorate --all'
}

switch(pckg)
{
  case 'ui-web':
    package_path == '

@Library('PSL') _
def common_jenkins = new ors.utils.common_jenkins(steps, env)
def common_scm = new ors.utils.common_scm(steps, env)

pipeline {
    agent {
        label 'cloud&&centos'
    }

    stages {
        stage ('build') {
            steps {
                script {
                    env.WORKSPACE = pwd()
                    
                    // def package_path = 'packages/ui-web' // Lerna should let us know which files are changed
                    def package_path = 'packages/services'
                    def package_contents = readFile "$env.WORKSPACE/$package_path/package.json"

                    dir('groovy_scripts_folder') {
                        git url: 'https://git.autodesk.com/Account-Portal/pipelines.git', branch: 'ui-web', credentialsId: 'ors_git_service_account'
                    }

                    if(package_path == 'packages/ui-web'){
                        groovy_script = load 'groovy_scripts_folder/pipeline.groovy'
                        groovy_script.setup_env()
                        groovy_script.prep(common_jenkins, common_scm, package_path, package_contents)
                        groovy_script.unit_tests(package_path)
                    } else if(package_path == 'packages/services') {
                        groovy_script = load 'groovy_scripts_folder/pipeline-lambda.groovy'
                        groovy_script.setup_env(package_path)
                        groovy_script.prep()
                        groovy_script.unit_tests()
                        groovy_script.build()
                        groovy_script.deploy()
                        groovy_script.api_tests()
                        groovy_script.post_install()
                    }
                }
            }
        }
    }
}
