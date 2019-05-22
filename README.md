<h2>Android project</h2>
Open the tddd80-projekt\client folder as a project in Android Studio, not the project 
root folder. If Android Studio doesn't automatically mark the correct folders, mark 
tddd80-projekt\client\app\src\main\java as Source Root and 
tddd80-projekt\client\app\src\main\res as Resource Root.

<h2>Server project</h2>
Install requirements with <b>pip install</b> in the project folder. 
Make sure that the tddd80-projekt\app is marked as source root in pycharm. 
If you want to launch the server localy use a "Flask server" launch configuration with 
"server" as the specified module. A .secret file with a random string also needs to be 
created in the tddd80-projekt\app\server together with an image named steviewonder.jpg. 
An sqlite database must also be running on "sqlite:///test.db".