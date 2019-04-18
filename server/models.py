from __init__ import db
import uuid
import datetime

from werkzeug.security import generate_password_hash, check_password_hash
from flask_jwt_extended import create_access_token


class User(db.Model):
    id = db.Column(db.String, unique=True, primary_key=True)
    username = db.Column(db.String, unique=True, nullable=False)
    email = db.Column(db.String, unique=True, nullable=False)
    picture_path = db.Column(db.String)

    def __init__(self, username, email, picture_path=None):
        self.id = uuid.uuid4().hex
        self.username = username
        self.email = email
        if picture_path:
            self.picture_path = picture_path

    def generate_auth_token(self):
        expires = datetime.timedelta(weeks=1)
        token = create_access_token(self.id, expires_delta=expires)
        return {'token': token}

    def serialize(self):
        return {
            'id': self.id,
            'username': self.username,
            'email': self.email,
            'picture': self.picture_path
        }


class Post(db.Model):
    id = db.Column(db.String, unique=True, primary_key=True)
    author_id = db.Column(db.String, db.ForeignKey('user.id'))
    content = db.Column(db.String, nullable=False)
    timestamp = db.Column(db.DateTime, nullable=False)

    def __init__(self, author, content):
        self.id = uuid.uuid4().hex
        self.author_id = author
        self.content = content
        self.timestamp = datetime.datetime.now()

    def get_reactions(self):
        return PostReaction.query.filter_by(post_id=self.id).all()

    def kill_children(self, temp_db):
        comments = Comment.query.filter_by(post_id=self.id).all()
        for comment in comments:
            comment.kill_children(temp_db)
        # TODO: TAKE A LOOK AT THIS
        temp_db.session.query(Comment).filter(Comment.id == self.id).delete()
        temp_db.session.query(PostReaction).filter(PostReaction.post_id == self.id).delete()
        temp_db.session.commit()
        
    def reaction_count(self, reaction_type):
        sq = db.session.query(PostReaction).count(PostReaction.post_id).label('count') \
            .group_by(PostReaction.post_id).subquery()
        result = db.session.query(Post, sq.c.count)\
            .join(sq, sq.c.post_id == self.id, sq.c.reaction_type == reaction_type).all()
        return result

    def serialize(self):
        return {
            'id': self.id,
            'author_id': self.author_id,
            'content': self.content,
            'timestamp': self.timestamp,
        }


class PostReaction(db.Model):
    id = db.Column(db.Integer, autoincrement=True, unique=True, primary_key=True)
    post_id = db.Column(db.String, db.ForeignKey('post.id'), unique=True, nullable=False)
    user_id = db.Column(db.String, db.ForeignKey('user.id'), unique=True, nullable=False)
    reaction_type = db.Column(db.Integer, nullable=False)

    def __init__(self, post_id, user_id, reaction_type):
        self.post_id = post_id
        self.user_id = user_id
        if reaction_type > 6 or reaction_type < 0:
            self.reaction_type = 0
        else:
            self.reaction_type = reaction_type

    def serialize(self):
        return {
            'id': self.id,
            'post_id': self.post_id,
            'user_id': self.user_id,
            'reaction_type': self.reaction_type,
        }


class CommentReaction(db.Model):
    id = db.Column(db.Integer, autoincrement=True, unique=True, primary_key=True)
    comment_id = db.Column(db.String, db.ForeignKey('post.id'), unique=True, nullable=False)
    user_id = db.Column(db.String, db.ForeignKey('user.id'), unique=True, nullable=False)
    reaction_type = db.Column(db.Integer, nullable=False)

    def __init__(self, post_id, user_id, reaction_type):
        self.post_id = post_id
        self.user_id = user_id
        if reaction_type > 1 or reaction_type < 0:
            self.reaction_type = 0
        else:
            self.reaction_type = reaction_type

    def serialize(self):
        return {
            'id': self.id,
            'comment_id': self.comment_id,
            'user_id': self.user_id,
            'reaction_type': self.reaction_type,
        }


class FeedObject(db.Model):
    __tablename__ = "feed"
    FEED_HOT = "HOT"

    id = db.Column(db.Integer, autoincrement=True, unique=True, primary_key=True)
    type = db.Column(db.String, nullable=False)
    post_id = db.Column(db.String, db.ForeignKey('post.id'), unique=True)
    post = db.relationship('Post', backref='feed')


    def __init__(self, post, type):
        self.type = self.FEED_HOT
        self.post = post
        self.post_id = post.id

    def serialize(self):
        return {
            'id': self.id,
            'type': self.type,
            'post': self.post,
        }


class Comment(db.Model):
    id = db.Column(db.String, unique=True, primary_key=True)
    author = db.Column(db.String, db.ForeignKey('user.id'), unique=True)
    upvotes = db.Column(db.Integer, nullable=False)
    downvotes = db.Column(db.Integer, nullable=False)
    timestamp = db.Column(db.DateTime, nullable=False)
    content = db.Column(db.String, nullable=False)
    parent_id = db.Column(db.String, db.ForeignKey('comment.id'), unique=False)
    children = db.relationship('Comment', backref=db.backref('parent', remote_side=[id]))
    post_id = db.Column(db.String, db.ForeignKey('post.id'), nullable=False)

    def __init__(self, author, content, parent, post_id):
        self.id = uuid.uuid4().hex
        self.author = author
        self.upvotes = 0
        self.downvotes = 0
        self.timestamp = datetime.datetime.now()
        self.content = content
        self.parent = parent
        self.post_id = post_id

    def get_reactions(self):
        return CommentReaction.query.filter_by(comment_id=self.id).all()

    def kill_children(self, temp_db):
        temp_db.session.query(CommentReaction).filter(CommentReaction.id == self.id).delete()
        temp_db.session.commit()

    def serialize(self):
        return {
            'id': self.id,
            'author': self.author,
            'upvotes': self.upvotes,
            'downvotes': self.downvotes,
            'timestamp': self.timestamp,
            'content': self.content,
            'parent': self.parent,
        }


class UserPreference(db.Model):
    id = db.Column(db.Integer, autoincrement=True, unique=True, primary_key=True)
    user = db.Column(db.String, db.ForeignKey('user.id'), unique=True)
    language = db.Column(db.String)
    locale = db.Column(db.String)

    def __init__(self, user, language="eng", locale="se"):
        self.user = user
        self.language = language
        self.locale = locale


class UserCredentials(db.Model):
    id = db.Column(db.Integer, autoincrement=True, unique=True, primary_key=True)
    user_id = db.Column(db.String, db.ForeignKey('user.id'), unique=True)
    user = db.relationship('User', backref='usercredentials')
    password = db.Column(db.String, nullable=False)

    def __init__(self, user, password):
        self.user_id = user.id
        self.user = user
        # TODO: Hash password and stuff
        self.password = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password, password)


class Blacklisted(db.Model):
    id = db.Column(db.Integer, unique=True, autoincrement=True, primary_key=True)
    token_identifier = db.Column(db.String, unique=True)

    def __init__(self, token_identifier):
        self.token_identifier = token_identifier
