from apscheduler.schedulers.background import BlockingScheduler
from server.models import Post, FeedObject, db, app
import time
import datetime
import sys

FEED_LENGTH = 100
SCORE_MULTIPLIER = 10

sched = BlockingScheduler()


@sched.scheduled_job('interval', minutes=2)
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
        print(f"Feed generated...")
        sys.stdout.flush()
        # TODO: Sort through posts and compile the top.


sched.start()
