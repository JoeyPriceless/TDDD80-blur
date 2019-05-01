from rq import Queue
from redis import Redis
from models import Post, PostReaction, FeedObject
from __init__ import db
import time
import datetime

redis_conn = Redis()
q = Queue(connection=redis_conn)
FEED_LENGTH = 100
SCORE_MULTIPLIER = 10


def start_timer():
    create_feed()
    time.sleep(1500)


def create_feed():
    # Score = total_vote_score * multiplier / time_since_posted
    posts = Post.query.order_by(Post.time_created).limit(FEED_LENGTH)
    for post in posts:
        feedObject = FeedObject(post, get_total_score(post) * SCORE_MULTIPLIER)
        db.session.add(feedObject)

    db.session.commit()
    # TODO: Sort through posts and compile the top.
    pass

def get_total_score(post):
    pass

def count_reactions_per_post(reaction_type):
    sq = db.session.query(PostReaction).count(PostReaction.post_id).label('count') \
        .group_by(PostReaction.post_id).subquery()
    result = db.session.query(Post, sq.c.count) \
        .join(sq, sq.c.post_id == Post.id, sq.c.reaction_type == reaction_type).all()
