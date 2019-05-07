from rq import Queue
from redis import Redis
from server.models import Post, FeedObject
import time
import datetime
import sys

redis_conn = Redis()
q = Queue(connection=redis_conn)
FEED_LENGTH = 100
SCORE_MULTIPLIER = 10


def start_timer(db):
    q.enqueue(create_feed(db))
    print(f"Feed creation worker started.")
    sys.stdout.flush()


def create_feed(db):
    # Score = total_vote_score * multiplier / time_since_posted
    print(f"Generating feed...")
    sys.stdout.flush()
    posts = Post.query.order_by(Post.time_created).limit(FEED_LENGTH)
    for post in posts:
        feed_object = FeedObject(post, post.reaction_score() * SCORE_MULTIPLIER /
                                 (datetime.datetime.today() - post.time_created).total_seconds())
        print(f"{feed_object.serialize()}")
        sys.stdout.flush()
        db.session.add(feed_object)

    db.session.commit()
    print(f"Feed generated...")
    sys.stdout.flush()
    time.sleep(1500)
    # TODO: Sort through posts and compile the top.
