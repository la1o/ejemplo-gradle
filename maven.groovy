def call(){
    def STAGE_NAME = "Paso 1: Compilar"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"
        sh "mvn clean compile -e"
    }

    STAGE_NAME = "Paso 2: Testear"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"

        sh "mvn clean test -e"
    }

    STAGE_NAME = "Paso 3: Build .Jar"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"
        sh "mvn clean package -e"
    }

    STAGE_NAME = "Paso 4: Sonar - Análisis Estático"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube') {
            sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
        }
    }

    STAGE_NAME = "Paso 5: Curl Springboot Gradle sleep 20"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"
        sh "timeout 30 \$(which mvn) spring-boot:run &"
        sleep 20
        sh "curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
        sleep 10
    }

    STAGE_NAME = "Paso 6: Subir Nexus"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"
        nexusPublisher nexusInstanceId: 'nexus',
        nexusRepositoryId: 'devops-usach-nexus',
        packages: [
            [$class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '',
                    extension: 'jar',
                    filePath: 'build/DevOpsUsach2020-0.0.1.jar'
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

    STAGE_NAME = "Paso 7: Descargar Nexus"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"
        sh 'curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
    }

    STAGE_NAME = "Paso 8: Levantar Artefacto Jar"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"
        sh 'timeout 30 \$(nohup) java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
    }

    STAGE_NAME = "Paso 9: Testear Artefacto - Dormir(Esperar 20sg)"
    stage("$STAGE_NAME"){
        env.TAREA = "$STAGE_NAME"
        sleep 20
        sh "curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
        sleep 10
    }
}

return this;