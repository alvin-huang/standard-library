def call(body) {
    def pipelineParams = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    node('master'){
        stage('Git Checkout'){
            deleteDir()
            checkout scm
        }
        
        stepDockerfileLint() // Lint any dockerfiles in the PWD

        stage('Build and Test') {
            if (env.BRANCH_NAME.startsWith('PR-*')) { // Run pr_script for PRs
                echo "[stdlib] Running pr_script for PRs"
                sh pipelineParams.pr_script
                return
            } else if (env.BRANCH_NAME != 'master') { // Run feature_script for feature branches
                echo "[stdlib] Running feature_script for feature branch"
                sh pipelineParams.feature_script
                return
            } else { // Run master release_script
                echo "[stdlib] Running release_script for master branch"
                sh pipelineParams.release_script
            }
        }
        // if a report pattern is defined we published them after the build and test
        stage('blah') {
          echo "blah"
        }
        echo "i am here"
        echo "${pipelineParams.reports_pattern}"
        if (pipelineParams.reports_pattern) {
            stage('Publish Reports') {
                junit "${pipelineParams.reports_pattern}"
                step([$class: 'Publisher', reportFilenamePattern: "${pipelineParams.reports_pattern}"])

            }
        }
        // only package for master branch
        if (env.BRANCH_NAME == 'master') {
            stage('Package'){
                echo "[stdlib] building a package"
                // stashing artifacts so we can publish them
                stash includes: "${pipelineParams.artifact_pattern}", name: 'artifacts'
            }
        }
    }
    // integration tests
    if(env.BRANCH_NAME == 'master') {
        node('master'){
            def oses = ['ubuntu', 'centos']
            def builders = [:]

            for (x in oses) {
                def label = x

                builders[label] = {
                    node {
                        docker.image(label).inside {
                            stage("testing $label") {
                                echo "integration testing $label image"
                            }
                        }
                    }
                }
            }
            parallel builders
        }
    }
    // upload binary to S3
    stage('Upload to S3') {
        slackSend (color: '#00FF00', channel: '#validations', message: "Waiting for input validation on whether the build worked @ (${env.RUN_DISPLAY_URL})")
        def submitter = input message: "Ready to upload to S3?", ok: "YES", submitterParameter: 'submitter'
        slackSend (color: '#00FF00', channel: '#validations', message: "'$submitter' approved the deploy to S3")
        node('master'){
            unstash 'artifacts'
            withAWS(credentials: 'dd2f3729-de38-4b36-8387-9c7854c00a78',region: 'us-east-1') {
                s3Upload acl: 'Private', path: 'path/to/app/', bucket: pipelineParams.release_bucket, includePathPattern: pipelineParams.artifact_pattern
            }
        }
    }
    // create JIRA ticket if provided
    if (pipelineParams.jira_ticket) {
        stepCreateJIRATicket(pipelineParams.jira_ticket)
    }
}
