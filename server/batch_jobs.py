from rq import Queue
from redis import Redis
from models import Post, PostReaction
from __init__ import db
import time

redis_conn = Redis()
q = Queue(connection=redis_conn)


def start_timer():
    create_feed()
    time.sleep(1500)


def create_feed():

    # TODO: Sort through posts and compile the top.


def count_reactions_per_post(reaction_type):
    sq = db.session.query(PostReaction).count(PostReaction.post_id).label('count') \
        .group_by(PostReaction.post_id).subquery()
    result = db.session.query(Post, sq.c.count) \
        .join(sq, sq.c.post_id == Post.id, sq.c.reaction_type == reaction_type).all()
