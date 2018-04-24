//This step creates a JIRA ticket based on parameters passed in from a Jenkinsfile

def call (ArrayList<LinkedHashMap> tickets) {
    for (i = 0; i < tickets.size(); i++) {
        call(tickets[i])
    }
}

def call(LinkedHashMap <String, String> jira_ticket) {
    node {
        stage ('Create JIRA ticket') {
            def issue = [
                fields: [
                    project: [key: jira_ticket.project],
                    summary: jira_ticket.summary,
                    description: jira_ticket.description,
                    issuetype: [id: '10010']
                ]
            ]
            response = jiraNewIssue issue: issue
            slackSend (color: '#00FF00', channel: '#validations', message: "JIRA ticket created @ https://<myjiraorg>.atlassian.net/browse/${response.data['key'].toString()}")
        }
    }
}