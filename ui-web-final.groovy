	def start()
	{
	
	}

	def Prep()
	 {
		stage('Prep'){
			   steps {
						script {
							sh "docker pull ${env.BUILD_CONTAINER_IMAGE}"
							sh "docker pull ${env.DEPLOY_CONTAINER_IMAGE}"
							echo """
							
		Defining additional environment variables used for branching logic in 
		subsequent stages of pipeline
							
							"""
							 docker.image("${env.BUILD_CONTAINER_IMAGE}").inside(){
								env.AUTHOR = sh(returnStdout: true, script: 'git log -n 1 --pretty=format:%an').trim()
								env.RELEASE_CANDIDATE_TAG = sh(returnStdout: true, script: "pipeline/release-candidate-tag ${common_scm.hash()}.${env.BUILD_ID}").trim()
								env.RELEASE_TAG = sh(returnStdout: true, script: "semver -i patch ${env.RELEASE_CANDIDATE_TAG}").trim()
								env.TRIGGER_TYPE = common_jenkins.get_trigger_type(currentBuild)
								env.TRIGGERED_BY =  ( env.AUTHOR == env.SERVICE_ACCOUNT_USR ) ? 'AUTOMATION' : \
													( env.TRIGGER_TYPE ==~ /Pull request .*/ ) ? 'PR' : \
													( env.TRIGGER_TYPE ==~ /Push .*/ ) ? 'PUSH' : \
													( env.TRIGGER_TYPE ==~ /Started by .*/ ) ? 'USER' : env.TRIGGER_TYPE
								/* For PRs env.BRANCH_NAME contains the PR number
								rather than an actual branch name; overwriting
								with the name of the base branch (env.CHANGE_TARGET)
								for clarity and consistency.
								*/
								if (env.TRIGGERED_BY == 'PR') {
									env.BRANCH_NAME = env.CHANGE_TARGET
								}

								try {
									/* Pull the publish registry from package.json as this determines
									where builds will be archived when "npm publish" is run. 
									This info is needed in order to pull (and deploy) an archived build.
									*/
									env.PROJECT_NAME = sh(returnStdout: true, script: 'node -pe "require(\\"./package.json\\").name"').trim()
									env.NPM_PUBLISH_REGISTRY = sh(returnStdout: true, script: 'node -pe "require(\\"./package.json\\").publishConfig.registry"').trim()
									env.NPM_PUBLISH_REPO_NAME = env.NPM_PUBLISH_REGISTRY.substring(env.NPM_PUBLISH_REGISTRY.lastIndexOf("/") + 1)
								} catch (err) {
									throw """
			Unable to find publish config in package.json. 
			This file must contain a publishConfig section indicating a registry where builds should be archived.
			See npm publishConfig for details.
									
									"""
								}
							}

							echo sh(returnStdout: true, script: 'env')

							if( env.TRIGGERED_BY != 'AUTOMATION' ){
								sh "pipeline/prep-workspace"
							}

							docker.image("${env.BUILD_CONTAINER_IMAGE}").inside(){
								sshagent(['svc_p_account_ssh_id']) {
									sh 'ssh -T -oStrictHostKeyChecking=no git@git.autodesk.com || true'
									// # As of 10/16/17, our private NPM registry (Artifactory) is not configured
									// to handle namespaced NPM packages correctly. Until this problem is fixed
									// we're pulling packages from the public NPM registry.

									// sh "yarn config set registry ${env.NPM_REGISTRY}${env.NPM_VIRTUAL_REPO}"

									echo 'Installing npm dependencies'
									sh "yarn"
								}
							}
						}
					}
				}
	}
       
	def unitTests()
	{
		   stage('Unit Tests') {
				when { expression {
					return  env.TRIGGERED_BY == 'PUSH' || env.TRIGGERED_BY == 'USER'
				} }
				steps {
					script {
						docker.image("${env.BUILD_CONTAINER_IMAGE}").inside(){
							wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
								echo 'Running tests'
								sh "yarn test --coverage"
							}
						}
					}
				}
			}
	}
	
	def  geminiTests()
	{
			stage('Gemini Tests') {
				when { expression {
					return  env.TRIGGERED_BY == 'PUSH' || env.TRIGGERED_BY == 'USER'
				} }
				steps {
					script {
						wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
							sh 'docker-compose up -d --force-recreate'
							echo 'Running Gemini Tests'
							sh '''
								docker-compose run \
								app \
								yarn gemini:docker:run
							'''
						}
					}
				}
			}
	}

	def buildPublish()
	{
			stage('Build & Publish') {
				when { expression { 
						return  env.TRIGGERED_BY == 'PUSH' || env.TRIGGERED_BY == 'USER' && env.BRANCH_NAME ==~ /(master)/
				}}
				steps {
					script {
						docker.image("${env.BUILD_CONTAINER_IMAGE}").inside(){
							echo "Building and publishing NPM package to ${env.NPM_PUBLISH_REGISTRY}"
							sh "yarn version --no-git-tag-version --new-version ${env.RELEASE_CANDIDATE_TAG}"
							sh "yarn build"
							sh "pipeline/publish"
						}
					}
				}
			}
	}

	def versionChangelogTag() 
	{
			stage('Version, changelog, tag') {
				when { expression { 
						return  (env.TRIGGERED_BY == 'PUSH' || env.TRIGGERED_BY == 'USER') && env.BRANCH_NAME ==~ /(master)/
				}}
				steps {
					script {
						/* UPDATE VERSION, CHANGELOG, COMMIT, TAG, PUSH */
						docker.image("${env.BUILD_CONTAINER_IMAGE}").inside() {
							sshagent(['svc_p_account_ssh_id']) {
								sh 'ssh -T -oStrictHostKeyChecking=no git@git.autodesk.com || true'
								env.BUILD_ARCHIVE_PATH = "${env.NPM_PUBLISH_REPO_NAME}/${env.PROJECT_NAME}/-/${env.PROJECT_NAME}-${env.RELEASE_CANDIDATE_TAG}"
								sh "pipeline/tag-release ${env.RELEASE_TAG} ${env.BUILD_ARCHIVE_PATH}"
								sh "git push origin-ssh ${env.BRANCH_NAME} && git push origin-ssh --tags"
							}
						}
					}
				}
			}
	}
	
	def deploy()
	{
			stage('Deploy') {
				when { expression {
						return  env.TRIGGERED_BY == 'PUSH' || env.TRIGGERED_BY == 'USER' && env.BRANCH_NAME ==~ /(master)/
				}}
				steps {
					script {
						/* DEPLOY BUILD */
						docker.image("${env.DEPLOY_CONTAINER_IMAGE}").inside("-v /tmp:/tmp") {
							sshagent(['svc_p_account_ssh_id']) { 
								sh 'ssh -T -oStrictHostKeyChecking=no git@git.autodesk.com || true'
								sh "bash /bin/aws_auth -u ${SERVICE_ACCOUNT_USR} -p ${SERVICE_ACCOUNT_PSW} -v ${VAULT_PATH} -a ${VAULT_ADDR}"
								sh "pipeline/deploy ${env.BRANCH_NAME} ${env.CDN}/${env.PROJECT_NAME}/${env.RELEASE_TAG}"
							}
						}
					}
				}
			}
	}

	def post() {
		post {
			always {
				sh 'docker-compose down || true'

				script {
					sh 'docker-compose down --rmi all || true'
					
					if (fileExists('./gemini-report')) {
						archiveArtifacts artifacts: 'gemini-report/**'
					}

					if (fileExists('./coverage')) {
						archiveArtifacts artifacts: 'coverage/**'
					}

					def success = currentBuild.result != 'FAILURE'
					def masterBranch = env.BRANCH_NAME ==~ /(master)/
					masterBranch && slackSend(message: "Push by ${env.AUTHOR} - Build ${success ? "Succeeded" : "Failed"}: ${env.BUILD_INFO}",
						teamDomain: 'autodesk', token: "${env.SLACK_TOKEN}", channel: "${env.TEAM_SLACK_CHANNEL}",
						color: "${success ? "good" : "danger"}")  
				}
			}
		}
	}

