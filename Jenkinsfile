pipeline {
  agent any
  options {
    quietPeriod(30)
    buildDiscarder(logRotator(numToKeepStr: '3'))
  }
  environment {
    DEFAULT_BRANCH = 'main'
    DEPLOYMENT_FILE = 'k8s/dev/'
  }
  triggers {
    // Needed to trigger builds from Bitbucket
    pollSCM('')
  }
  tools {
    maven '3.8.2'
    jdk 'JDK17'
  }
  stages {
    stage('Prepare') {
      steps {
        checkout scm
      }
    }
    stage('Build') {
      steps {
        octriMavenBuild(deployArtifacts: env.BRANCH_NAME == env.DEFAULT_BRANCH)
      }
    }
    stage('Build Docker image') {
      when {
        branch env.DEFAULT_BRANCH
      }
      steps {
        mavenDockerMetadata(env)
        buildProjectDockerImage(env.IMAGE_NAME)
      }
    }
    stage('Deploy latest build') {
      when {
        branch env.DEFAULT_BRANCH
      }
      steps {
        deployLatestBuild(env.DEPLOYMENT_FILE)
      }
    }
  }
  post {
    // TODO: Uncomment when there are tests
    // always {
    //   junit 'target/surefire-reports/**/*.xml'
    // }
    unsuccessful {
      emailStatusChange()
    }
    fixed {
      emailStatusChange()
    }
  }
}
