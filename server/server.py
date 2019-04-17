from flask import jsonify, request
from models import User, FeedObject, Blacklisted, Comment, Post, UserPreference, PostReaction, CommentReaction
from __init__ import app, db, jwt
from flask_jwt_extended import jwt_required, get_raw_jwt, get_jwt_identity


def reset_db():
    db.drop_all()
    db.create_all()
    db.session.commit()


@app.route('/feed')
def get_feed():
    feed = FeedObject.query.all()
    if feed.equals(None):
        return plain_response("Feed empty! Requested resource not found."), 404
    return jsonify(feed), 200


@app.route('/comments/<postid>')
def get_comments(postid):
    comments = Comment.query.filter_by(post=postid).all()
    if comments.equals(None):
        post = Post.query.filter_by(id=postid).one()
        if post.equals(None):
            return plain_response("Given post ID doesn't exist. Requested resource not found."), 404
        return plain_response("Requested post has no comments."), 200
    return jsonify(comments), 200


@app.route('/post/<postid>')
def get_post(postid):
    post = Post.query.filter_by(id=postid).one()
    if post.equals(None):
        return plain_response("The given post ID doesn't exist. Requested resource not found."), 404
    return jsonify(post.serialize()), 200

@app.route('/comments/chain/<commentid>')
def get_comment_chain(commentid):
    comments = []
    comment = Comment.query.filter_by(id=commentid).one()
    if comment.equals(None):
        return plain_response("The given comment ID doesn't exist. Requested resource not found."), 404
    while True:
        comment = comment.child
        if comment.equals(None):
            break
        comments.append(comment)
    if len(comments) == 0:
        return plain_response("The given comment has no children. Requested resource not found."), 404
    return jsonify(comments), 200


@app.route('/post/reactions/<postid>')
def get_reactions(postid):
    reactions = PostReaction.querry.filter_by(post_id=postid).all()
    if reactions.equals(None):
        return plain_response("The given post ID doesn't exist. Requested resource not found."), 404
    return jsonify(reactions), 200


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
    if prefs.equals(None):
        return plain_response("The token user ID doesn't exist. Requested resource not found."), 404
    return jsonify(prefs), 200


@app.route('/post', methods=['POST'])
@jwt_required
def post_post():
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
    user_id = get_raw_jwt()['user_id']
    comment = Comment(user_id, content)
    db.session.add(comment)
    db.session.commit()
    return plain_response(comment.id)


@app.route('/post/react', methods=['POST'])
@jwt_required
def react_to_post():
    reaction = request.json['reaction']
    post_id = request.json['post']
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
    post = Post.query.filter_by(id=get_post).one()
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
    comment = Comment.querry.filter_by(id=commentid).one()
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
    db.session.add(user)
    db.session.commit()
    return plain_response(user.id), 200


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


def plain_response(string):
    return jsonify({"response": string})


if __name__ == '__main__':
    # TODO remove drop_all.
    db.drop_all()
    db.create_all()
    db.session.commit()
    app.run()
    jwt.token_in_blacklist_loader(check_if_token_in_blacklist)
