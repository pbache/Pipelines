def setup_env(package_path) {
    stage('Env') {
        env.WORKER = 'cloud&&centos'

        env.PACKAGE_PATH = package_path

        // Build Configuration
        env.BUILD_IMAGE = 'node:8'
        env.BUILD_INFO = "${env.JOB_NAME}-${env.BUILD_NUMBER}\n${env.BUILD_URL}"

        // Deploy Configuration
        env.DEPLOY_TOOLS_IMAGE = 'autodesk-docker.art-bobcat.autodesk.com:10873/team-spg/bedrock-build-terraform:latest'

        // Notifications
        env.TEAM_SLACK_CHANNEL = '#spg-feynman-ci'

        // Build Status
        env.SUCCESS = 'SUCCESS'
        env.isMasterBranch = false

        env.LDAP = credentials('svc_p_account_id')
        env.SLACK_TOKEN = credentials('ors_slack_token')
        env.TG_VAR_BUCKET = 'tf-user-profile-lambda'
        env.VAULT_ADDR = 'https://vault.aws.autodesk.com'
        env.VAULT_PATH = 'spg/feynman-dev/aws/adsk-eis-feynman-dev/sts/admin'
    }
}

def prep() {
    stage('Prepare') {
        def branch = "${env.BRANCH_NAME}"

        sh "docker pull ${env.DEPLOY_TOOLS_IMAGE}"
        sh "docker pull ${env.BUILD_IMAGE}"

        switch (branch) {
            case 'master':
                sh 'echo master'
                env.isMasterBranch = true
                break
            default:
                sh 'echo default'
        }
    }
}


def unit_tests() {
    stage('Unit Test') {
        docker.image("${env.BUILD_IMAGE}").inside('-u root'){
            sh "cd ${env.PACKAGE_PATH} && yarn --quiet"
            sh "cd ${env.PACKAGE_PATH} && chmod 0777 ./node_modules/.bin/mocha"
            sh "cd ${env.PACKAGE_PATH} && chmod 0777 ./node_modules/.bin/eslint"
            sh "cd ${env.PACKAGE_PATH} && yarn test"
        }
    }
}

def build() {
    stage('Build') {
        docker.image("${env.BUILD_IMAGE}").inside('-u root'){
            // Need to set file permissions to run on lambda
            sh "cd ${env.PACKAGE_PATH} && find . -type d -print0 | xargs -0 chmod 0755"
            sh "cd ${env.PACKAGE_PATH} && find . -type f -print0 | xargs -0 chmod 0644"
        }
    }
}

def deploy(def dep_env) {
    stage('Deploy Lambda ("${dep_env}")') {

        def STAGE = "${dep_env}"
        def env_var = "-e VAULT_PATH='${VAULT_PATH}' -e STAGE='${STAGE}' -e LDAP_USR='sainatjadmin' -e LDAP_PSW='Good4now' -e VAULT_ADDR='${VAULT_ADDR}'"
            docker.image("${env.DEPLOY_TOOLS_IMAGE}").inside(env_var){
            if ("${env.isMasterBranch}") {
                sh "bash /bin/aws_auth"
                sh "cd packages/services && serverless create_domain || exit 0"
                sh "cd packages/services && serverless package --package ./.serverless-dev"
                sh "cd packages/services && serverless deploy --package .serverless-dev"
            }
        }
    }
}

def api_tests() {    
    stage('API Tests') {
        build job: '../feynman-selenium-ci/master', 
            parameters: [[$class: 'StringParameterValue', name: 'TestSuite', value: 'user_profile']]
    }
}
def post_install () {
    if (fileExists('./.serverless-dev')) {
        archiveArtifacts artifacts: '.serverless-dev/**'
    }

    if (fileExists('./.serverless-compose')) {
        archiveArtifacts artifacts: '.serverless-compose/**'
    }

    sh 'rm -f ~/.aws/credentials'
    sh "docker rmi --force '${env.DEPLOY_TOOLS_IMAGE}' >/dev/null 2>&1 || true"
    sh "docker rmi --force '${env.BUILD_IMAGE}' >/dev/null 2>&1 || true"

    buildSucceeded = currentBuild.result == "${env.SUCCESS}"

    // slackSend(message: "Build ${buildSucceeded ? "Succeeded" : "Failed"}: ${env.BUILD_INFO}",
    //         teamDomain: 'autodesk', token: "${env.SLACK_TOKEN}", channel: "${env.TEAM_SLACK_CHANNEL}",
    //         color: "${buildSucceeded ? "good" : "danger"}")
}

return this
