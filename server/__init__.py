from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask_jwt_extended import JWTManager
import os

db = SQLAlchemy()
jwt = JWTManager()

def create_app():
    app = Flask(__name__)
    db.init_app(app)
    jwt.init_app(app)
    #app.config.from_object(Config)
    is_remote ='NAMESPACE' in os.environ and os.environ['NAMESPACE'] == 'heroku'
    if is_remote:
        db_uri = os.environ['DATABASE_URL']
        secret = os.environ['SECRET_KEY']
        debug_flag = False
    else:  # when running locally with sqlite
        db_uri = 'sqlite:///test.db'
        f = open("server/.secret", 'r')
        secret = f.read()
        f.close()
        debug_flag = True

    app.config['SQLALCHEMY_DATABASE_URI'] = db_uri
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
    app.config['SECRET_KEY'] = secret
    # Makes sure that the black list is checked when jwt_required is called.
    app.config['JWT_BLACKLIST_ENABLED'] = True
    app.config['JWT_BLACKLIST_TOKEN_CHECKS'] = ['access', 'refresh']


    with app.app_context():
        from . import routes
        if not is_remote:
            db.drop_all()
        db.create_all()
        jwt.token_in_blacklist_loader(check_if_token_in_blacklist)
        return app

@jwt.token_in_blacklist_loader
def check_if_token_in_blacklist(decrypted_token):
    jti = decrypted_token['jti']
    from .models import Blacklisted
    result = Blacklisted.query.filter_by(token_identifier=jti).scalar()
    return result is not None