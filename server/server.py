from flask import jsonify, request
from models import User, FeedObject, Blacklisted, Comment, Post, UserPreference, PostReaction, CommentReaction,\
    UserCredentials
from __init__ import app, db, jwt
from flask_jwt_extended import jwt_required, get_raw_jwt


def reset_db():
    db.drop_all()
    db.create_all()
    db.session.commit()


@app.route('/feed/<type>')
def get_feed_hot(type):
    feed = FeedObject.query.filter_by(type=type).one()
    if feed is None:
        return plain_response("Feed empty! Requested resource not found."), 404
    return jsonify(feed.serialize()), 200


@app.route('/comments/<postid>')
def get_comments(postid):
    comments = Comment.query.filter_by(post=postid).all()
    if comments is None:
        post = Post.query.filter_by(id=postid).one()
        if post is None:
            return plain_response("Given post ID doesn't exist. Requested resource not found."), 404
        return plain_response("Requested post has no comments."), 200
    return jsonify(comments), 200


@app.route('/post/<postid>')
def get_post(postid):
    post = Post.query.filter_by(id=postid).one()
    if post is None:
        return plain_response("The given post ID doesn't exist. Requested resource not found."), 404
    return jsonify(post.serialize()), 200

app.route('/post/extras/<postid>')
def get_post_with_extras(postid):
    post = Post.query.filter_by(id=postid).one()
    author = User.query.filter_by(id=post.author_id).one()
    reactions = get_reactions(postid)

    # serialized for easier gson handling according to https://stackoverflow.com/a/39320732/4400799
    return jsonify({
        'post': post.serialize(),
        'author': author.serialize(),
        'reactions': reactions
    }), 200

@app.route('/comments/chain/<commentid>')
def get_comment_chain(commentid):
    comments = []
    comment = Comment.query.filter_by(id=commentid).one()
    if comment is None:
        return plain_response("The given comment ID doesn't exist. Requested resource not found."), 404
    while True:
        comment = comment.child
        if comment is None:
            break
        comments.append(comment)
    if len(comments) == 0:
        return plain_response("The given comment has no children. Requested resource not found."), 404
    return jsonify(comments), 200


@app.route('/post/reactions/<postid>')
def get_reactions(postid):
    reactions = PostReaction.query.filter_by(post_id=postid).all()
    if reactions is None:
        return plain_response("The given post ID doesn't exist. Requested resource not found."), 404
    return serialize_list(reactions), 200


@app.route('/user/<userid>')
def get_user(userid):
    user = User.query.filter_by(id=userid).one()
    if user is None:
        return plain_response("The given user ID doesn't exist. Requested resource not found."), 404
    return jsonify(user.serialize()), 200


@app.route('/user/pref/')
@jwt_required
def get_user_preference():
    userid = get_raw_jwt()['user_id']
    prefs = UserPreference.query.filter_by(user=userid).one()
    if prefs is None:
        return plain_response("The token user ID doesn't exist. Requested resource not found."), 404
    return jsonify(prefs), 200


@app.route('/post', methods=['POST'])
@jwt_required
def create_post():
    content = request.json['content']
    user_id = get_raw_jwt()['user_id']
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
    user_id = get_raw_jwt()['user_id']
    comment = Comment(user_id, content, parent, post_id)
    db.session.add(comment)
    db.session.commit()
    return plain_response(comment.id)


@app.route('/post/reactions', methods=['POST'])
@jwt_required
def react_to_post():
    reaction = int(request.json['reaction'])
    post_id = request.json['post_id']
    user_id = get_raw_jwt()['user_id']
    post_reaction = PostReaction(post_id, user_id, reaction)
    db.session.add(post_reaction)
    db.session.commit()
    return plain_response(post_reaction.id)


@app.route('/comment/react', methods=['POST'])
@jwt_required
def react_to_comment():
    reaction = request.json['reaction']
    comment_id = request.json['comment']
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
    if post.author != get_raw_jwt()['user_id']:
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
    if comment.author != get_raw_jwt()['user_id']:
        return plain_response("User is not author of specified post. Permission denied."), 403
    comment.kill_children(db)
    db.session.delete(comment)
    db.session.push()


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
        return plain_response('Incorrect password or email'), 409


@app.route('/user', methods=["POST"])
def create_user():
    username = request.json['username']
    email = request.json['email']
    if User.query.filter_by(username=username).scalar() is not None:
        return plain_response('Username already exists'), 409
    if User.query.filter_by(email=email).scalar() is not None:
        return plain_response('Email already exists'), 409
    password = request.json['password']
    # TODO make sure credentials are created in User constructor.
    user = User(username, email)
    credentials = UserCredentials(user, password)
    db.session.add(user)
    db.session.add(credentials)
    db.session.commit()
    return plain_response(user.id), 200


@app.route('/user/logout', methods=["POST"])
@jwt_required
def logout():
    jti = get_raw_jwt()['jti']
    blacklisted = Blacklisted(jti)
    db.session.add(blacklisted)
    db.session.commit()
    return plain_response(''), 200


@jwt.token_in_blacklist_loader
def check_if_token_in_blacklist(decrypted_token):
    jti = decrypted_token['jti']
    result = Blacklisted.query.filter_by(token_identifier=jti).scalar()
    return result is not None


def plain_response(string):
    return jsonify({"response": string})


def serialize_list(lst):
    return jsonify([element.serialize() for element in lst])


if __name__ == '__main__':
    # TODO remove drop_all.
    db.drop_all()
    db.create_all()
    db.session.commit()
    app.run()
    jwt.token_in_blacklist_loader(check_if_token_in_blacklist)
