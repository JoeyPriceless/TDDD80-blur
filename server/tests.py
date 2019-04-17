import unittest
from models import *
import server

URL_ROOT = "https://tddd80-server.herokuapp.com/"


class TestStringMethods(unittest.TestCase):

    def test_1_create_feed_with_posts(self):
        pass

    def test_2_add_comments(self):
        pass


server.reset_db()
if __name__ == '__main__':
    unittest.main()
