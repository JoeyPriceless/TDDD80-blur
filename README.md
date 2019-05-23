<h1>TDDD80 Project</h1>
Axel Wretman | Joseph Hughes

<h2>Build instructions</h2>

<h3>Android</h3>
Open the tddd80-projekt\client folder as a project in Android Studio, not the project 
root folder. If Android Studio doesn't automatically mark the correct folders, mark 
tddd80-projekt\client\app\src\main\java as Source Root and 
tddd80-projekt\client\app\src\main\res as Resource Root.

<h4>Recommended dependencies and software</h4>
Android Studio: 3.4

Gradle-version: 3.4.0

Android SDK: 28

The remaining dependencies are specified in build.gradle (Module: app)
and will be automatically synced by Gradle.

<h3>Server</h3>
Make sure that the tddd80-projekt\app is marked as source root in PyCharm. 
If you want to launch the server localy use a "Flask server" launch configuration with 
"server" as the specified module. A an image named steviewonder.jpg must be included in tddd80-projekt\app\server if you wish to run unit tests.

<h4>Recommended dependencies and software</h4>
PyCharm: 2019.1

Python: 3.7

pip: 10.0.1

SQLite: 3.28

The remaining dependencies are specified in requirements.txt

Create a virtual environment and run

\>\>\>pip install -r requirements.txt

in your shell.