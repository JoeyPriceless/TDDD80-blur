from rq import Queue
from redis import Redis
from server.models import Post, FeedObject
import time
import datetime

redis_conn = Redis()
q = Queue(connection=redis_conn)
FEED_LENGTH = 100
SCORE_MULTIPLIER = 10


def start_timer(db):
    q.enqueue(create_feed(), db)


def create_feed(db):
    # Score = total_vote_score * multiplier / time_since_posted
    posts = Post.query.order_by(Post.time_created).limit(FEED_LENGTH)
    for post in posts:
        feed_object = FeedObject(post, post.reaction_score() * SCORE_MULTIPLIER /
                                 (datetime.datetime.today() - post.time_created).total_seconds())
        db.session.add(feed_object)

    db.session.commit()
    time.sleep(1500)
    # TODO: Sort through posts and compile the top.
