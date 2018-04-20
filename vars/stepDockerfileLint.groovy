/*
* This step lints all Dockerfiles within a repo.
*/

def call() {
  stage("Linting Dockerfile") {

    String [] dockerfile_list = sh(returnStdout: true, script: 'find `pwd` -type f -name "Dockerfile*"').trim().split('\n')
    for (i = 0; i < dockerfile_list.size(); i++) {
      echo "[standard-library] linting ${dockerfile_list[i]}"
      sh "docker run -i --rm -v ${dockerfile_list[i]}:/root/Dockerfile projectatomic/dockerfile-lint dockerfile_lint -p -f /root/Dockerfile"
    }
  }
}
