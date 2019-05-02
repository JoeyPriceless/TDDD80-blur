from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask_jwt_extended import JWTManager
#from batch_jobs import start_timer
import os


print("Running __init__")
app = Flask(__name__)
if 'NAMESPACE' in os.environ and os.environ['NAMESPACE'] == 'heroku':
    db_uri = os.environ['DATABASE_URL']
    secret = os.environ['SECRET_KEY']
    debug_flag = False
else: # when running locally with sqlite
    db_uri = 'sqlite:///test.db'
    f = open(".secret", 'r')
    secret = f.read()
    f.close()
    debug_flag = True


app.config['SQLALCHEMY_DATABASE_URI'] = db_uri
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['SECRET_KEY'] = secret
# Makes sure that the black list is checked when jwt_required is called.
app.config['JWT_BLACKLIST_ENABLED'] = True
app.config['JWT_BLACKLIST_TOKEN_CHECKS'] = ['access', 'refresh']
db = SQLAlchemy(app)
jwt = JWTManager(app)
#start_timer()