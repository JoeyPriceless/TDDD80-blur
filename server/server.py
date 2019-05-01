from flask import jsonify, request
from models import *
from __init__ import app, db, jwt
from flask_jwt_extended import jwt_required, get_raw_jwt

USERNAME_MIN_LENGTH = 3
USERNAME_MAX_LENGTH = 24
PASSWORD_MIN_LENGTH = 8


def reset_db():
    print("Dropping all tables")
    db.drop_all()
    print("Initializing all tables")
    db.create_all()
    db.session.commit()


@app.route('/feed/<feed_type>')
def get_feed_hot(feed_type):
    feed = FeedObject.query.filter_by(type=feed_type).all()
    if feed is None:
        return plain_response("Feed empty! Requested resource not found."), 404
    post_feed = list()
    for feedObject in feed:
        post_feed.append(get_post_with_extras(feedObject.post_id))
    return jsonify(post_feed)


@app.route('/comments/<postid>')
def get_comments(postid):
    comments = Comment.query.filter_by(post=postid).all()
    if comments is None:
        post = Post.query.filter_by(id=postid).one()
        if post is None:
            return plain_response("Given post ID doesn't exist. Requested resource not found."), 404
        return plain_response("Requested post has no comments.")
    return jsonify(comments)


@app.route('/post/<postid>')
def get_post(postid):
    post = Post.query.filter_by(id=postid).one()
    if post is None:
        return plain_response("The given post ID doesn't exist. Requested resource not found."), 404
    return jsonify(post.serialize())


@app.route('/post/extras/<postid>')
def get_post_with_extras(postid):
    post = Post.query.filter_by(id=postid).one()
    author = User.query.filter_by(id=post.author_id).one()
    reactions = serialize_list(PostReaction.query.filter_by(post_id=postid).all())

    # serialized for easier gson handling according to https://stackoverflow.com/a/39320732/4400799
    return jsonify({
        'post': post.serialize(),
        'author': author.serialize(),
        'reactions': reactions
    })


@app.route('/comments/chain/<commentid>')
def get_comment_chain(commentid):
    comments = []
    comment = Comment.query.filter_by(id=commentid).one()
    if comment is None:
        return plain_response("The given comment ID doesn't exist. Requested resource not found."), 404
    # TODO: This is outdated, we now support multiple children. Is that even right?
    while comment is not None:
        comment = comment.child
        if comment is None:
            break
        comments.append(comment)
    if len(comments) == 0:
        return plain_response("The given comment has no children. Requested resource not found."), 404
    return jsonify(comments)


@app.route('/post/react/<postid>')
def get_reactions(postid):
    reactions = PostReaction.query.filter_by(post_id=postid).all()
    if reactions is None:
        return plain_response("The given post ID doesn't exist. Requested resource not found."), 404
    return jsonify(serialize_list(reactions))


@app.route('/user/<userid>')
def get_user(userid):
    user = User.query.filter_by(id=userid).one()
    if user is None:
        return plain_response("The given user ID doesn't exist. Requested resource not found."), 404
    return jsonify(user.serialize())


@app.route('/user/pref/')
@jwt_required
def get_user_preference():
    userid = get_raw_jwt()['identity']
    prefs = UserPreference.query.filter_by(user=userid).one()
    if prefs is None:
        return plain_response("The token user ID doesn't exist. Requested resource not found."), 404
    return jsonify(prefs)


@app.route('/post', methods=['POST'])
@jwt_required
def create_post():
    content = request.json['content']
    user_id = get_raw_jwt()['identity']
    post = Post(user_id, content)
    db.session.add(post)
    db.session.commit()
    return plain_response(post.id)


@app.route('/comment', methods=['POST'])
@jwt_required
def post_comment():
    content = request.json['content']
    parent = request.json['parent']
    post_id = request.json['post_id']
    user_id = get_raw_jwt()['identity']
    # TODO: Needs to be able to handle posting comments without a parent comment.
    comment = Comment(user_id, content, parent, post_id)
    db.session.add(comment)
    db.session.commit()
    return plain_response(comment.id)


@app.route('/post/react', methods=['POST'])
@jwt_required
def react_to_post():
    reaction = int(request.json['reaction'])
    post_id = request.json['post_id']
    user_id = get_raw_jwt()['identity']
    post_reaction = PostReaction(post_id, user_id, reaction)
    db.session.add(post_reaction)
    db.session.commit()
    return plain_response(post_reaction.id)


@app.route('/comment/react', methods=['POST'])
@jwt_required
def react_to_comment():
    reaction = request.json['reaction']
    comment_id = request.json['comment_id']
    user_id = get_raw_jwt()['user']
    comment_reaction = CommentReaction(comment_id, user_id, reaction)
    db.session.add(comment_reaction)
    db.session.commit()
    return plain_response(comment_reaction.id)


@app.route('/post/delete/<postid>')
@jwt_required
def delete_post(postid):
    post = Post.query.filter_by(id=postid).one()
    if post is None:
        return plain_response("The given post ID doesn't exist. Requested resource not found."), 404
    if post.author != get_raw_jwt()['identity']:
        return plain_response("User is not author of specified post. Permission denied."), 403
    post.kill_children(db)
    db.session.delete(post)
    db.session.commit()


@app.route('/comment/delete/<commentid>')
@jwt_required
def delete_comment(commentid):
    comment = Comment.query.filter_by(id=commentid).one()
    if comment is None:
        return plain_response("The given post ID doesn't exist. Requested resource not found."), 404
    if comment.author != get_raw_jwt()['identity']:
        return plain_response("User is not author of specified post. Permission denied."), 403
    comment.kill_children(db)
    db.session.delete(comment)
    db.session.push()


@app.route('/user', methods=["POST"])
def create_user():
    username = request.json['username']
    email = request.json['email']
    password = request.json['password']

    if len(username) < USERNAME_MIN_LENGTH or len(username) > USERNAME_MAX_LENGTH:
        return plain_response('Invalid username length. Must be between 3-24 characters.'), 409
    if len(password) < PASSWORD_MIN_LENGTH:
        return plain_response('Invalid password. Must be longer than 8 characters.'), 409
    if User.query.filter_by(username=username).scalar() is not None:
        return plain_response('Username already exists'), 409
    if User.query.filter_by(email=email).scalar() is not None:
        return plain_response('Email already exists'), 409

    user = User(username, email)
    credentials = UserCredentials(user, password)
    db.session.add(user)
    db.session.add(credentials)
    db.session.commit()
    return jsonify(get_user_token(user))


@app.route('/user/login', methods=["POST"])
def login():
    email = request.json['email']
    password = request.json['password']
    if len(password) < PASSWORD_MIN_LENGTH:
        return plain_response('Invalid password. Must be longer than 8 characters.'), 409
    user = User.query.filter_by(email=email).scalar()
    if user is not None:
        credentials = UserCredentials.query.filter_by(user_id=user.id).scalar()
    else:
        return plain_response('Incorrect password or email'), 409
    if credentials is None:
        return plain_response('Could not find credentials for existing user'), 500
    elif credentials.check_password(password):
        return jsonify(get_user_token(user))
    else:
        return plain_response('Incorrect password or email'), 409


def get_user_token(user):
    value = user.generate_auth_token()
    value['user_id'] = user.id
    return value


@app.route('/user/logout', methods=["POST"])
@jwt_required
def logout():
    jti = get_raw_jwt()['jti']
    blacklisted = Blacklisted(jti)
    db.session.add(blacklisted)
    db.session.commit()
    return plain_response('')


@jwt.token_in_blacklist_loader
def check_if_token_in_blacklist(decrypted_token):
    jti = decrypted_token['jti']
    result = Blacklisted.query.filter_by(token_identifier=jti).scalar()
    return result is not None


def plain_response(string):
    return jsonify({"response": string})


def serialize_list(lst):
    return [element.serialize() for element in lst]


if __name__ == '__main__':
    db.create_all()
    db.session.commit()
    app.run()
    jwt.token_in_blacklist_loader(check_if_token_in_blacklist)
