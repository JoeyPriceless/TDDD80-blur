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
        if  picture_path:
            self.picture_path = picture_path


class Post(db.Model):
    id = db.Column(db.String, unique=True, primary_key=True)
    author = db.Column(db.String, db.ForeignKey('user.id'))
    content = db.Column(db.String, nullable=False)
    timestamp = db.Column(db.DateTime, nullable=False)
    reactions_id = db.Column(db.String, db.ForeignKey('reactions.id'), unique=True, nullable=False)
    reactions = db.relationship('Reactions', uselist=False, backref='post')

    def __init__(self, author, content):
        self.id = uuid.uuid4().hex
        self.author = author
        self.content = content
        self.timestamp = datetime.datetime.now()
        reaction = Reactions()
        self.reactions = reaction
        self.reactions_id = reaction.id

    def serialize(self):
        return{
            'id': self.id,
            'author': self.author,
            'content': self.content,
            'timestamp': self.timestamp,
            'downvotes_0': self.reactions.downvotes_0,
            'downvotes_1': self.reactions.downvotes_1,
            'downvotes_2': self.reactions.downvotes_2,
            'upvotes_0': self.reactions.upvotes_0,
            'upvotes_1': self.reactions.upvotes_1,
            'upvotes_2': self.reactions.upvotes_2,
        }


class Reactions(db.Model):
    id = db.Column(db.Integer, autoincrement=True, unique=True, primary_key=True)
    upvotes_0 = db.Column(db.Integer, nullable=False)
    upvotes_1 = db.Column(db.Integer, nullable=False)
    upvotes_2 = db.Column(db.Integer, nullable=False)
    downvotes_0 = db.Column(db.Integer, nullable=False)
    downvotes_1 = db.Column(db.Integer, nullable=False)
    downvotes_2 = db.Column(db.Integer, nullable=False)

    def __init__(self):
        self.upvotes_0 = 0
        self.upvotes_1 = 0
        self.upvotes_2 = 0
        self.downvotes_0 = 0
        self.downvotes_1 = 0
        self.downvotes_2 = 0


class FeedObject(db.Model):
    __tablename__ = "Feed"
    id = db.Column(db.Integer, autoincrement=True, unique=True, primary_key=True)
    post_id = db.Column(db.String, db.ForeignKey('post.id'), unique=True)
    post = db.relationship('Post', backref='feed')

    def __init__(self, post):
        self.post = post
        self.post_id = post.id



class Comment(db.Model):
    id = db.Column(db.String, unique=True, primary_key=True)
    author = db.Column(db.String, db.ForeignKey('user.id'), unique=True)
    upvotes = db.Column(db.Integer, nullable=False)
    downvotes = db.Column(db.Integer, nullable=False)
    timestamp = db.Column(db.DateTime, nullable=False)
    content = db.Column(db.String, nullable=False)
    parent = db.Column(db.String, db.ForeignKey('comment.id'))
    post = db.Column(db.String, db.ForeignKey('post.id'))

    def __init__(self, author, content, parent, post):
        self.id = uuid.uuid4().hex
        self.author = author
        self.upvotes = 0
        self.downvotes = 0
        self.timestamp = datetime.datetime.now()
        self.content = content
        self.parent = parent
        self.post = post

    def serialize(self):
        return {
            'id': self.id,
            'author': self.author,
            'upvotes': self.upvotes,
            'downvotes': self.downvotes,
            'timestamp': self.timestamp,
            'content': self.content,
            'parent': self.parent
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
