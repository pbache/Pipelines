//package com.javacodegeeks.example.groovy.scripting
//class stages
//{	
	def start() {
	
		for (int i = 0; i < lambda_stages.length ; i++) {
				println i 
				//def value = i
				if (lambda_stages[i] == "prepare") {
					prepare()
				}
				
				else if (lambda_stages[i] == "build"){
				build()
				}
				
				else if (lambda_stages[i] == "deployDev")
				{
				deployDev()
				}
				
				else if (lambda_stages[i] == "deployCompose")
				{
				deployCompose()
				}
				else if (lambda_stages[i] == "deployINT")
				{
				deployINT()
				}
				
				else if (lambda_stages[i] == "deploySTG")
				{
				deploySTG()
				}
				else if (lambda_stages[i] == "deployPRD")
				{
				deployPRD()
				}
				
				else println "stage is not available"
				
			}
	
	}
	
	def prepare() {
		stage('Prepare'){
				steps {
					sh "docker pull ${DEPLOY_TOOLS_IMAGE}"
					sh "docker pull ${BUILD_IMAGE}"
					script {
						branch = "${env.BRANCH_NAME}"
						switch (branch) {
							case 'master':
								sh 'echo master'
								isMasterBranch = true
								break
							default:
								sh 'echo default'
						}
					}
				}
		}		
	}
		
		
	def build() {
		stage('Build') {
				agent {
					docker {
						image "$BUILD_IMAGE"
						reuseNode true
						args '-u root'
					}
				}
				steps {
					sh 'yarn --quiet'
					sh 'chmod 0777 ./node_modules/.bin/mocha'
					sh 'yarn coverage'
					// Need to set file permissions since we performed yarn install as root
					sh 'find . -type d -print0 | xargs -0 chmod 0755'
					sh 'find . -type f -print0 | xargs -0 chmod 0644'
				}
			}
	}		
		
	def deployDev(){
		
			stage('Deploy Lambda (DEV)') {
				agent {
					docker {
						image "${DEPLOY_TOOLS_IMAGE}"
						reuseNode true
					}
				}
				environment {
					VAULT_PATH = 'spg/feynman-dev/aws/adsk-eis-feynman-dev/sts/admin'
					STAGE = 'dev'
				}
				steps {
					script {
						if (isMasterBranch) {
							sh 'bash /bin/aws_auth'
							sh 'serverless create_domain || exit 0'
							sh 'serverless package --package ./.serverless-dev'
							sh 'serverless deploy --package .serverless-dev'
						}
					}
				}
			}
	}
	
	def deployCompose(){
		
		stage('Deploy Lambda (COMPOSE)') {
				agent {
					docker {
						image "${DEPLOY_TOOLS_IMAGE}"
						reuseNode true
					}
				}
				environment {
					VAULT_PATH = 'spg/feynman-dev/aws/adsk-eis-feynman-dev/sts/admin'
					STAGE = 'compose'
				}
				steps {
					script {
						if (isMasterBranch) {
							sh 'bash /bin/aws_auth'
							sh 'serverless create_domain --stage compose || exit 0'
							sh 'serverless package --stage compose --package ./.serverless-compose'
							sh 'serverless deploy --stage compose --package .serverless-compose'
						}
						currentBuild.result = SUCCESS // TODO remove this when commenting out other stages
					}
				}
			}
	}
//      Uncomment to deploy to different environments
	def deployINT(){
			stage('Deploy Lambda (INT)') {
				agent {
					docker {
						image "${DEPLOY_TOOLS_IMAGE}"
						reuseNode true
					}
				}
				environment {
					VAULT_PATH = 'spg/feynman-int/aws/adsk-eis-feynman-int/sts/admin'
					STAGE = 'int'
				}
				steps {
					script {
						if (isMasterBranch) {
							sh 'bash /bin/aws_auth'
							sh 'serverless package'
							sh 'serverless deploy --package ./.serverless'
						}
					}
				}
			}
	}
		
	def deploySTG(){
			stage('Deploy Lambda (STG)') {
				agent {
					docker {
						image "${DEPLOY_TOOLS_IMAGE}"
						reuseNode true
					}
				}
				environment {
					VAULT_PATH = 'spg/feynman-stg/aws/adsk-eis-feynman-stg/sts/admin'
					STAGE = 'stg'
				}
				steps {
					script {
						if (isMasterBranch) {
							sh 'bash /bin/aws_auth'
							sh 'serverless package'
							sh 'serverless deploy --package ./.serverless'
						}
					}
				}
			}
	}
		
	def deployPRD(){
			stage('Deploy Lambda (PRD)') {
				agent {
					docker {
						image "${DEPLOY_TOOLS_IMAGE}"
						reuseNode true
					}
				}
				environment {
					VAULT_PATH = 'spg/feynman-prd/aws/adsk-eis-feynman-prd/sts/admin'
					STAGE = 'prd'
				}
				steps {
					script {
						if (isMasterBranch) {
							sh 'bash /bin/aws_auth'
							sh 'serverless package'
							sh 'serverless deploy --package ./.serverless'
						}
						currentBuild.result = SUCCESS
					}
				}
			}
	}
		
	def post(){
			post {
			always {
				script {
					if (fileExists('./.serverless-dev')) {
						archiveArtifacts artifacts: '.serverless-dev/**'
					}

					if (fileExists('./.serverless-compose')) {
						archiveArtifacts artifacts: '.serverless-compose/**'
					}

					sh 'rm -f ~/.aws/credentials'
					sh "docker rmi --force '${DEPLOY_TOOLS_IMAGE}' >/dev/null 2>&1 || true"
					sh "docker rmi --force '${BUILD_IMAGE}' >/dev/null 2>&1 || true"

					buildSucceeded = currentBuild.result == SUCCESS

					slackSend(message: "Build ${buildSucceeded ? "Succeeded" : "Failed"}: ${BUILD_INFO_LAMBDA}",
							teamDomain: 'autodesk', token: "${SLACK_TOKEN}", channel: "${TEAM_SLACK_CHANNEL}",
							color: "${buildSucceeded ? "good" : "danger"}")
				}
			}
	}
//*/ 
//}
	
 