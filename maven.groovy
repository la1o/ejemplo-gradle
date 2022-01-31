def call(){
    def stageName = "Paso 1: Compilar"
    stage("$stageName"){
        env.TAREA = "$stageName"
        sh "mvn clean compile -e"
    }

    stageName = "Paso 2: Testear"
    stage("$stageName"){
        env.TAREA = "$stageName"

        sh "mvn clean test -e"
    }

    stageName = "Paso 3: Build .Jar"
    stage("$stageName"){
        env.TAREA = "$stageName"
        sh "mvn clean package -e"
    }

    stageName = "Paso 4: Sonar - Análisis Estático"
    stage("$stageName"){
        env.TAREA = "$stageName"
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube') {
            sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
        }
    }

    stageName = "Paso 5: Curl Springboot Gradle sleep 20"
    stage("$stageName"){
        env.TAREA = "$stageName"
        sh "timeout 30 \$(which mvn) spring-boot:run &"
        sleep 20
        sh "curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
        sleep 10
    }

    stageName = "Paso 6: Subir Nexus"
    stage("$stageName"){
        env.TAREA = "$stageName"
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

    stageName = "Paso 7: Descargar Nexus"
    stage("$stageName"){
        env.TAREA = "$stageName"
        sh 'curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
    }

    stageName = "Paso 8: Levantar Artefacto Jar"
    stage("$stageName"){
        env.TAREA = "$stageName"
        sh 'timeout 30 \$(nohup) java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
    }

    stageName = "Paso 9: Testear Artefacto - Dormir(Esperar 20sg)"
    stage("$stageName"){
        env.TAREA = "$stageName"
        sleep 20
        sh "curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
        sleep 10
    }
}

return this;