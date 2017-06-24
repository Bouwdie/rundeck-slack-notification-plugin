# Installing
Installing groovy plugins in _Rundeck_ is as easy as 123. Just grab the `SlackNotificationPlugin.groovy` and place it in the _Rundeck_ plugin folder. 
No restart required, a _Rundeck_ plugin scan will be with you shortly.
Don't forget to check out the [official guide](http://rundeck.org/docs/plugins-user-guide/installing.html).

# Configuring
Configuration of notifications is done at job level. 
Once the plugin has been picked up by _Rundeck_, browse to any of your jobs and: _Job_ -> _Edit_ -> _Send Notification_ -> _Slack_ and fill out your details.
For the visual minded folks an [image](docs/job-configuration.png).
The notifier supports `onStart`, `onFailure` and `onSuccess` notifications.
After that it should be smooth sailing from there on out.

# Needs fixing?
Please submit a merge request when you have a bug or feature we might all benefit from!
Working with IntelliJ? use: `gradle idea`

# Cornerstones
* http://rundeck.org/docs/developer/notification-plugin.html
* https://github.com/rundeck-plugins/pagerduty-notification/blob/master/src/PagerDutyNotification.groovy