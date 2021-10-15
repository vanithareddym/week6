    pipeline {
  	     agent any
  	     triggers {
  	          pollSCM('H/40 * * * *')
  	     }
  	     stages {
  		stage("Compile") {
  			when {
  	         branch "feat-*"
  	        }
  	               steps {
  	                    sh "chmod +x gradlew"
  			   sh "./gradlew compileJava"
  	               }
  	          }
  	          stage("Unit test") {
  			when {
  	         branch "feat-*"
  	        }
  	               steps {
  	                    sh "./gradlew test"
  	               }
  	          }
  		    stage("Code coverage") {
               		when{
  		branch "mai-*"
  		}
  	               steps {
  	                    sh "chmod +x gradlew"
  			    sh "./gradlew jacocoTestReport"
  	                    sh "./gradlew jacocoTestCoverageVerification"
  	               }
  	          }
  	          stage("Static code analysis") {
  	              when {
  	         branch "feat-*"
  	        }
  			 steps {
  	                    sh "./gradlew checkstyleMain"
  	               }
  	          }
  	          stage("Package") {
  	        	when {
  	         branch "feat-*"
  	        }
  		      steps {
  	                    sh "./gradlew build"
  	               }
  	          }


  	          stage("Docker build") {
  			when {
  	         branch "feat-*"
  	        }
  	               steps {
  	                    sh "docker build -t leszko/calculator:${BUILD_TIMESTAMP} ."
  	               }
  	          }


  	          stage("Docker login") {
  			when {
  	         branch "feat-*"
  	        }
  	               steps {
  	                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'docker-hub-credentials',
  	                               usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
  	                         sh "docker login --username $USERNAME --password $PASSWORD"
  	                    }
  	               }
  	          }


  	          stage("Docker push") {
  			when {
  	         branch "feat-*"
  	        }
  	               steps {
  	                    sh "docker push leszko/calculator:${BUILD_TIMESTAMP}"
  	               }
  	          }


  	          stage("Update version") {
  			when {
  	         branch "feat-*"
  	        }
  	               steps {
  	                    sh "sed  -i 's/{{VERSION}}/${BUILD_TIMESTAMP}/g' calculator.yaml"
  	               }
  	          }

  	          stage("Deploy to staging") {
  	        	when {
  	         branch "feat-*"
  	        }
  		      steps {
  	                    sh "kubectl config use-context staging"
  	                    sh "kubectl apply -f hazelcast.yaml"
  	                    sh "kubectl apply -f calculator.yaml"
  	               }
  	          }


  	          stage("Acceptance test") {

  			when {
  	         branch "feat-*"
  	        }
  		      steps {
  	                    sleep 60
  	                    sh "chmod +x acceptance-test.sh && ./acceptance-test.sh"
  	               }
  	          }


  	          stage("Release") {
  	        	when {
  	         branch "feat-*"
  	        }
  		      steps {
  	                    sh "kubectl config use-context production"
  	                    sh "kubectl apply -f hazelcast.yaml"
  	                    sh "kubectl apply -f calculator.yaml"
  	               }
  	          }
  	          stage("Smoke test") {
  	        	when {
  	         branch "feat-*"
  	        }
  		     steps {
  	                  sleep 60
  	                  sh "chmod +x smoke-test.sh && ./smoke-test.sh"
  	              }
  	          }
  	     }
  	}
 podTemplate(yaml: '''
     apiVersion: v1
     kind: Pod
     spec:
       containers:
       - name: gradle
         image: gradle:6.3-jdk14
         command:
         - sleep
         args:
         - 99d
         volumeMounts:
         - name: shared-storage
           mountPath: /mnt
       - name: kaniko
         image: gcr.io/kaniko-project/executor:debug
         command:
         - sleep
         args:
         - 9999999
         volumeMounts:
         - name: shared-storage
           mountPath: /mnt
         - name: kaniko-secret
           mountPath: /kaniko/.docker
       restartPolicy: Never
       volumes:
       - name: shared-storage
         persistentVolumeClaim:
           claimName: jenkins-pv-claim
       - name: kaniko-secret
         secret:
             secretName: dockercred
             items:
             - key: .dockerconfigjson
               path: config.json
 ''')
{
  node(POD_LABEL)
	{
    		stage('Build a gradle project')
		{

     		git url: "https://github.com/vanithareddym/week6" , branch: "playground"
     		if(branch="playground")
     		{
     		echo " No container will be created for playground branch"
     		}
     		else
     		{
     		container('gradle')
			{
        		stage('Build container')
				{
         	 		sh '''
                    chmod +x gradlew
                    ./gradlew build
                    mv ./build/libs/calculator-0.0.1-SNAPSHOT.jar /mnt
                    '''
				}
			}
			}
		}
		stage('Build Image')
		{
			container('kaniko')
			{
        		stage('Build a calculator program')
				{
          			sh '''
					echo 'FROM openjdk:8-jre' > Dockerfile
					echo 'COPY ./calculator-0.0.1-SNAPSHOT.jar app.jar' >> Dockerfile
					echo 'ENTRYPOINT ["java", "-jar", "app.jar"]' >> Dockerfile
					mv /mnt/calculator-0.0.1-SNAPSHOT.jar .
					/kaniko/executor --context `pwd` --destination vanithamreddy/calculator-playground:2.0
					'''
				}
			}
		}
	}
}
