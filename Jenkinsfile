pipeline {
     agent any
     triggers {
          pollSCM('* * * * *')
     }
     stages {
          stage("Compile") {
               when {
                branch "feature-*"
              } 
		steps {
                    sh "chmod +x gradlew"
		   sh "./gradlew compileJava"
               }
          }
          stage("Unit test") {
               when {
                branch "feature-*"
              } 
		steps {
                    sh "./gradlew test"
               }
          }
          stage("Code coverage") {
               when {
		branch "main"
	      }
	       steps {
                    sh "./gradlew jacocoTestReport"
                    sh "./gradlew jacocoTestCoverageVerification"
               }
          }
          stage("Static code analysis") {
               when {
                branch "feature-*"
              } 
		steps {
                    sh "./gradlew checkstyleMain"
               }
          }
          stage("Package") {
               when {
                branch "feature-*"
              } 
		steps {
                    sh "./gradlew build"
               }
          }

          stage("Docker build") {
               when {
                branch "feature-*"
              } 
		steps {
                    sh "docker build -t leszko/calculator:${BUILD_TIMESTAMP} ."
               }
          }

          stage("Docker login") {
               when {
                branch "feature-*"
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
                branch "feature-*"
              } 
		steps {
                    sh "docker push leszko/calculator:${BUILD_TIMESTAMP}"
               }
          }

          stage("Update version") {
               when {
                branch "feature-*"
              } 
		steps {
                    sh "sed  -i 's/{{VERSION}}/${BUILD_TIMESTAMP}/g' calculator.yaml"
               }
          }
          
          stage("Deploy to staging") {
               when {
                branch "feature-*"
              } 
		steps {
                    sh "kubectl config use-context staging"
                    sh "kubectl apply -f hazelcast.yaml"
                    sh "kubectl apply -f calculator.yaml"
               }
          }

          stage("Acceptance test") {
               when {
                branch "feature-*"
              } 
		steps {
                    sleep 60
                    sh "chmod +x acceptance-test.sh && ./acceptance-test.sh"
               }
          }

          stage("Release") {
               when {
                branch "feature-*"
              } 
		steps {
                    sh "kubectl config use-context production"
                    sh "kubectl apply -f hazelcast.yaml"
                    sh "kubectl apply -f calculator.yaml"
               }
          }
          stage("Smoke test") {
              when {
                branch "feature-*"
              } 
		steps {
                  sleep 60
                  sh "chmod +x smoke-test.sh && ./smoke-test.sh"
              }
          }
     }
}

