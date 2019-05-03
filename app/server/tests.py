import requests
import unittest
import json

URL_ROOT = "http://127.0.0.1:5000/"
#URL_ROOT = "https://tddd80-server.herokuapp.com/"


class TestServerFunctions(unittest.TestCase):

    token = ""
    user_id = ""

    def test_0_login(self):
        r = requests.post(URL_ROOT + "user", json={'username': 'test_user', 'email': 'bengt@gmail.com',
                                                   'password': 'password123'})
        self.assertEqual(r.status_code, 200)
        print("User with id: " + get_field(r, 'user_id') + " created.")
        r = requests.post(URL_ROOT + "user/login", json={'email': 'bengt@gmail.com', 'password': 'password123'})
        self.assertEqual(r.status_code, 200)
        self.__class__.token = "Bearer " + get_field(r, 'token')
        self.__class__.user_id = get_field(r, 'user_id')
        print("Logged in with jwt token: " + self.__class__.token)

    def test_1_feed(self):
        if self.__class__.token is None:
            self.test_0_login()
        requests.get(URL_ROOT + "/feed/", json={'username': 'test_user', 'password': 'password123'})

    def test_2_post(self):
        if self.__class__.token is None:
            self.test_0_login()

        post_ids = list()
        # Test creating a post.
        content = 'Mannen myten legenden.'
        r = requests.post(URL_ROOT + "post", json={'content': content},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        post_ids.append(get_field(r, 'response'))

        # Test getting the just created post.
        post_id = get_field(r, 'response')
        r = requests.get(URL_ROOT + "post/" + post_id)
        self.assertEqual(r.status_code, 200)
        post = json.loads(r.text)
        self.assertEqual(post.get('content'), content)
        self.assertEqual(post.get('author_id'), self.__class__.user_id)
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
        self.assertEqual(post.get('author_id'), self.__class__.user_id)
        self.assertEqual(post.get('id'), post_id)

        # Test reacting to post
        reaction = 1
        r = requests.post(URL_ROOT + "post/react", json={'post_id': post_id, 'reaction': reaction},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        self.assertEqual(reaction, get_field(r, 'own_reaction'))

        # Test changing reaction
        reaction = 3
        r = requests.post(URL_ROOT + "post/react", json={'post_id': post_id, 'reaction': reaction},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        self.assertEqual(reaction, get_field(r, 'own_reaction'))

        # Test removing reaction
        r = requests.post(URL_ROOT + "post/react", json={'post_id': post_id, 'reaction': reaction},
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
        post_id = r.text

        comment_ids = list()
        # Test creating a comment.
        content = 'Mannen myten legenden.'
        r = requests.post(URL_ROOT + "comment", json={'content': content, 'parent': None, 'post_id': post_id},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        comment_ids.append(r.text)

        # Test getting the just created comment.
        comment_id = r.text
        r = requests.get(URL_ROOT + "/comment/" + comment_id)
        self.assertEqual(r.status_code, 200)
        comment = json.loads(r.text)
        self.assertEqual(comment.content, content)
        self.assertEqual(comment.author_id, self.__class__.user_id)
        self.assertEqual(comment.id, comment_id)

        # Test that multiple root comments can be created.
        content = 'Mannen som inte är legenden'
        r = requests.post(URL_ROOT + "/commment", json={'content': content, 'parent': None, 'post_id': post_id},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        comment_id = r.text
        comment_ids.append(comment_id)
        r = requests.get(URL_ROOT + "/comment/" + comment_id)
        self.assertEqual(r.status_code, 200)
        comment = json.loads(r.text)
        self.assertEqual(comment.content, content)
        self.assertEqual(comment.author_id, self.__class__.user_id)
        self.assertEqual(comment.id, comment_id)

        # Test that comment chain works.
        parent_id = comment_id
        content = 'Mannen som inte är barn'
        r = requests.post(URL_ROOT + "/commment", json={'content': content, 'parent': parent_id, 'post_id': post_id},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        comment_id = r.text
        comment_ids.append(comment_id)
        r = requests.get(URL_ROOT + "/comment/" + comment_id)
        self.assertEqual(r.status_code, 200)
        comment = json.loads(r.text)
        self.assertEqual(comment.content, content)
        self.assertEqual(comment.author_id, self.__class__.user_id)
        self.assertEqual(comment.id, comment_id)
        r = requests.get(URL_ROOT + "/comments/chain/" + parent_id)
        self.assertEqual(r.status_code, 200)
        comments = json.loads(r.text)
        self.assertEqual(comments[0].id, comment_id)

        # Test reacting to comment
        reaction = '1'
        r = requests.post(URL_ROOT + "/comment/react", json={'comment_id': comment_id, 'reaction': reaction},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        r = requests.get(URL_ROOT + "/comment/" + comment_id)
        self.assertEqual(r.status_code, 200)
        self.assertEqual(json.loads(r.text).upvotes, 1)

        # Test changing reaction
        reaction = '-1'
        r = requests.post(URL_ROOT + "comment/react", json={'comment_id': comment_id, 'reaction': reaction},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        self.assertEqual(reaction, get_field(r, 'reactions'))

        # Test removing reaction
        r = requests.post(URL_ROOT + "comment/react", json={'comment_id': comment_id, 'reaction': reaction},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)
        self.assertEqual('null', get_field(r, 'reactions'))

        # Test deleting all previous comments.
        for comment_id in comment_ids:
            r = requests.post(URL_ROOT + "/comment/delete/" + comment_id,
                              headers={'Authorization': self.__class__.token})
            self.assertEqual(r.status_code, 200)
            r = requests.get(URL_ROOT + "/comments/" + comment_id,
                             headers={'Authorization': self.__class__.token})
            self.assertEqual(r.status_code, 404)
            r = requests.post(URL_ROOT + "/post/delete/" + post_id,
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
        requests.post(URL_ROOT + "/comment/react", json={'comment_id': comment_id, 'reaction': reaction},
                      headers={'Authorization': self.__class__.token})
        requests.post(URL_ROOT + "post/react", json={'post_id': post_id, 'reaction': reaction},
                      headers={'Authorization': self.__class__.token})

        r = requests.get(URL_ROOT + "/post/extras/" + post_id)
        post_with_extras = json.loads(r)

    def test_5_feed(self):
        pass

    def test_6_unauthorization(self):
        if self.__class__.token is None:
            self.test_0_login()

        # Create a post
        content = 'Mannen myten legenden.'
        r = requests.post(URL_ROOT + "post", json={'content': content},
                          headers={'Authorization': self.__class__.token})
        post_id = get_field(r, 'response')

        # Test logging out
        r = requests.get(URL_ROOT + '/user/logout')
        self.assertEqual(r.status_code, 200)

        # Test commenting, reacting and posting when logged out
        r = requests.post(URL_ROOT + "comment", json={'content': content, 'parent': None, 'post_id': post_id},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 401)
        r = requests.post(URL_ROOT + "post/react", json={'post_id': post_id, 'reaction': 1},
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
        self.assertEqual(r.status_code, 401)

        # Try reacting to the post as a new user
        r = requests.post(URL_ROOT + "post/react", json={'post_id': post_id, 'reaction': 1},
                          headers={'Authorization': self.__class__.token})
        self.assertEqual(r.status_code, 200)


def get_field(response, field):
    res = json.loads(response.text)
    return res.get(field)


if __name__ == '__main__':
    print("Starting tests")
    unittest.main()