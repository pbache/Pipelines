@Library('PSL') _
def common_jenkins = new ors.utils.common_jenkins(steps, env)
def common_scm = new ors.utils.common_scm(steps, env)

def comm_test(){
  def name = sh (script: 'git whatchanged -n 1 --pretty=format: --name-only',returnStdout:true).trim().split('/')
  for (i = 0; i < name.length; i++){
    println(name[i])
    	if(name[i] == 'lambda'){
		currentBuild.displayName = name[i]+currentBuild.displayName
      		def pipeui=load 'Pipelines/ui-web.groovy'
		setup(name[i],pipeserv)
	 	startup(name[i],pipeserv)
	 	tear-down(name[i],pipeserv)
	} 
	if(name[i] == ui-web){
      		currentBuild.displayName = name[i]+currentBuild.displayName
      		def pipeserv=load 'Pipelines/lambdas.groovy'
	  	setup(name[i],pipeui)
	  	startup(name[i],pipeui)
	  	tear-down(name[i],pipeui)
	}
  }

}

def setup(string package_path, def grovy_obj){
	if(package_path = lambda){
       		grovy_obj.setup_env(package_path)
    		grovy_obj.prep()
		grovy_obj.unit_tests()
		grovy_obj.build()
	}
	if(package_path = ui-web){
        	grovy_obj.set_up()
    		grovy_obj.prep(common_jenkins, common_scm, package_path, package_contents)
		grovy_obj.unit_tests()
	
	}
}

def startup(string package_path, def grovy_obj){
 	if(package_path = lambda){
	 	grovy_obj.deploy("dev")
	  	grovy_obj.api_tests()
		grovy_obj.deploy_int("int")
	  	grovy_obj.api_tests()
		grovy_obj.deploy_stg("stg")
		grovy_obj.api_tests()
		grovy_obj.deploy_prod("prod")
  	}
 	if(package_path = ui-web){
       		grovy_obj.unit_tests(package_path)
	
	}

}

def tear-down(string package_path, def grovy_obj){
  	if(package_path = lambda){
        	grovy_obj.post_install()
	}
	if(package_path = ui-web){
        	grovy_obj.fetch_release_candidate_tag(version, hash, build_id)
		grovy_obj.semver(increment, version)
		grovy_obj.get_release_tag(release_candidate_tag)
		grovy_obj.run_shell_script(command)
	}
}



-----------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------

/* Another appoarch

def common_prep(){
 for(name){
    if(name == lambda){
      def grovy_obj = load 'lambda.groovy'
	  setup-lambda(grovy_obj)
	  startup-lambda(grovy_obj)
	  tear-down-lambda(grovy_obj)
	}
	if(name == ui-web){
      def grovy_obj = load 'ui-web.groovy'
	  setup-ui-web(name,grovy_obj)
	  startup-ui-web(name,grovy_obj)
	  tear-down-ui-web(name,grovy_obj)
	}
	
 }

}




def setup-lamda(grovy_obj){
       grovy_obj.setupenv
    	grovy_obj.prep()
		grovy_obj.unittest()
		grovy_obj.build()
}


def startup-lambda(grovy_obj){
	  grovy_obj.deploy_dev()
	  grovy_obj.deploy_int()

}

def tear-down-lambda(grovy_obj){
        grovy_obj.post_inst()
	
}




def setup-ui-web(grovy_obj){
       grovy_obj.setupenv
    	grovy_obj.prep(comm,commonscn,package_path)
		grovy_obj.unittest()
		
}


def startup-ui-web(grovy_obj){
	  grovy_obj.deploy_dev()
	  grovy_obj.deploy_int()

}

def tear-down-ui-web(grovy_obj){
         grovy_obj.releasetag()
		grovy_obj.releasecandidatetag()
		......
		......
	
}
*/
