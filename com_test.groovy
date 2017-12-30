
def comm_test(){
  def name = sh (script: 'git whatchanged -n 1 --pretty=format: --name-only',returnStdout:true).trim().split('/')
  for (i = 0; i < name.length; i++){
     println(name[i])
    if(name[i] == 'lambda')
      def grovy_obj = load 'lambda.groovy'
	  setup(name[i],grovy_obj)
	  startup(name[i],grovy_obj)
	  tear-down(name[i],grovy_obj)
	}
	if(name == ui-web){
      def grovy_obj = load 'ui-web.groovy'
	  setup(name[i],grovy_obj)
	  startup(name[i],grovy_obj)
	  tear-down(name[i],grovy_obj)
	}
	
 }

}




def setup(string package_path,def grovy_obj){
	if(package_path = lambda){
       grovy_obj.setupenv
    	grovy_obj.prep()
		grovy_obj.unittest()
		grovy_obj.build()
	
	}
	if(package_path = ui-web){
        grovy_obj.setupenv
    	grovy_obj.prep(comm,commonscn,package_path)
		grovy_obj.unittest()
	
	}
}

def startup(string package_path,def grovy_obj){
  if(package_path = lambda){
	  grovy_obj.deploy_dev()
	  grovy_obj.deploy_int()
  }
  if(package_path = ui-web){
       grovy_obj.deploy_dev()
	  grovy_obj.deploy_int()
	
	}

}

def tear-down(string package_path,def grovy_obj){
  if(package_path = lambda){
        grovy_obj.post_inst()
	
	}
	if(package_path = ui-web){
        grovy_obj.releasetag()
		grovy_obj.releasecandidatetag()
		......
		......
	
	}
}



-----------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------



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
