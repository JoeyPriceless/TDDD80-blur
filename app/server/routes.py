from flask import jsonify, request
from server.models import *
from flask import current_app as app
from flask_jwt_extended import jwt_required, jwt_optional, get_jwt_identity, get_raw_jwt
import sys
import json
from cloudinary import uploader
from server.util import serialize_list
USERNAME_MIN_LENGTH = 3
USERNAME_MAX_LENGTH = 24
PASSWORD_MIN_LENGTH = 8
ALLOWED_EXTENSION = 'jpg'


def reset_db():
    """
    Used for testing purposes. NEVER in deployment enviroment!
    """
    print("Dropping all tables")
    db.drop_all()
    print("Initializing all tables")
    db.create_all()
    db.session.commit()


@app.route('/feed/<feedtype>')
@jwt_optional
def get_feed(feedtype):
    """
    Returns the pregenerated feed list ordered by the given feed type.
    """
    user_id = get_jwt_identity()
    feed = FeedObject.get_type_sorted_feed(feedtype)
    if feed is None:
        return respond(plain_response("Feed empty! Requested resource not found."), 404)

    return respond({
        'type': feedtype,
        'posts': [feed_object.post.serialize(user_id) for feed_object in feed]
    })


@app.route('/comments/<postid>')
@jwt_optional
def get_comments(postid):
    """
    Returns all comments for the post specified by given postID.
    """
    comments = Comment.query.filter_by(post_id=postid).all()
    if comments is None:
        post = Post.query.filter_by(id=postid).one()
        if post is None:
            return respond(
                plain_response("Given post ID doesn't exist. Requested resource not found."), 404)
        return respond(plain_response("Requested post has no comments."), 404)
    user_id = get_jwt_identity()
    comments_list = [comment.serialize(user_id=user_id) for comment in comments]
    return respond({'comments': comments_list})


@app.route('/post/<postid>')
@jwt_optional
def get_post(postid):
    """
    Returns the post object with the given ID.
    """
    post = Post.query.filter_by(id=postid).scalar()
    user_id = get_jwt_identity()
    if post:
        return respond(post.serialize(user_id))
    else:
        return respond(plain_response("Given post ID doesn't exist. Requested resource not found."), 404)


@app.route('/comment/<commentid>')
def get_comment(commentid):
    """
    Returns the comment object with given ID.
    """
    comment = Comment.query.filter_by(id=commentid).scalar()
    if comment:
        return respond(comment.serialize())
    else:
        return respond(plain_response("Given comment ID doesn't exist. Requested resource not found."), 404)


@app.route('/post/reactions/<postid>')
def get_reactions(postid):
    """
    Returns a list with all reaction objects that are children to the post with the given ID.
    """
    reactions = PostReaction.query.filter_by(post_id=postid).all()
    if reactions is None:
        return respond(
            plain_response("The given post ID doesn't exist. Requested resource not found."), 404)
    return respond(serialize_list(reactions))


@app.route('/user/<userid>')
def get_user(userid):
    """
    Returns the user object with the given ID.
    """
    user = User.query.filter_by(id=userid).one()
    if user is None:
        return respond(
            plain_response("The given user ID doesn't exist. Requested resource not found."), 404)
    return respond(user.serialize())


@app.route('/user/picture/<userid>', methods=['POST'])
def set_profile_picture(userid):
    """
    Takes the attached image and uploads it to cloudinary before saving the URL to the user object with the given ID.
    """
    user = User.query.filter_by(id=userid).scalar()
    if user is None:
        return respond(plain_response('No user with given ID. Resource not found.'), 404)
    if 'file' in request.files:
        file = request.files['file']
        extension = get_file_extention(file.filename)
        if file and extension == ALLOWED_EXTENSION:
            # Upload the image to cloudinary and store the URL in the user object.
            url = uploader.upload(file)['url']
            user.picture_uri = url
            db.session.commit()
            return respond(url, 200)
        return respond(plain_response('Invalid filename/extension.'), 409)
    return respond(plain_response('No file sent.'), 409)


@app.route('/user/picture/<userid>')
def get_profile_picture(userid):
    """
    Returns the cloudinary URL to the user's profile picture.
    """
    user = User.query.filter_by(id=userid).scalar()
    if user is None:
        return respond(plain_response('No user exists with given ID. Resource not found.'), 404)
    if user.picture_uri == 'null':
        respond(plain_response('No image found for user.'), 404)
    return respond(user.picture_uri, 200)


@app.route('/post/attachment/<postid>', methods=['POST'])
def set_post_attachment(postid):
    """
    Uploads the image to cloudinary and stores the url in the post object with the given ID.
    """
    print('request.files[\'file\']: ' + str(request.files))
    print('request.json ' + str(request.json))
    print('request.forms ' + str(request.form))
    sys.stdout.flush()

    post = Post.query.filter_by(id=postid).scalar()
    if post is None:
        return respond(plain_response('No post with given ID. Resource not found.'), 404)
    if 'file' in request.files:
        file = request.files['file']
        extension = get_file_extention(file.filename)
        if file and extension == ALLOWED_EXTENSION:
            # Upload the image to cloudinary and store the URL in the post object.
            url = uploader.upload(file)['url']
            post.attachment_uri = url
            db.session.commit()
            return respond(url, 200)
        return respond(plain_response('Invalid filename/extension.'), 409)
    return respond(plain_response('No file sent.'), 409)


@app.route('/post/attachment/<postid>')
def get_post_attachment(postid):
    """
    Returns the cloudinary URL for the post's attached image.
    """
    post = Post.query.filter_by(id=postid).scalar()
    if post is None:
        return respond(plain_response('No post exists with given ID. Resource not found.'), 404)
    if post.attachment_uri == 'null':
        respond(plain_response('No image found for post.'), 404)
    return respond(post.attachment_uri, 200)


@app.route('/post', methods=['POST'])
@jwt_required
def create_post():
    """
    Creates a new post object in the database and returns its ID.
    """
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
    """
    Creates a new comment object in the database and returns its ID.
    """
    content = request.json['content']
    parent = request.json['parent']
    post_id = request.json['post_id']
    user_id = get_jwt_identity()
    comment = Comment(user_id, content, post_id, parent_id=parent)
    db.session.add(comment)
    db.session.commit()
    return respond(plain_response(comment.id))


@app.route('/post/reactions', methods=['POST'])
@jwt_required
def react_to_post():
    """
    Creates a new reaction object of the specified type as a child to the post with the given ID.
    """
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
    """
    Creates a new reaction object of the specified type as a child to the comment with the given ID.
    """
    reaction = request.json['reaction']
    comment_id = request.json['comment_id']
    comment = Comment.query.filter_by(id=comment_id).scalar()
    if not comment:
        return respond(plain_response(
            "The given comment ID doesn't exist. Requested resource not found"), 404)
    user_id = get_jwt_identity()
    # Check if CommentReaction already exists. If so update its type. If it's of the same type then remove the reaction.
    # Otherwise, create a new reaction.
    comment_reaction = CommentReaction.query.filter_by(comment_id=comment_id, user_id=user_id) \
        .scalar()
    if comment_reaction:
        if comment_reaction.reaction_type == int(reaction):
            db.session.delete(comment_reaction)
        else:
            comment_reaction.reaction_type = reaction
    else:
        comment_reaction = CommentReaction(comment_id, user_id, reaction)
        db.session.add(comment_reaction)
    db.session.commit()
    return respond(plain_response(comment.reaction_score()))


@app.route('/post/<postid>', methods=['DELETE'])
@jwt_required
def delete_post(postid):
    """
    Removes the post with the given ID from the database.
    """
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
    """
    Removes the comment with the given ID from the database.
    """
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
    """
    Creates a new user object and returns a token for the new user.
    """
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
    """
    Checks credentials, creates and returns a token upon sucess.
    """
    email = request.json['email']
    password = request.json['password']
    if len(password) < PASSWORD_MIN_LENGTH:
        return plain_response('Invalid password. Must be longer than 8 characters.'), 409
    user = User.query.filter_by(email=email).scalar()
    if user is not None:
        credentials = UserCredentials.query.filter_by(user_id=user.id).scalar()
    else:
        return respond(plain_response('Incorrect email.'), 409)

    if credentials is None:
        return respond(plain_response('Could not find credentials for existing user.'), 500)
    elif credentials.check_password(password):
        return respond(get_user_token(user))
    else:
        return respond(plain_response('Incorrect password or email.'), 409)


def get_user_token(user):
    """
    Returns a fresh token for the user.
    """
    value = user.generate_auth_token()
    value['user_id'] = user.id
    return value


@app.route('/user/logout', methods=["POST"])
@jwt_required
def logout():
    """
    Blacklists the users current token.
    """
    jti = get_raw_jwt()['jti']
    blacklisted = Blacklisted(jti)
    db.session.add(blacklisted)
    db.session.commit()
    return respond(plain_response(''))


def plain_response(string):
    """
    Adds an extra layer of JSON for consistancy and ease of handling on the client side.
    """
    return {"response": string}


def respond(response, status=200):
    """
    Logs the info about the response and formats it correctly for responding.
    """
    print(f"{status}: {json.dumps(response, indent=2)}")
    sys.stdout.flush()
    return jsonify(response), status


def get_file_extention(filename):
    """
    Returns only the file extension from the file name.
    """
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower()
