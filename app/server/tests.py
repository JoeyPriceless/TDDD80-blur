import requests
import unittest
import json

URL_ROOT = "http://127.0.0.1:5000/"


class TestServerFunctions(unittest.TestCase):
    token = None
    user_id = None

    def test_0_login(self):
        r = requests.post(URL_ROOT + "user", json={'username': 'test_user', 'email': 'bengt@gmail.com',
                                                   'password': 'password123'})
        self.assertEqual(r.status_code, 200)
        print("User with id: " + get_field(r, 'user_id') + " created.")
        r = requests.post(URL_ROOT + "user/login", json={'email': 'bengt@gmail.com', 'password': 'password123'})
        self.assertEqual(r.status_code, 200)
        self.__class__.token = "Bearer " + get_field(r, 'token')
        self.__class__.user_id = get_field(r, 'user_id')

        files = {'file': open('steviewonder.jpg', 'rb')}
        r = requests.post(URL_ROOT + "user/picture/" + self.__class__.user_id, files=files)
        self.assertEqual(r.status_code, 200)

        r = requests.get(URL_ROOT + "user/picture/" + self.__class__.user_id)
        self.assertEqual(r.status_code, 200)
        # TODO: Make sure that the image is of Stevie Wonder
        print("Logged in with jwt token: " + self.__class__.token)

    def test_1_feed(self):
        if self.__class__.token is None:
            self.test_0_login()

        r = requests.post(URL_ROOT + "post", json={'content': "TESTESTEST"},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        post_id = get_field(r, 'response')
        reaction = 1
        requests.post(URL_ROOT + "post/reactions", json={'post_id': post_id, 'reaction': reaction},
                      headers={'Authorization': self.__class__.token})

        r = requests.post(URL_ROOT + "post", json={'content': "TESTESTEST"},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        post_id = get_field(r, 'response')
        reaction = 2
        r = requests.post(URL_ROOT + "post/reactions", json={'post_id': post_id, 'reaction': reaction},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        print(requests.get(URL_ROOT + "feed/" + "1").text)
        print(requests.get(URL_ROOT + "feed/" + "2").text)

    def test_2_post(self):
        if self.__class__.token is None:
            self.test_0_login()

        post_ids = list()
        # Test creating a post.
        content = 'Mannen myten legenden.'
        r = requests.post(URL_ROOT + "post", json={'content': content},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        post_id = get_field(r, 'response')
        post_ids.append(get_field(r, 'response'))

        # Upload post attachment.
        files = {'file': open('steviewonder.jpg', 'rb')}
        r = requests.post(URL_ROOT + "post/attachment/" + post_id, files=files)
        self.assertEqual(r.status_code, 200)

        # Test to see that the attached image uploaded correctly.
        r = requests.get(URL_ROOT + "post/attachment/" + post_id)
        self.assertEqual(r.status_code, 200)
        # TODO: Make sure that the image is of Stevie Wonder

        # Test getting the just created post.
        r = requests.get(URL_ROOT + "post/" + post_id)
        self.assertEqual(r.status_code, 200)
        post = json.loads(r.text)
        self.assertEqual(post.get('content'), content)
        self.assertEqual(post.get('author').get('id'), self.__class__.user_id)
        self.assertEqual(post.get('id'), post_id)

        # Test that multiple posts can be created.
        content = 'Mannen som inte är legenden'
        r = requests.post(URL_ROOT + "post", json={'content': content},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        post_id = get_field(r, 'response')
        post_ids.append(post_id)
        r = requests.get(URL_ROOT + "post/" + post_id)
        self.assertEqual(r.status_code, 200)
        post = json.loads(r.text)
        self.assertEqual(post.get('content'), content)
        self.assertEqual(post.get('author').get('id'), self.__class__.user_id)
        self.assertEqual(post.get('id'), post_id)

        # Test reacting to post
        reaction = 1
        r = requests.post(URL_ROOT + "post/reactions", json={'post_id': post_id, 'reaction': reaction},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        self.assertEqual(reaction, get_field(r, 'own_reaction'))

        # Test changing reaction
        reaction = 3
        r = requests.post(URL_ROOT + "post/reactions", json={'post_id': post_id, 'reaction': reaction},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        self.assertEqual(reaction, get_field(r, 'own_reaction'))

        # Test removing reaction
        r = requests.post(URL_ROOT + "post/reactions", json={'post_id': post_id, 'reaction': reaction},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        self.assertEqual('null', get_field(r, 'own_reaction'))

        # Test deleting post with without authorization
        for post_id in post_ids:
            r = requests.delete(URL_ROOT + "post/" + post_id)
            self.assertEqual(r.status_code, 401)
            r = requests.get(URL_ROOT + "post/" + post_id)
            self.assertEqual(r.status_code, 200)

        # Test deleting all previous posts.
        for post_id in post_ids:
            r = requests.delete(URL_ROOT + "post/" + post_id,
                                headers={'Authorization': self.__class__.token})
            self.assertEqual(r.status_code, 200)
            r = requests.get(URL_ROOT + "post/" + post_id)
            self.assertEqual(r.status_code, 404)

    def test_3_comment(self):
        if self.__class__.token is None:
            self.test_0_login()

        r = requests.post(URL_ROOT + "post", json={'content': 'Template post.'},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        post_id = get_field(r, 'response')

        comment_ids = list()
        # Test creating a comment.
        content = 'Mannen myten legenden.'
        r = requests.post(URL_ROOT + "comment", json={'content': content, 'parent': None, 'post_id': post_id},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        comment_id = get_field(r, 'response')
        comment_ids.append(comment_id)

        # Test getting the just created comment.
        r = requests.get(URL_ROOT + "comment/" + comment_id)
        self.assertEqual(r.status_code, 200)
        comment = json.loads(r.text)
        self.assertEqual(comment['content'], content)
        self.assertEqual(comment['author_id'], self.__class__.user_id)
        self.assertEqual(comment['id'], comment_id)

        # Test that multiple root comments can be created.
        content = 'Mannen som inte är legenden'
        r = requests.post(URL_ROOT + "comment", json={'content': content, 'parent': None, 'post_id': post_id},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        comment_id = get_field(r, 'response')
        comment_ids.append(comment_id)
        r = requests.get(URL_ROOT + "comment/" + comment_id)
        self.assertEqual(r.status_code, 200)
        comment = json.loads(r.text)
        self.assertEqual(comment['content'], content)
        self.assertEqual(comment['author_id'], self.__class__.user_id)
        self.assertEqual(comment['id'], comment_id)

        # Test that comment chain works.
        parent_id = comment_id
        content = 'Mannen som inte är barn'
        r = requests.post(URL_ROOT + "comment", json={'content': content, 'parent': parent_id, 'post_id': post_id},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        comment_id = get_field(r, 'response')
        comment_ids.append(comment_id)
        r = requests.get(URL_ROOT + "comment/" + comment_id)
        self.assertEqual(r.status_code, 200)
        comment = json.loads(r.text)
        self.assertEqual(comment['content'], content)
        self.assertEqual(comment['author_id'], self.__class__.user_id)
        self.assertEqual(comment['id'], comment_id)
        r = requests.get(URL_ROOT + "comments/chain/" + parent_id)
        self.assertEqual(r.status_code, 200)
        comments = json.loads(r.text)
        self.assertEqual(comments[0]['id'], comment_id)

        # Test reacting to comment
        reaction = '1'
        r = requests.post(URL_ROOT + "comment/reactions", json={'comment_id': comment_id, 'reaction': reaction},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        r = requests.get(URL_ROOT + "comment/" + comment_id)
        self.assertEqual(r.status_code, 200)
        reactions = json.loads(r.text)['reactions']
        self.assertEqual(reactions['own_reaction'], 1)
        self.assertEqual(reactions['score'], 1)

        # Test changing reaction
        reaction = '-1'
        r = requests.post(URL_ROOT + "comment/reactions", json={'comment_id': comment_id, 'reaction': reaction},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        self.assertEqual(-1, get_field(r, 'response')['reaction_type'])

        # Test removing reaction
        r = requests.post(URL_ROOT + "comment/reactions", json={'comment_id': comment_id, 'reaction': reaction},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        self.assertEqual(None, get_field(r, 'reaction_type'))

        # Test deleting all previous comments.
        for comment_id in comment_ids:
            r = requests.delete(URL_ROOT + "comment/" + comment_id,
                                headers={'Authorization': self.__class__.token})
            self.assertEqual(r.status_code, 200)
            r = requests.get(URL_ROOT + "comment/" + comment_id,
                             headers={'Authorization': self.__class__.token})
            self.assertEqual(r.status_code, 404)
        r = requests.delete(URL_ROOT + "post/" + post_id,
                            headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)

    def test_4_combo_scenario(self):
        if self.__class__.token is None:
            self.test_0_login()

        post_ids = []
        comment_ids = []

        # Create a post
        content = 'Mannen myten legenden.'
        r = requests.post(URL_ROOT + "post", json={'content': content},
                          headers={'Authorization': self.__class__.token})
        post_id = get_field(r, 'response')
        post_ids.append(post_id)

        # Create a comment
        content = 'Mannen myten legenden.'
        r = requests.post(URL_ROOT + "comment", json={'content': content, 'parent': None, 'post_id': post_id},
                          headers={'Authorization': self.__class__.token})
        comment_id = r.text
        comment_ids.append(r.text)
        content = 'Mannen myten legenden.2'
        r = requests.post(URL_ROOT + "comment", json={'content': content, 'parent': None, 'post_id': post_id},
                          headers={'Authorization': self.__class__.token})
        comment_ids.append(r.text)
        content = 'Mannen myten legenden.3'
        r = requests.post(URL_ROOT + "comment", json={'content': content, 'parent': None, 'post_id': post_id},
                          headers={'Authorization': self.__class__.token})
        comment_ids.append(r.text)
        r = requests.post(URL_ROOT + "comment", json={'content': content, 'parent': comment_id, 'post_id': post_id},
                          headers={'Authorization': self.__class__.token})
        comment_ids.append(r.text)
        comment_id = r.text
        r = requests.post(URL_ROOT + "comment", json={'content': content, 'parent': comment_id, 'post_id': post_id},
                          headers={'Authorization': self.__class__.token})
        comment_ids.append(r.text)

        # React to post and comment
        reaction = '1'
        requests.post(URL_ROOT + "comment/react", json={'comment_id': comment_id, 'reaction': reaction},
                      headers={'Authorization': self.__class__.token})
        requests.post(URL_ROOT + "post/reactions", json={'post_id': post_id, 'reaction': reaction},
                      headers={'Authorization': self.__class__.token})

        r = requests.get(URL_ROOT + "post/extras/" + post_id)

    def test_5_unauthorization(self):
        if self.__class__.token is None:
            self.test_0_login()

        # Create a post
        content = 'Mannen myten legenden.'
        r = requests.post(URL_ROOT + "post", json={'content': content},
                          headers={'Authorization': self.__class__.token})
        post_id = get_field(r, 'response')

        # Test logging out
        r = requests.post(URL_ROOT + 'user/logout',  headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)

        # Test commenting, reacting and posting when logged out
        r = requests.post(URL_ROOT + "comment", json={'content': content, 'parent': None, 'post_id': post_id},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 401)
        r = requests.post(URL_ROOT + "post/reactions", json={'post_id': post_id, 'reaction': 1},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 401)
        r = requests.post(URL_ROOT + "post", json={'content': content},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 401)

        # Login as a new user
        r = requests.post(URL_ROOT + "user", json={'username': 'test_user2', 'email': 'alfons@gmail.com',
                                                   'password': 'password123'})
        print("User with id: " + get_field(r, 'user_id') + " created.")
        r = requests.post(URL_ROOT + "user/login", json={'email': 'alfons@gmail.com', 'password': 'password123'})
        new_token = self.__class__.token = "Bearer " + get_field(r, 'token')

        # Try deleting the post as a new user
        r = requests.delete(URL_ROOT + "post/" + post_id,
                            headers={'Authorization': new_token})
        self.assertEqual(r.status_code, 403)

        # Try reacting to the post as a new user
        r = requests.post(URL_ROOT + "post/reactions", json={'post_id': post_id, 'reaction': 1},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)


def get_field(response, field):
    res = json.loads(response.text)
    return res.get(field)


def parse_plain_response(response):
    return response[1:len(response)-2]


if __name__ == '__main__':
    print("Starting tests")
    unittest.main()
