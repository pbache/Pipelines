	def start()
	{
	
	for (int i = 0; i < ui_stages.length ; i++) {
        println i 
        //def value = i
        if (ui_stages[i] == "Prep") {
            Prep()
        }
        
        else if (ui_stages[i] == "unitTests"){
        unitTests()
        }
        
        else if (ui_stages[i] == "geminiTests")
        {
        geminiTests()
        }
        
        else if (ui_stages[i] == "buildPublish")
        {
        buildPublish()
        }
        else if (ui_stages[i] == "versionChangelogTag")
        {
        versionChangelogTag()
        }
        
        else if (ui_stages[i] == "deploy")
        {
        deploySTG()
        }
        else if (ui_stages[i] == "post")
        {
        post()
        }
        
        else println "stage is not available"
        
    }
	
	}

	
