def setup(def build_env){
  println("will deploy to ${build_env}")
  def name = sh (script: 'git whatchanged -n 1 --pretty=format: --name-only',returnStdout:true).trim().split('/')
  for (i = 0; i < name.length; i++){
    println(name[i])
    if(name[i] == 'ui-web'){
      currentBuild.displayName = name[i]+currentBuild.displayName
      def pipeui=load 'Pipelines/ui-web.groovy'
        pipeui.set_up()
        pipeui.prep(common_jenkins, common_scm, package_path, package_contents)
        pipeui.unit_tests()
     } else if(name[i] == 'lambdas') {
        currentBuild.displayName = name[i]+currentBuild.displayName
        def pipeserv=load 'Pipelines/lambdas.groovy'
        pipeserv.setup_env(package_path)
        pipeserv.prep()
        pipeserv.unit_tests()
        pipeserv.build()
        
        }
     }
 }
def startup(def build_env){
  println("will deploy to ${build_env}")
  for (i = 0; i < name.length; i++){
  println(name[i])
  if(name[i] == 'ui-web'){
    def pipeui=load 'Pipelines/ui-web.groovy'
    pipeui.unit_tests(package_path)
  } else if(name[i] == 'lambdas') {
      def pipeserv=load 'Pipelines/lambdas.groovy'
      pipeserv.deploy("dev")
      pipeserv.api_tests()
      pipeserv.deploy_int("int")
      pipeserv.api_tests()
      pipeserv.deploy_stg("stg")
      pipeserv.api_tests()
      pipeserv.deploy_prod("prod")
     }
 }
 def tearDown(def build_env){
  println("will deploy to ${build_env}")
  for (i = 0; i < name.length; i++){
    println(name[i])
    if(name[i] == 'ui-web'){
      def pipeui=load 'Pipelines/ui-web.groovy'
      pipeui.fetch_release_candidate_tag(version, hash, build_id)
      pipeui.semver(increment, version)
      pipeui.get_release_tag(release_candidate_tag)
      pipeui.run_shell_script(command)
     } 
     else if(name[i] == 'lambdas') {
        def pipeserv=load 'Pipelines/lambdas.groovy'
        pipeserv.post_install ()
     }
   }
 }
