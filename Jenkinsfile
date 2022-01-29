pipeline {
    agent any
    environment {
        NEXUS_USER      = credentials('NEXUS-USER')
        NEXUS_PASSWORD = credentials('NEXUS-PASS')
    }
    parameters {
        choice(
            name:'compileTool',
            choices: ['Maven', 'Gradle'],
            description: 'Seleccione herramienta de compilacion'
        )
    }
    stages {
        stage("Pipeline"){
            steps {
                script{
                    sh "env"
                    switch(params.compileTool) {
                        case 'Maven':
                            def ejecucion = load 'maven.groovy'
                            ejecucion.call()
                        break;
                        case 'Gradle':
                            def ejecucion = load 'gradle.groovy'
                            ejecucion.call()
                        break;
                    }
                }
            }

            post{
                success{
                    slackSend color: 'good', message: "[ecabrera] [${JOB_NAME}] [${BUILD_TAG}] Ejecucion Exitosa", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'jenkins-slack-plugin'
                }

                failure{
                    slackSend color: 'danger', message: "[ecabrera] [${env.JOB_NAME}] [${BUILD_TAG}] Ejecucion fallida en stage [${env.TAREA}]", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'jenkins-slack-plugin'
                }
            }
        }
    }
}