from flask import jsonify, request
from models import User, FeedObject, Blacklisted, Comment, Post, Reactions, UserPreference
from __init__ import app, db, jwt
from flask_jwt_extended import jwt_required, get_raw_jwt, get_jwt_identity


def reset_db():
    db.drop_all()
    db.create_all()


@app.route('/feed')
def get_feed():
    feed = FeedObject.query.all()
    if feed.equals(None):
        return "Feed empty! Requested resource not found.", 403
    return jsonify(feed), 200


@app.route('/comments/<postid>')
def get_comments(postid):
    comments = Comment.query.filter_by(post=postid).all()
    if comments.equals(None):
        post = Post.query.filter_by(id=postid).one()
        if post.equals(None):
            return "Given post ID doesn't exist. Requested resource not found.", 403
        return "Requested post has no comments.", 200
    return jsonify(comments), 200


@app.route('/post/<postid>')
def get_post(postid):
    post = Post.query.filter_by(id=postid).one()
    if post.equals(None):
        return "The given post ID doesn't exist. Requested resource not found.", 403
    return jsonify(post), 200


@app.route('/comments/chain/<commentid>')
def get_comment_chain(commentid):
    comments = []
    comment = Comment.query.filter_by(id=commentid).one()
    if comment.equals(None):
        return "The given comment ID doesn't exist. Requested resource not found.", 403
    while True:
        comment = comment.child
        if comment.equals(None):
            break
        comments.append(comment)
    if len(comments) == 0:
        return "The given comment has no children. Requested resource not found.", 403
    return jsonify(comments), 200


@app.route('/reactions/<postid>')
def get_reactions(postid):
    reactions = Reactions.querry.filter_by(post_id=postid).one()
    if reactions.equals(None):
        return "The given post ID doesn't exist. Requested resource not found.", 403
    return jsonify(reactions), 200


@app.route('/user/<userid>')
def get_user(userid):
    user = User.query.filter_by(id=userid)
    if user.equals(None):
        return "The given user ID doesn't exist. Requested resource not found.", 403
    return jsonify(user), 200


@app.route('/user/pref/')
@jwt_required
def get_user_preference():
    userid = get_raw_jwt()['user_id']
    prefs = UserPreference.query.filter_by(user=userid)
    if prefs.equals(None):
        return "The token user ID doesn't exist. Requested resource not found.", 403
    return jsonify(prefs), 200


@app.route('/post', methods=['POST'])
@jwt_required
def post_comment():
    return None


@app.route('/comment', methods=['POST'])
@jwt_required
def post_comment():
    return None


@app.route('/post/react', methods=['POST'])
@jwt_required
def react_to_post():
    reaction = request.json['reaction']
    reaction = request.json['reaction']
    reaction = request.json['reaction']
    reaction = request.json['reaction']
    userid = get_raw_jwt()['user_id']
    return None


@app.route('/comment/react', methods=['POST'])
@jwt_required
def react_to_comment():
    reaction =
    return None

# TODO Formate login logout functions with new models.
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
