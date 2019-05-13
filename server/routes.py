from flask import jsonify, request
from server.models import *
from flask import current_app as app, send_from_directory
from flask_jwt_extended import jwt_required, jwt_optional, get_jwt_identity, get_raw_jwt
import sys
import json
import os
import cloudinary
from server.util import serialize_list
USERNAME_MIN_LENGTH = 3
USERNAME_MAX_LENGTH = 24
PASSWORD_MIN_LENGTH = 8
ALLOWED_EXTENSION = 'jpg'


def reset_db():
    print("Dropping all tables")
    db.drop_all()
    print("Initializing all tables")
    db.create_all()
    db.session.commit()


@app.route('/feed/<feedtype>')
@jwt_optional
def get_feed(feedtype):
    user_id = get_jwt_identity()
    # TODO: implement proper feed creation. Currently just returns all posts
    create_feed()
    feed = FeedObject.get_type_sorted_feed(feedtype)
    if feed is None:
        return respond(plain_response("Feed empty! Requested resource not found."), 404)

    return respond({
        'type': feedtype,
        'posts': [feed_object.post.serialize(user_id) for feed_object in feed]
    })


FEED_LENGTH = 100
SCORE_MULTIPLIER = 10


def create_feed():
    with app.app_context():
        # Score = total_vote_score * multiplier / time_since_posted
        print(f"Generating feed...")
        sys.stdout.flush()
        FeedObject.query.delete()
        posts = Post.query.order_by(Post.time_created).limit(FEED_LENGTH)
        for post in posts:
            feed_object = FeedObject(post, post.reaction_score() * SCORE_MULTIPLIER /
                                     (datetime.datetime.today() - post.time_created).total_seconds())
            print(f"{feed_object.serialize()}")
            sys.stdout.flush()
            db.session.add(feed_object)

        db.session.commit()
        print(f"Feed generated.")
        sys.stdout.flush()
        # TODO: Sort through posts and compile the top.


@app.route('/comments/<postid>')
def get_comments(postid):
    comments = Comment.query.filter_by(post_id=postid).all()
    if comments is None:
        post = Post.query.filter_by(id=postid).one()
        if post is None:
            return respond(
                plain_response("Given post ID doesn't exist. Requested resource not found."), 404)
        return respond(plain_response("Requested post has no comments."), 404)
    return respond(comments)


@app.route('/post/<postid>')
@jwt_optional
def get_post(postid):
    post = Post.query.filter_by(id=postid).scalar()
    user_id = get_jwt_identity()
    if post:
        return respond(post.serialize(user_id))
    else:
        return respond(plain_response("Given post ID doesn't exist. Requested resource not found."), 404)


@app.route('/comment/<commentid>')
def get_comment(commentid):
    comment = Comment.query.filter_by(id=commentid).scalar()
    if comment:
        return respond(comment.serialize())
    else:
        return respond(plain_response("Given comment ID doesn't exist. Requested resource not found."), 404)


@app.route('/comments/chain/<commentid>')
def get_comment_chain(commentid):
    comment = Comment.query.filter_by(id=commentid).one()
    if comment is None:
        return respond(plain_response(
            "The given comment ID doesn't exist. Requested resource not found."), 404)
    comments = comment.children
    if len(comments) == 0:
        return respond(
            plain_response("The given comment has no children. Requested resource not found."), 404)
    return respond(serialize_list(comments))


@app.route('/post/reactions/<postid>')
def get_reactions(postid):
    reactions = PostReaction.query.filter_by(post_id=postid).all()
    if reactions is None:
        return respond(
            plain_response("The given post ID doesn't exist. Requested resource not found."), 404)
    return respond(serialize_list(reactions))


@app.route('/user/<userid>')
def get_user(userid):
    user = User.query.filter_by(id=userid).one()
    if user is None:
        return respond(
            plain_response("The given user ID doesn't exist. Requested resource not found."), 404)
    return respond(user.serialize())


@app.route('/user/picture/<userid>', methods=['POST'])
def set_profile_picture(userid):
    user = User.query.filter_by(id=userid).scalar()
    if user is None:
        return respond(plain_response('No user with given ID. Resource not found.'), 404)
    if 'file' in request.files:
        file = request.files['file']
        extension = get_file_extention(file.filename)
        if file and extension == ALLOWED_EXTENSION:
            url = cloudinary.uploader.upload(file)['url']
            user.picture_uri = url
            db.session.commit()
            return respond(url, 200)
        return respond(plain_response('Invalid filename/extension.'), 409)
    return respond(plain_response('No file sent.'), 409)


@app.route('/user/picture/<userid>')
def get_profile_picture(userid):
    user = User.query.filter_by(id=userid).scalar()
    if user is None:
        return respond(plain_response('No user exists with given ID. Resource not found.'), 404)
    return respond(user.picture_uri, 200)


@app.route('/post/attachment/<postid>', methods=['POST'])
def set_post_attachment(postid):
    post = Post.query.filter_by(id=postid).scalar()
    if post is None:
        return respond(plain_response('No user with given ID. Resource not found.'), 404)
    if 'file' in request.files:
        file = request.files['file']
        extension = get_file_extention(file.filename)
        if file and extension == ALLOWED_EXTENSION:
            url = cloudinary.uploader.upload(file)
            post.attachment_uri = url
            db.session.commit()
            return respond(url, 200)
        return respond(plain_response('Invalid filename/extension.'), 409)
    return respond(plain_response('No file sent.'), 409)


@app.route('/post/attachment/<postid>')
def get_post_attachment(postid):
    post = Post.query.filter_by(id=postid).scalar()
    if post is None:
        return respond(plain_response('No post exists with given ID. Resource not found.'), 404)
    return respond(post.attachment_uri, 200)


@app.route('/post', methods=['POST'])
@jwt_required
def create_post():
    content = request.json['content']
    location = request.json['location'] if "location" in request.json else None

    user_id = get_jwt_identity()
    post = Post(user_id, content, location=location)
    db.session.add(post)
    db.session.commit()
    return respond(plain_response(post.id))


@app.route('/comment', methods=['POST'])
@jwt_required
def post_comment():
    content = request.json['content']
    parent = request.json['parent']
    post_id = request.json['post_id']
    user_id = get_jwt_identity()
    # TODO: Needs to be able to handle posting comments without a parent comment.
    comment = Comment(user_id, content, parent, post_id)
    db.session.add(comment)
    db.session.commit()
    return respond(plain_response(comment.id))


@app.route('/post/reactions', methods=['POST'])
@jwt_required
def react_to_post():
    reaction = int(request.json['reaction'])
    post_id = request.json['post_id']
    post = Post.query.filter_by(id=post_id).scalar()
    if not post:
        return respond(plain_response(
            "The given post ID doesn't exist. Requested resource not found"), 404)
    user_id = get_jwt_identity()
    # Check if PostReaction already exists. If so update its type. Otherwise, create a new reaction.
    post_reaction = PostReaction.query.filter_by(post_id=post_id, user_id=user_id).scalar()
    if post_reaction:
        # If user selects same reaction, it should be deleted. Otherwise, change to new reaction.
        if post_reaction.reaction_type == reaction:
            db.session.delete(post_reaction)
        else:
            post_reaction.reaction_type = reaction
    else:
        post_reaction = PostReaction(post_id, user_id, reaction)
        db.session.add(post_reaction)
    db.session.commit()
    return respond(post.serialize_reactions(user_id))


@app.route('/comment/reactions', methods=['POST'])
@jwt_required
def react_to_comment():
    # TODO change from reaction type to binary reaction
    reaction = request.json['reaction']
    comment_id = request.json['comment_id']
    comment = Comment.query.filter_by(id=comment_id).scalar()
    if not comment:
        return respond(plain_response(
            "The given comment ID doesn't exist. Requested resource not found"), 404)
    user_id = get_jwt_identity()
    # Check if CommentReaction already exists. If so update its type. Otherwise, create a new
    # reaction.
    comment_reaction = CommentReaction.query.filter_by(comment_id=comment_id, user_id=user_id) \
        .scalar()
    if comment_reaction:
        if comment_reaction.reaction_type == reaction:
            db.session.delete(comment_reaction)
        else:
            comment_reaction.reaction_type = reaction
    else:
        comment_reaction = CommentReaction(comment_id, user_id, reaction)
        db.session.add(comment_reaction)
    db.session.commit()
    return respond(plain_response(comment_reaction.serialize()))


@app.route('/post/<postid>', methods=['DELETE'])
@jwt_required
def delete_post(postid):
    post = Post.query.filter_by(id=postid).scalar()
    if post is None:
        return respond(
            plain_response("The given post ID doesn't exist. Requested resource not found."), 404)
    if post.author_id != get_jwt_identity():
        return respond(
            plain_response("User is not author of specified post. Permission denied."), 403)
    post.kill_children(db)
    db.session.delete(post)
    db.session.commit()
    return respond('')


@app.route('/comment/<commentid>', methods=['DELETE'])
@jwt_required
def delete_comment(commentid):
    comment = Comment.query.filter_by(id=commentid).one()
    if comment is None:
        return respond(
            plain_response("The given post ID doesn't exist. Requested resource not found."), 404)
    if comment.author_id != get_jwt_identity():
        return respond(
            plain_response("User is not author of specified post. Permission denied."), 403)
    comment.kill_children(db)
    db.session.delete(comment)
    db.session.commit()
    return respond('')


@app.route('/user', methods=["POST"])
def create_user():
    username = request.json['username']
    email = request.json['email']
    password = request.json['password']

    if len(username) < USERNAME_MIN_LENGTH or len(username) > USERNAME_MAX_LENGTH:
        return respond(
            plain_response('Invalid username length. Must be between 3-24 characters.'), 409)
    if ' ' in username:
        return respond(
            plain_response('Invalid username. Username can not contain spaces.'), 409)
    if len(password) < PASSWORD_MIN_LENGTH:
        return respond(
            plain_response('Invalid password. Must be longer than 8 characters.'), 409)
    if User.query.filter_by(username=username).scalar() is not None:
        return respond(plain_response('Username already exists'), 409)
    if User.query.filter_by(email=email).scalar() is not None:
        return respond(plain_response('Email already exists'), 409)

    user = User(username, email)
    credentials = UserCredentials(user, password)
    db.session.add(user)
    db.session.add(credentials)
    db.session.commit()
    return respond(get_user_token(user))


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
        return respond(plain_response('Incorrect password or email'), 409)

    if credentials is None:
        return respond(plain_response('Could not find credentials for existing user'), 500)
    elif credentials.check_password(password):
        return respond(get_user_token(user))
    else:
        return respond(plain_response('Incorrect password or email'), 409)


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
    return respond(plain_response(''))


def plain_response(string):
    return {"response": string}


def respond(response, status=200):
    print(f"{status}: {json.dumps(response, indent=2)}")
    sys.stdout.flush()
    return jsonify(response), status


def get_file_extention(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower()
