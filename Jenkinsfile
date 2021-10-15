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
                    sh "./gradlew test"
               }
          }
	  stage("Code coverage") {
	       steps {
                    sh "./gradlew jacocoTestReport"
                    sh "./gradlew jacocoTestCoverageVerification"
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
     		container('gradle')
			{
        		stage('Build a gradle project')
				{
         	 		sh '''
         	 		chmod +x gradlew
                    ./gradlew build
                    mv ./build/libs/calculator-0.0.1-SNAPSHOT.jar /mnt
                    '''
				}
			}
		}
		stage('Build Java Image')
		{
			container('kaniko')
			{
        		stage('Build a kaniko project')
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
		}
	}
}
