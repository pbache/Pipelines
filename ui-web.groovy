/*def prep_common(def build_env){
  println("will deploy to ${build_env}")
  def name = sh (script: 'git whatchanged -n 1 --pretty=format: --name-only',returnStdout:true).trim().split('/')
  for (i = 0; i < name.length; i++){
     println(name[i])
   if(name[i] == 'ui-web'){
      currentBuild.displayName = name[i]+currentBuild.displayName
      def pipeui=load 'Pipelines/ui-web.groovy'
      pipeui.setup_env()
      pipeui.prep(common_jenkins, common_scm, package_path, package_contents)
      pipeui.unit_tests(package_path)
  } else if(name[i] == 'lambdas') {
      currentBuild.displayName = name[i]+currentBuild.displayName
      def pipeserv=load 'Pipelines/lambdas.groovy'
      pipeui.setup_env(package_path)
      pipeui.prep()
      pipeui.build()
      if($build_env == 'ALL'){
         pipeui.deploy("stg")
        pipeui.unit_tests()
         pipeui.deploy("dev")
        pipeui.unit_tests()
         pipeui.deploy("prod")
      } else {
        pipeui.deploy("${build_env}")
      }
     
      pipeui.api_tests()
      pipeui.post_install()
 }
}
return this
*/
setup() 

{



}
def start() {

	if (service == ui)
	{
	@Library('PSL') _
	def common_jenkins = new ors.utils.common_jenkins(steps, env)
	def common_scm = new ors.utils.common_scm(steps, env)

	 //   environment {
			/*
			These variables should change according to team/project
			*/
			def TEAM_SLACK_CHANNEL="#spg-feynman-ci"
			def TEAM_EMAIL="m2s6m0x4t8p6v0q0@autodesk.slack.com"
			def SERVICE_ACCOUNT=credentials('svc_p_account_id')
			def VAULT_PATH = 'spg/feynman-dev/aws/adsk-eis-feynman-dev/sts/admin'
			def CDN="s3://cdn-dev-manage.autodesk.com"
			/*
			These shouldn't change unless you have your own build container or NPM registry
			*/
			def NPM_REGISTRY="https://art-bobcat.autodesk.com/artifactory/"
			def NPM_AUTH="api/npm/auth"
			def NPM_VIRTUAL_REPO="api/npm/autodesk-npm-virtual/"
			def BUILD_CONTAINER_IMAGE="autodesk-docker.art-bobcat.autodesk.com:10873/team-account/build-npm:latest"
			def DEPLOY_CONTAINER_IMAGE="autodesk-docker.art-bobcat.autodesk.com:10873/team-spg/bedrock-build-terraform:latest"
			def VAULT_ADDR = 'https://vault.aws.autodesk.com'
			def BUILD_INFO="${env.JOB_NAME} ${env.BUILD_NUMBER} \n ${env.BUILD_URL}";
			def SLACK_TOKEN=credentials('ors_slack_token')
			def CI=true
	  //  }
	}
//lambda	
	
	if (service == lambda){	
	//def WORKER = 'cloud&&centos'

	// Build Configuration
	def BUILD_IMAGE = 'node:8'
	def BUILD_INFO_LAMBDA = "${env.JOB_NAME}-${env.BUILD_NUMBER}\n${env.BUILD_URL}"

	// Deploy Configuration
	def DEPLOY_TOOLS_IMAGE = 'autodesk-docker.art-bobcat.autodesk.com:10873/team-spg/bedrock-build-terraform:latest'

	// Notifications
	def TEAM_SLACK_CHANNEL = '#spg-feynman-ci'

	// Build Status
	def SUCCESS = 'SUCCESS'
	def isMasterBranch = false

	}

	pipeline {
		agent {
			label 'cloud&&centos'
		}
	 }
 
 }
