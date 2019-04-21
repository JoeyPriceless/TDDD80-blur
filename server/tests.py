import requests
import unittest
import json
from flask import jsonify
from models import *
import server

URL_ROOT = "https://tddd80-server.herokuapp.com/"


class TestServerFunctions(unittest.TestCase):

    def test_0_login(self):
        r = requests.post(URL_ROOT + "user", json={'username': 'test_user', 'email': 'begnt@gmail.com',
                                                   'password': 'password123'})
        self.assertEqual(r.status_code, 200)
        print("User with id: " + get_field(r, 'response') + " created.")
        r = requests.post(URL_ROOT + "user/login", json={'username': 'test_user', 'password': 'password123'})
        self.assertEqual(r.status_code, 200)
        self.token = "Bearer " + get_field(r, 'token')
        self.user_id = get_field(r, 'user_id')
        print("Logged in with jwt token: " + self.token)

    def test_1_feed(self):
        if self.token is None:
            self.test_0_login()
        requests.get(URL_ROOT + "/feed/", json={'username': 'test_user', 'password': 'password123'})
        pass

    def test_2_post(self):
        if self.token is None:
            self.test_0_login()
        # Test creating a post.
        content = 'Mannen myten legenden.'
        r = requests.post(URL_ROOT + "/post", json={'content': content},
                          headers={'Authorization': self.token})
        self.assertEqual(r.status_code, 200)

        # Test getting the just created post.
        post_id = r.text
        r = requests.get(URL_ROOT + "/post/" + post_id)
        self.assertEqual(r.status_code, 200)
        post = json.loads(r.text)
        self.assertEqual(post.content, content)
        self.assertEqual(post.author_id, self.user_id)
        self.assertEqual(post.id, post_id)

        # Test that multiple posts can be created.
        content = 'Mannen som inte är legenden'
        r = requests.post(URL_ROOT + "/post", json={'content': content},
                          headers={'Authorization': self.token})
        self.assertEqual(r.status_code, 200)
        post_id = r.text
        r = requests.get(URL_ROOT + "/post/" + post_id)
        self.assertEqual(r.status_code, 200)
        post = json.loads(r.text)
        self.assertEqual(post.content, content)
        self.assertEqual(post.author_id, self.user_id)
        self.assertEqual(post.id, post_id)

        # Test reacting to post
        reaction = '1'
        r = requests.post(URL_ROOT + "/post/reactions", json={'post_id': post_id, 'reaction': reaction},
                          headers={'Authorization': self.token})
        self.assertEqual(r.status_code, 200)
        reaction_id = r.text
        r = requests.get(URL_ROOT + "/post", json={'post_id': post_id, 'reaction': reaction},
                          headers={'Authorization': self.token})

        pass

    def test_3_comment(self):
        if self.token is None:
            self.test_0_login()
        pass

    def test_4_deletion(self):
        if self.token is None:
            self.test_0_login()
        pass


def get_field(response, field):
    res = json.loads(response.text)
    return res.get(field)


def init_tests():
    r = requests.post(URL_ROOT + "user", json={'username': 'test_user', 'password': 'password123'})
    print(r)
    r = requests.post(URL_ROOT + "user/login", json={'username': 'test_user', 'password': 'password123'})
    print(r)
    return get_field(r, 'token'), get_field(r, 'user_id')


server.reset_db()

if __name__ == '__main__':
    unittest.main()
