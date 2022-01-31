def call(){
    def STAGE_NAME = "Paso 1: Build && Test"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"
        sh "echo 'Build && Test!'"
        sh "gradle clean build"
        // code
    }

    STAGE_NAME = "Paso 2: Sonar - Análisis Estático"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube') {
            sh "echo 'Calling sonar by ID!'"
            // Run Maven on a Unix agent to execute Sonar.
            sh 'gradle sonarqube -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
        }
    }

    STAGE_NAME = "Paso 3: Curl Springboot Gradle sleep 20"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"
        sh "timeout 30 \$(which gradle) bootRun&"
        //sh "gradle bootRun&"
        sleep 20
        sh "curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
        sleep 10
    }

    STAGE_NAME = "Paso 4: Subir Nexus"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"
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

    STAGE_NAME = "Paso 5: Descargar Nexus"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"
        sh 'ls -laht build/libs/DevOpsUsach2020-0.0.1.jar'
        sh 'md5sum build/libs/DevOpsUsach2020-0.0.1.jar'
        sh 'curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
        sh 'ls -laht DevOpsUsach2020-0.0.1.jar'
        sh 'md5sum DevOpsUsach2020-0.0.1.jar'
    }

    STAGE_NAME = "Paso 6: Levantar Artefacto Jar"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"
        sh 'timeout 30 $(which nohup) java -jar DevOpsUsach2020-0.0.1.jar &'
        //sh 'nohup java -jar DevOpsUsach2020-0.0.1.jar &'
    }

    STAGE_NAME = "Paso 7: Testear Artefacto - Dormir(Esperar 20sg)"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"
        sleep 20
        sh "curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
        sleep 10
    }
}

return this;