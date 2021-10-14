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
pipeline {

     agent any

     triggers {

          pollSCM('* * * * *')

     }

     stages {

	stage("Compile") {

               steps {

                    sh "chmod +x gradlew"

		   sh "./gradlew compileJava"

               }

          }

          stage("Unit test") {

               steps {

                       sh "chmod +x gradlew"

		       sh "./gradlew test"

               }

          }

          stage("Code coverage") {

               steps {

                    sh "chmod +x gradlew"

		    sh "./gradlew jacocoTestReport"

                    sh "./gradlew jacocoTestCoverageVerification"

               }

          }

	     stage("Static code analysis") {

		 steps {

                    	sh "chmod +x gradlew"

			 sh "./gradlew checkstyleMain"

               }

          }

	     stage("gradle") 
          {	              
		  steps 
                   {

                    sh "chmod +x gradlew"

		     sh "./gradlew build"
                    sh "mv ./build/libs/calculator-0.0.1-SNAPSHOT.jar /mnt "

                   }

                
             
           }

          stage("Docker build") {

               steps {

                    sh "docker build -t vanithamreddy/calculator:1.0"

               }

          }


          stage("Docker login") {

               steps {

                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'docker-hub-credentials',

                               usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {

                         sh "docker login --username vanithamreddy --password CrazyPilla@1"

                    }

               }

          }




          stage("Docker push") {

               steps {

                    sh "docker push vanithamreddy/calculator:1.0"

               }

          }




          stage("Update version") {

               steps {

                    sh "sed  -i 's/{{VERSION}}/${BUILD_TIMESTAMP}/g' calculator.yaml"

               }

          }

          stage("Build Java Image") 
		{
        		steps
				{
          			sh '''
					echo 'FROM openjdk:8-jre' > Dockerfile
					echo 'COPY ./calculator-0.0.1-SNAPSHOT.jar app.jar' >> Dockerfile
					echo 'ENTRYPOINT ["java", "-jar", "app.jar"]' >> Dockerfile
					mv /mnt/calculator-0.0.1-SNAPSHOT.jar .
					/kaniko/executor --context `pwd` --destination vanithamreddy/calculator:1.0 					
					'''
				}
		}


          

          stage("Deploy to staging") {

	      steps {

                    sh "kubectl config use-context staging"

                    sh "kubectl apply -f hazelcast.yaml"

                    sh "kubectl apply -f calculator.yaml"

               }

          }




          stage("Acceptance test") {

	      steps {

                    sleep 60

                    sh "chmod +x acceptance-test.sh && ./acceptance-test.sh"

               }

          }




          stage("Release") {

         steps {

                    sh "kubectl config use-context production"

                    sh "kubectl apply -f hazelcast.yaml"

                    sh "kubectl apply -f calculator.yaml"

               }

          }

          stage("Smoke test") {

	     steps {

                  sleep 60

                  sh "chmod +x smoke-test.sh && ./smoke-test.sh"

              }

          }

     }

}


