import requests
import unittest
import json
from flask import jsonify
from models import *
import server

URL_ROOT = "https://tddd80-server.herokuapp.com/"


class TestServerFunctions(unittest.TestCase):

    def test_0_login(self):
        r = requests.post(URL_ROOT + "user", json={'username': 'test_user', 'password': 'password123'})
        self.assertEqual(r.status_code, 200)
        print("User with id: " + get_field(r, 'response') + " created.")
        r = requests.post(URL_ROOT + "user/login", json={'username': 'test_user', 'password': 'password123'})
        self.assertEqual(r.status_code, 200)
        self.token = get_field(r, 'token')
        self.user_id = get_field(r, 'user_id')
        print("Logged in with jwt token: " + self.token)

    def test_1_feed(self):
        if self.token is None:
            self.test_0_login()

        pass

    def test_2_post(self):
        if self.token is None:
            self.test_0_login()
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
