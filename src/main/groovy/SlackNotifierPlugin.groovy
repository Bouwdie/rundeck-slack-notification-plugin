import com.dtolabs.rundeck.plugins.notification.NotificationPlugin
import com.fasterxml.jackson.databind.ObjectMapper

class DEFAULTS {}

enum JobState {
    FAILED,
    SUCCEEDED,
    STARTED
}

/**
 * Dirty but the truth; global vars are defined in this wonderful class
 */
class Globals {
    final static Map<JobState, String> stateToColor = [
            (JobState.SUCCEEDED): 'good',
            (JobState.FAILED)   : 'danger',
            (JobState.STARTED)  : 'warning',
    ].asImmutable()
}

/**
 * Send a slack notification upon job events.
 * @param execution
 * @param configuration
 */
static sendMessage(JobState state, Map execution, Map configuration) {

    def jobName
    if (execution.job.group != "") {
        jobName = execution.job.group + "/" + execution.job.name
    } else {
        jobName = execution.job.name
    }

    def text = "Execution <" + execution.href + "| #" + execution.id + "> of rundeck job <" + execution.job.href + "|" + jobName + "> " + execution.status + "!"

    //setup the fields we always send to slack
    def fields = [
            [
                    title: "Job",
                    value: "<" + execution.job.href + "|" + jobName + ">",
                    short: true
            ],
            [
                    title: "Project",
                    value: execution.project,
                    short: true
            ],
            [
                    title: "Status",
                    value: execution.status.capitalize(),
                    short: true
            ],
            [
                    title: "Execution",
                    value: "<" + execution.href + "|#" + execution.id + ">",
                    short: true
            ],
            [
                    title: "Start",
                    value: execution.dateStartedW3c,
                    short: true
            ],
            [
                    title: "End",
                    value: execution.dateEndedW3c ?: "n/a",
                    short: true
            ],
            [
                    title: "By",
                    value: execution.user,
                    short: true
            ],
            [
                    title: "Options",
                    value: execution.argstring ?: "none",
                    short: true
            ],
    ]

    if (execution.failedNodeListString != null) {
        fields << [
                title: "Failed Nodes",
                value: execution.failedNodeListString,
                short: false
        ]
    }

    def message = [
            channel    : configuration.channel,
            attachments: [
                    [
                            fallback: text,
                            pretext : text,
                            color   : Globals.stateToColor.getAt(state),
                            fields  : fields
                    ]
            ]
    ]

    // Send our message into the clouds (/TXXXXXXX/BXXXXXXX/XXXXXXXXXXXXXXXXXXXXX)
    def url = new URL(configuration.url + configuration.team + "/" + configuration.bot + "/" + configuration.token)
    def connection = url.openConnection()

    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    connection.addRequestProperty("Content-type", "application/json")

    def writer = new OutputStreamWriter(connection.getOutputStream())
    def json = new ObjectMapper()
    def payload = json.writeValueAsString(message)
    writer.write(payload)
    writer.flush()
    writer.close()
    connection.connect()

    def response = connection.content.text
    if (!"ok".equals(response)) {
        System.err.println("ERROR: Slack plugin response: " + response + " for request payload " + payload + " to " + url.toString())
    }
}

rundeckPlugin(NotificationPlugin) {
    title = "Slack"
    description = "Send a message to an incoming slack web-hook."

    configuration {
        url title: "URL", description: "Base url of slack api", defaultValue: "https://hooks.slack.com/services/", required: true
        channel title: "Channel", description: "Slack channel", required: true
        team title: "Team", description: "Team id (the 1st `TXXXXXXX` segment of the web-hook)", required: true
        bot title: "Bot", description: "Bot id (the 2nd `BXXXXXXX` segment of the web-hook)", required: true
        token title: "Token", description: "Token id (the 3rd `XXXXXXXXXXXXXXXXXXXXX` segment of the web-hook)", required: true
    }

    onstart { Map executionData, Map configuration ->
        sendMessage(JobState.STARTED, executionData, configuration)
        true
    }

    onfailure { Map executionData, Map configuration ->
        sendMessage(JobState.FAILED, executionData, configuration)
        true
    }

    onsuccess { Map executionData, Map configuration ->
        sendMessage(JobState.SUCCEEDED, executionData, configuration)
        true
    }
}