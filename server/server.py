from flask import jsonify, request
from models import User, Message, Blacklisted
from __init__ import app, db, jwt
from flask_jwt_extended import jwt_required, get_raw_jwt, get_jwt_identity


# initiate DB
@app.route('/init')
def init_db():
    db.drop_all()
    db.create_all()
    return '', 200


# get feed
@app.route('/feed')
def get_feed():
    return '', 200


@app.route('/user/login', methods=["POST"])
def login():
    email = request.json['email']
    password = request.json['password']
    user = User.query.filter_by(email=email).scalar()
    if user is not None and user.check_password(password):
        value = user.generate_auth_token()
        value['user_id'] = user.id
        return jsonify(value)
    else:
        return 'Incorrect password or email', 409


@app.route('/user', methods=["POST"])
def create_user():
    username = request.json['username']
    email = request.json['email']
    if User.query.filter_by(user=username).scalar() is not None:
        return 'Username already exists', 409
    if User.query.filter_by(email=email).scalar() is not None:
        return 'Email already exists', 409
    password = request.json['password']
    # TODO make sure credentials are created in User constructor.
    user = User(username, email, password)
    db.session.add(user)
    db.session.commit()
    return str(user.id), 200


@app.route('/user/logout', methods=["POST"])
@jwt_required
def logout():
    jti = get_raw_jwt()['jti']
    blacklisted = Blacklisted(jti)
    db.session.add(blacklisted)
    db.session.commit()
    return '', 200


@jwt.token_in_blacklist_loader
def check_if_token_in_blacklist(decrypted_token):
    jti = decrypted_token['jti']
    result = Blacklisted.query.filter_by(token_identifier=jti).scalar()
    return result is not None


if __name__ == '__main__':
    app.run()
