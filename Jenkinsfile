pipeline {
    agent any
    environment {
        NEXUS_USER      = credentials('NEXUS-USER')
        NEXUS_PASSWORD = credentials('NEXUS-PASS')
        TAREA = ""
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
                    sh "printenv"
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
                    slackSend color: 'good', message: "[ecabrera] [${env.JOB_NAME}] [${env.BUILD_DISPLAY_NAME}] Ejecucion Exitosa", tokenCredentialId: 'jenkins-slack-plugin'
                }

                failure{
                    slackSend color: 'danger', message: "[ecabrera] [${env.JOB_NAME}] [${env.BUILD_DISPLAY_NAME}] Ejecucion fallida en stage [${env.TAREA}]", tokenCredentialId: 'jenkins-slack-plugin'
                }
            }
        }
    }
}