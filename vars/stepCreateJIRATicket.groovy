def call (ArrayList<LinkedHashMap> tickets) {
    for (i = 0; i < tickets.size(); i++) {
        call(tickets[i])
    }
}

def call(LinkedHashiMap <String, String> jira_ticket) {
    node {
        stage ('Create JIRA ticket') {
            def issue = [
                fields: [
                    project: [key: 'ALVIN'],
                    summary: jira_ticket.summary,
                    description: jira_ticket.description,
                    issuetype: [id: '10010']
                ]
            ]
            response = jiraNewIssue issue: issue
            echo response.successful.toString()
            echo response.data.toString()
        }
    }
}