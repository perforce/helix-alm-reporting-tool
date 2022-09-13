![Support](https://img.shields.io/badge/Support-Community-yellow.svg)
# Helix ALM Reporting Tool

The Helix ALM Reporting Tool can be used to submit automated test results to Helix ALM from any tool without writing your own JUnit/xUnit parser.

The reporting tool retrieves report files, parses them, and converts their data into objects that can be sent to the submit build call on the Helix ALM REST API. The tool uses the [halm-rest-client-api]() component to handle interactions with the REST API.

The reporting tool is used by the [Helix ALM Jenkins plugin]() to parse the JUnit and xUnit reports in Jenkins. See the [Helix ALM help](https://help.perforce.com/alm/help.php?product=helixalm&type=web&topic=JenkinsPlugin) for information about using the plugin.

[Learn more](https://www.perforce.com/products/helix-alm) about Helix ALM.

Maintained by [Perforce Software](https://www.perforce.com/).

## Requirements
* [Helix ALM 2022.2 Server](https://www.perforce.com/downloads/helix-alm) or later
* [Helix ALM REST API 2022.2](https://www.perforce.com/downloads/helix-alm) or later. Installed with the Helix ALM Server.

## License
[MIT License](LICENSE.txt)

# Using or building the reporting tool

## Building
To build, use: `./gradlew.bat build`

## Versioning
When publishing, update the `currentVersion` in the local project's `gradle.properties` file.

We follow semantic versioning for Major - Minor - Patch
* Major = Breaking
* Minor = Additive
* Patch = Bugfix

## Variables
Set any required variables in `~/.gradle/gradle.properties`. Variables can also be specified via the command line:

`./gradlew.bat build -PartifactoryUsername=username -PartifactoryPassword=password`

## Support
The reporting-tool is a community supported project and is not officially supported by Perforce.

Pull requests and issues are the responsibility of the project's moderator(s); this may be a vetted individual or team
with members outside of the Perforce organization. Perforce does not officially support these projects, therefore all
issues should be reported and managed via GitHub (not via Perforce's standard support process).
