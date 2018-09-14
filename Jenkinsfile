def slackChannel = 'components'
def version = 'will be replaced'
def image = 'will be replaced'

pipeline {
  agent {
    kubernetes {
      label 'connectors-se'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
    - name: maven
      image: jenkinsxio/builder-maven:0.0.319
      command:
      - cat
      tty: true
      volumeMounts:
      - name: docker
        mountPath: /var/run/docker.sock
      - name: m2
        mountPath: /root/.m2/repository

  volumes:
  - name: docker
    hostPath:
      path: /var/run/docker.sock
  - name: m2
    hostPath:
      path: /tmp/jenkins/tdi/m2
"""
    }
  }

  environment {
    MAVEN_OPTS = '-Dmaven.artifact.threads=128 -Dorg.slf4j.simpleLogger.showThreadName=true -Dorg.slf4j.simpleLogger.showDateTime=true -Dorg.slf4j.simpleLogger.dateTimeFormat=HH:mm:ss'
    TALEND_REGISTRY="registry.datapwn.com"
  }

  options {
    buildDiscarder(logRotator(artifactNumToKeepStr: '5', numToKeepStr: env.BRANCH_NAME == 'master' ? '10' : '2'))
    timeout(time: 60, unit: 'MINUTES')
    skipStagesAfterUnstable()
  }

  triggers {
    cron(env.BRANCH_NAME == "master" ? "@daily" : "")
  }

  stages {
    stage('Run maven') {
      steps {
        container('maven') {
          sh 'mvn clean install -T1C -Pdocker'
        }
      }
    }
    stage('Build Docker Components Image') {
      steps {
        container('maven') {
          withCredentials([
            usernamePassword(
              credentialsId: 'docker-registry-credentials',
              passwordVariable: 'DOCKER_PASSWORD',
              usernameVariable: 'DOCKER_LOGIN')
          ]) {
            sh "chmod +x ./connectors-se-docker/src/main/scripts/docker/*.sh && ./connectors-se-docker/src/main/scripts/docker/all.sh `git rev-parse --abbrev-ref HEAD | tr / _`"
          }
        }
      }
    }
    stage('Publish Site') {
      steps {
        container('maven') {
          sh 'mvn clean site:site site:stage -T1C -Dmaven.test.failure.ignore=true'
        }
      }
    }
  }
  post {
    always {
      junit testResults: '*/target/surefire-reports/*.xml', allowEmptyResults: true
      publishHTML (target: [
        allowMissing: true,
        alwaysLinkToLastBuild: false,
        keepAll: true,
        reportDir: 'target/staging',
        reportFiles: 'index.html',
        reportName: "Maven Site"
      ])
    }
    success {
      slackSend (color: '#00FF00', message: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})", channel: "${slackChannel}")
    }
    failure {
      slackSend (color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})", channel: "${slackChannel}")
    }
  }
}
