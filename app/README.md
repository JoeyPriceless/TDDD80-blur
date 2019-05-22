#TDDD80 Project
Axel Wretman | Joseph Hughes

##Build instructions

###Client
Open the directory "client" in Android Studio.

####Recommended dependencies and software
**Android Studio:** 3.4

**Gradle-version:** 3.4.0

**Android SDK:** 28

The remaining dependencies are specified in build.gradle (Module: app)
and will be automatically synced by Gradle.

###Server
Open the directory root directory in PyCharm.

Create a run configuration using Flask with a target type "Module" and target "server".
####Recommended dependencies and software
**PyCharm:** 2019.1

**Python:** 3.7

**pip:** 10.0.1

The remaining dependencies are specified in requirements.txt
Create a virtual environment and run

_\>\>\>pip install -r requirements.txt_

in your shell.

##Coverage
An export of the coverage report can be found in app/coverage. Run index.html to view the report.