/*
* This step lints all Dockerfiles within a repo.
*/

def call() {
  stage("Linting Dockerfile") {

    String [] dockerfile_list = sh(returnStdout: true, script: 'find `pwd` -type f -name "Dockerfile*"').trim().split('\n')
    for (i = 0; i < dockerfile_list.size(); i++) {
      echo "[standard-library] linting ${dockerfile_list[i]}"
      docker.image('projectatomic/dockerfile-lint').inside {
          sh 'dockerfile_lint -p -f Dockerfile || true' // ignoring errors for a POC
      }
    }
  }
}
