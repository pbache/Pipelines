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
	
	
