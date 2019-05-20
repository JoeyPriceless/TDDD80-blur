from server import db, jwt, app
import uuid
import datetime

from werkzeug.security import generate_password_hash, check_password_hash
from flask_jwt_extended import create_access_token
from server.util import format_datetime, serialize_list


class User(db.Model):
    id = db.Column(db.String, unique=True, primary_key=True)
    username = db.Column(db.String, unique=True, nullable=False)
    email = db.Column(db.String, unique=True, nullable=False)
    picture_uri = db.Column(db.String)

    def __init__(self, username, email, picture_uri=None):
        self.id = uuid.uuid4().hex
        self.username = username
        self.email = email
        if picture_uri:
            self.picture_uri = picture_uri

    def generate_auth_token(self):
        expires = datetime.timedelta(weeks=1)
        token = create_access_token(self.id, expires_delta=expires)
        return {'token': token}

    def serialize(self):
        return {
            'id': self.id,
            'username': self.username,
            'email': self.email,
            'picture': self.picture_uri
        }


class Post(db.Model):
    id = db.Column(db.String, unique=True, primary_key=True)
    author_id = db.Column(db.String, db.ForeignKey('user.id'))
    content = db.Column(db.String, nullable=False)
    time_created = db.Column(db.DateTime, nullable=False)
    location = db.Column(db.String, nullable=True)
    attachment_uri = db.Column(db.String, nullable=True, unique=True)

    def __init__(self, author, content, location=None, attachment_uri=None):
        self.id = uuid.uuid4().hex
        self.author_id = author
        self.content = content
        self.time_created = datetime.datetime.now()
        self.location = location
        if attachment_uri:
            self.attachment_uri = attachment_uri

    def get_reactions(self):
        return PostReaction.query.filter_by(post_id=self.id).all()

    def kill_children(self, temp_db):
        comments = Comment.query.filter_by(post_id=self.id).all()
        for comment in comments:
            comment.kill_children(temp_db)
        # TODO: TAKE A LOOK AT THIS
        temp_db.session.query(Comment).filter(Comment.id == self.id).delete()
        temp_db.session.query(PostReaction).filter(PostReaction.post_id == self.id).delete()
        temp_db.session.query(FeedObject).filter(FeedObject.post_id == self.id).delete()
        temp_db.session.commit()
        
    def reaction_count(self, reaction_type):
        score = 0
        reactions = self.get_reactions()
        # Score +1 if reaction type is positive and -1 if it's negative
        # Reaction type is an int between 0-5, with the first 3 being positive reactions.
        for reaction in reactions:
            score += 1 if (reaction.reaction_type == int(reaction_type)) else 0
        return score

    def reaction_score(self):
        score = 0
        reactions = self.get_reactions()
        # Score +1 if reaction type is positive and -1 if it's negative
        # Reaction type is an int between 0-5, with the first 3 being positive reactions.
        for reaction in reactions:
            score += 1 if (reaction.reaction_type < 3) else -1
        return score

    def serialize(self, user_id=None):
        # serialized for easier gson handling according to
        # https://stackoverflow.com/a/39320732/4400799
        author = User.query.filter_by(id=self.author_id).one()
        # should probably use count_reactions
        return {
            'id': self.id,
            'author': author.serialize(),
            'content': self.content,
            'time_created': {
                'datetime': format_datetime(self.time_created)
            },
            'location': self.location,
            'reactions': self.serialize_reactions(user_id),
            'attachment_uri': self.attachment_uri,
        }

    def serialize_reactions(self, user_id=None):
        reactions = serialize_list(PostReaction.query.filter_by(post_id=self.id).all())
        # Generates the requesters own reaction type if it exists.
        # If the requester isn't logged in or hasn't reacted, the value is -1.
        own_reaction_type = "null"
        if user_id:
            own_reaction = PostReaction.query.filter_by(post_id=self.id, user_id=user_id).scalar()
            own_reaction_type = own_reaction.reaction_type if own_reaction else "null"
        return {
            'score': self.reaction_score(),
            'own_reaction': own_reaction_type
        }


class Comment(db.Model):
    id = db.Column(db.String, unique=True, primary_key=True)
    author_id = db.Column(db.String, db.ForeignKey('user.id'), unique=False)
    time_created = db.Column(db.DateTime, nullable=False)
    content = db.Column(db.String, nullable=False)
    parent_id = db.Column(db.String, db.ForeignKey('comment.id'), unique=False, nullable=True)
    children = db.relationship('Comment', backref=db.backref('parent', remote_side=[id]))
    post_id = db.Column(db.String, db.ForeignKey('post.id'), nullable=False)

    def __init__(self, author, content, post_id, parent_id=None):
        self.id = uuid.uuid4().hex
        self.author_id = author
        self.time_created = datetime.datetime.now()
        self.content = content
        self.parent_id = parent_id
        self.post_id = post_id

    def kill_children(self, temp_db):
        temp_db.session.query(CommentReaction).filter(CommentReaction.id == self.id).delete()
        temp_db.session.commit()

    def get_reactions(self):
        return CommentReaction.query.filter_by(comment_id=self.id).all()

    def reaction_score(self):
        score = 0
        reactions = self.get_reactions()
        # Score +1 if reaction type is positive and -1 if it's negative
        # Reaction type is an int between 0-5, with the first 3 being positive reactions.
        for reaction in reactions:
            score += 1 if reaction.reaction_type == 1 else -1
        return score

    def get_user_reaction(self, user_id=None):
        reactions = serialize_list(CommentReaction.query.filter_by(comment_id=self.id).all())
        # Generates the requesters own reaction type if it exists.
        # If the requester isn't logged in or hasn't reacted, the value is -1.
        own_reaction_type = "null"
        if user_id:
            own_reaction = CommentReaction.query.filter_by(comment_id=self.id, user_id=user_id).scalar()
            own_reaction_type = own_reaction.reaction_type if own_reaction else "null"
        return own_reaction_type

    def serialize(self, user_id=None):
        author = User.query.filter_by(id=self.author_id).one()
        score = self.reaction_score()
        return {
            'id': self.id,
            'author': author.serialize(),
            'time_created': {
                'datetime': format_datetime(self.time_created)
            },
            'content': self.content,
            'score': score,
            'ownScore': self.get_user_reaction(user_id)
        }


class PostReaction(db.Model):
    id = db.Column(db.Integer, autoincrement=True, unique=True, primary_key=True)
    post_id = db.Column(db.String, db.ForeignKey('post.id'), unique=False, nullable=False)
    user_id = db.Column(db.String, db.ForeignKey('user.id'), unique=False, nullable=False)
    reaction_type = db.Column(db.Integer, nullable=False)

    def __init__(self, post_id, user_id, reaction_type):
        self.post_id = post_id
        self.user_id = user_id
        if reaction_type > 5 or reaction_type < 0:
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
    comment_id = db.Column(db.String, db.ForeignKey('post.id'), nullable=False)
    user_id = db.Column(db.String, db.ForeignKey('user.id'), unique=True, nullable=False)
    reaction_type = db.Column(db.Integer, nullable=False)

    def __init__(self, comment_id, user_id, reaction_type):
        self.comment_id = comment_id
        self.user_id = user_id
        if int(reaction_type) > 1 or int(reaction_type) < 0:
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


FEED_LENGTH = 100


class FeedObject(db.Model):
    __tablename__ = "feed"

    id = db.Column(db.Integer, autoincrement=True, unique=True, primary_key=True)
    post_id = db.Column(db.String, db.ForeignKey('post.id'), unique=True)
    post = db.relationship('Post', backref='feed')
    score = db.Column(db.Integer, nullable=False)
    reaction_0 = db.Column(db.Integer, nullable=True)
    reaction_1 = db.Column(db.Integer, nullable=True)
    reaction_2 = db.Column(db.Integer, nullable=True)
    reaction_3 = db.Column(db.Integer, nullable=True)
    reaction_4 = db.Column(db.Integer, nullable=True)
    reaction_5 = db.Column(db.Integer, nullable=True)

    def __init__(self, post, score):
        self.post = post
        self.post_id = post.id
        self.score = score
        self.reaction_0 = self.post.reaction_count("0")
        self.reaction_1 = self.post.reaction_count("1")
        self.reaction_2 = self.post.reaction_count("2")
        self.reaction_3 = self.post.reaction_count("3")
        self.reaction_4 = self.post.reaction_count("4")
        self.reaction_5 = self.post.reaction_count("5")

    @staticmethod
    def get_type_sorted_feed(feed_type):
        feed = FeedObject.query.order_by(FeedObject.reaction_0).limit(FEED_LENGTH)
        if feed_type == "1":
            feed = FeedObject.query.order_by(FeedObject.reaction_1).limit(FEED_LENGTH)
        if feed_type == "2":
            feed = FeedObject.query.order_by(FeedObject.reaction_2).limit(FEED_LENGTH)
        if feed_type == "3":
            feed = FeedObject.query.order_by(FeedObject.reaction_3).limit(FEED_LENGTH)
        if feed_type == "4":
            feed = FeedObject.query.order_by(FeedObject.reaction_4).limit(FEED_LENGTH)
        if feed_type == "5":
            feed = FeedObject.query.order_by(FeedObject.reaction_5).limit(FEED_LENGTH)
        return feed.all()

    def serialize(self):
        return {
            'id': self.id,
            'post': self.post,
        }


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
