import unittest
from models import *
import server

URL_ROOT = "https://tddd80-server.herokuapp.com/"


class TestStringMethods(unittest.TestCase):

    def test_1_create_feed_with_posts(self):
        username = "user"
        mail = "user@mail.com"
        picture_path = "./path/picture.png"
        user = User(username, mail, picture_path)
        self.assertEqual(user.username, username)
        db.session.add(user)

        # no picture path
        user_2 = User("user_2", "user_2@mail.com")
        self.assertIsNone(user_2.picture_path)
        db.session.add(user_2)

        post_1 = Post(user.id, "post 1 content")
        post_2 = Post(user.id, "post 2 content")
        db.session.add(post_1)
        db.session.add(post_2)

        db.session.commit()

        # reactions = post_1.get_reactions()
        # print("Reaction ID: " + reaction_id)
        # reaction_table = PostReaction.query.filter_by(id=reaction_id)
        # self.assertEqual(len(reaction_table.all()), 1)
        # reaction = reactions[0]
        # self.assertEqual(reaction.post_id, post_1)

        db.session.add(FeedObject(post_1))
        db.session.add(FeedObject(post_2))
        db.session.commit()

        feed = FeedObject.query.all()
        self.assertEqual(len(feed), 2)

    def test_2_add_comments(self):
        pass


server.reset_db()
if __name__ == '__main__':
    unittest.main()