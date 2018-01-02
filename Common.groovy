//def username = System.console().readLine 'are you ok to deploy in to production?'
//println "Hello $username"

def setUp(){
	 stage('setUp'){
	 def me =sh (script:'git diff HEAD^ HEAD --name-only',returnStdout:true).trim().split('/')
	 pckg= me.length>1?me[1]:'full'
	 
	 currentBuild.displayName = pckg+currentBuild.displayName
	 }
	}
def start(){
	 def lambda_pipeline=load './pipelines/lambda.groovy'
	 def uiweb_pipeline=load './pipelines/ui-web.groovy'
	 if(pckg=='full'){
	   print 'full'
	   lambda_pipeline.start()
	   uiweb_pipeline.start()
	 }
	 else if(pckg=='lambda')
	 {
	   lambda_pipeline.start()
	 }
	 else if(pckg=='ui-web')
	 {
	   uiweb_pipeline.start()
	 }
	}
def tearDown(){
	 stage('tearDown'){
	 print 'this is tearDown'
	 }
	}
	return this
	

