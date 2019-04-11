from __init__ import db, app
import uuid
import datetime


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

    def serialize(self):
        return {
            'id': self.id,
            'username': self.username,
            'email': self.email,
        }


class Post(db.Model):
    id = db.Column(db.String, unique=True, primary_key=True)
    author = db.Column(db.String, db.ForeignKey('user.id'))
    content = db.Column(db.String, nullable=False)
    timestamp = db.Column(db.DateTime, nullable=False)

    def __init__(self, author, content):
        self.id = uuid.uuid4().hex
        self.author = author
        self.content = content
        self.timestamp = datetime.datetime.now()

    def get_reactions(self):
        return PostReaction.querry.filter_by(post_id=self.id).all()

    def kill_children(self, db):
        comments = Comment.querry.filter_by(post_id=self.id).all()
        for comment in comments:
            comment.kill_children(db)
        reactions = PostReaction.querry.filter_by(post_id=self.id).all()
        db.session.delete(comments)
        db.session.delete(reactions)
        db.session.commit()

    def serialize(self):
        return{
            'id': self.id,
            'author': self.author,
            'content': self.content,
            'timestamp': self.timestamp,
        }


class PostReaction(db.Model):
    id = db.Column(db.Integer, autoincrement=True, unique=True, primary_key=True)
    post_id = db.Column(db.String, db.ForeignKey('post.id'), unique=True, nullable=False)
    user_id = db.Column(db.String, db.ForeignKey('user.id'), unique=True, nullable=False)
    vote_type = db.Column(db.Integer, nullable=False)

    def __init__(self, post_id, user_id, vote_type):
        self.post_id = post_id
        self.user_id = user_id
        if vote_type > 6 or vote_type < 0:
            self.vote_type = 0
        else:
            self.vote_type = vote_type

    def serialize(self):
        return {
            'id': self.id,
            'post_id': self.post_id,
            'user_id': self.user_id,
            'vote_type': self.vote_type,
        }


class CommentReaction(db.Model):
    id = db.Column(db.Integer, autoincrement=True, unique=True, primary_key=True)
    comment_id = db.Column(db.String, db.ForeignKey('post.id'), unique=True, nullable=False)
    user_id = db.Column(db.String, db.ForeignKey('user.id'), unique=True, nullable=False)
    vote_type = db.Column(db.Integer, nullable=False)

    def __init__(self, post_id, user_id, vote_type):
        self.post_id = post_id
        self.user_id = user_id
        if vote_type > 1 or vote_type < 0:
            self.vote_type = 0
        else:
            self.vote_type = vote_type

    def serialize(self):
        return {
            'id': self.id,
            'comment_id': self.comment_id,
            'user_id': self.user_id,
            'vote_type': self.vote_type,
        }


class FeedObject(db.Model):
    __tablename__ = "Feed"
    id = db.Column(db.Integer, autoincrement=True, unique=True, primary_key=True)
    post_id = db.Column(db.String, db.ForeignKey('post.id'), unique=True)
    post = db.relationship('Post', backref='feed')

    def __init__(self, post):
        self.post = post
        self.post_id = post.id

    def serialize(self):
        return {
            'id': self.id,
            'post': self.post,
        }


class Comment(db.Model):
    id = db.Column(db.String, unique=True, primary_key=True)
    author = db.Column(db.String, db.ForeignKey('user.id'), unique=True)
    upvotes = db.Column(db.Integer, nullable=False)
    downvotes = db.Column(db.Integer, nullable=False)
    timestamp = db.Column(db.DateTime, nullable=False)
    content = db.Column(db.String, nullable=False)
    parent = db.relationship('Comment', backref='child')
    post_id = db.Column(db.String, db.ForeignKey('post.id'))

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
        return CommentReaction.querry.filter_by(comment_id=self.id).all()

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


class Blacklisted(db.Model):
    id = db.Column(db.Integer, unique=True, autoincrement=True, primary_key=True)
    token_identifier = db.Column(db.String, unique=True)

    def __init__(self, token_identifier):
        self.token_identifier = token_identifier
