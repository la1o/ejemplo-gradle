def call(){
    def stageName = "Paso 1: Build && Test"
    stage("$stageName"){
        env.TAREA = "$stageName"
        sh "echo 'Build && Test!'"
        sh "gradle clean build"
        // code
    }

    stageName = "Paso 2: Sonar - Análisis Estático"
    stage("$stageName"){
        env.TAREA = "$stageName"
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube') {
            sh "echo 'Calling sonar by ID!'"
            // Run Maven on a Unix agent to execute Sonar.
            sh 'gradle sonarqube -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
        }
    }

    stageName = "Paso 3: Curl Springboot Gradle sleep 20"
    stage("$stageName"){
        env.TAREA = "$stageName"
        sh "timeout 30 \$(which gradle) bootRun&"
        //sh "gradle bootRun&"
        sleep 20
        sh "curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
        sleep 10
    }

    stageName = "Paso 4: Subir Nexus"
    stage("$stageName"){
        env.TAREA = "$stageName"
        nexusPublisher nexusInstanceId: 'nexus',
        nexusRepositoryId: 'devops-usach-nexus',
        packages: [
            [$class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '',
                    extension: 'jar',
                    filePath: 'build/libs/DevOpsUsach2020-0.0.1.jar'
                ]
            ],
                mavenCoordinate: [
                    artifactId: 'DevOpsUsach2020',
                    groupId: 'com.devopsusach2020',
                    packaging: 'jar',
                    version: '0.0.1'
                ]
            ]
        ]
    }

    stageName = "Paso 5: Descargar Nexus"
    stage("$stageName"){
        env.TAREA = "$stageName"
        sh 'ls -laht build/libs/DevOpsUsach2020-0.0.1.jar'
        sh 'md5sum build/libs/DevOpsUsach2020-0.0.1.jar'
        sh 'curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
        sh 'ls -laht DevOpsUsach2020-0.0.1.jar'
        sh 'md5sum DevOpsUsach2020-0.0.1.jar'
    }

    stageName = "Paso 6: Levantar Artefacto Jar"
    stage("$stageName"){
        env.TAREA = "$stageName"
        sh 'timeout 30 $(which nohup) java -jar DevOpsUsach2020-0.0.1.jar &'
        //sh 'nohup java -jar DevOpsUsach2020-0.0.1.jar &'
    }

    stageName = "Paso 7: Testear Artefacto - Dormir(Esperar 20sg)"
    stage("$stageName"){
        env.TAREA = "$stageName"
        sleep 20
        sh "curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
        sleep 10
    }
}

return this;