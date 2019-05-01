import requests
import unittest
import json
import server

URL_ROOT = "https://tddd80-server.herokuapp.com/"


class TestServerFunctions(unittest.TestCase):

    def test_0_login(self):
        r = requests.post(URL_ROOT + "user", json={'username': 'test_user', 'email': 'begnt@gmail.com',
                                                   'password': 'password123'})
        self.assertEqual(r.status_code, 200)
        print("User with id: " + get_field(r, 'response') + " created.")
        r = requests.post(URL_ROOT + "user/login", json={'emial': 'begnt@gmail.com', 'password': 'password123'})
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

        post_ids = list()
        # Test creating a post.
        content = 'Mannen myten legenden.'
        r = requests.post(URL_ROOT + "/post", json={'content': content},
                          headers={'Authorization': self.token})
        self.assertEqual(r.status_code, 200)
        post_ids.append(r.text)

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
        post_ids.append(post_id)
        r = requests.get(URL_ROOT + "/post/" + post_id)
        self.assertEqual(r.status_code, 200)
        post = json.loads(r.text)
        self.assertEqual(post.content, content)
        self.assertEqual(post.author_id, self.user_id)
        self.assertEqual(post.id, post_id)

        # Test reacting to post
        reaction = '1'
        r = requests.post(URL_ROOT + "/post/react", json={'post_id': post_id, 'reaction': reaction},
                          headers={'Authorization': self.token})
        self.assertEqual(r.status_code, 200)
        reaction_id = r.text
        r = requests.get(URL_ROOT + "/post/react/" + reaction_id, json={'post_id': post_id, 'reaction': reaction},
                         headers={'Authorization': self.token})
        self.assertEqual(r.status_code, 200)
        self.assertEqual(json.loads(r.text)[0].id, reaction_id)

        # TODO: Need to be able to remove reactions

        # Test deleting all previous posts.
        for post_id in post_ids:
            r = requests.post(URL_ROOT + "/post/delete/" + post_id,
                              headers={'Authorization': self.token})
            self.assertEqual(r.status_code, 200)
            r = requests.get(URL_ROOT + "/post/" + post_id,
                             headers={'Authorization': self.token})
            self.assertEqual(r.status_code, 404)
            # TODO: Should test deleting post with a unauthorized account.

        pass

    def test_3_comment(self):
        if self.token is None:
            self.test_0_login()

        r = requests.post(URL_ROOT + "/post", json={'content': 'Template post.'},
                          headers={'Authorization': self.token})
        self.assertEqual(r.status_code, 200)
        post_id = r.text

        comment_ids = list()
        # Test creating a comment.
        content = 'Mannen myten legenden.'
        r = requests.post(URL_ROOT + "/commment", json={'content': content, 'parent': None, 'post_id': post_id},
                          headers={'Authorization': self.token})
        self.assertEqual(r.status_code, 200)
        comment_ids.append(r.text)

        # Test getting the just created comment.
        comment_id = r.text
        r = requests.get(URL_ROOT + "/comment/" + comment_id)
        self.assertEqual(r.status_code, 200)
        comment = json.loads(r.text)
        self.assertEqual(comment.content, content)
        self.assertEqual(comment.author_id, self.user_id)
        self.assertEqual(comment.id, comment_id)

        # Test that multiple root comments can be created.
        content = 'Mannen som inte är legenden'
        r = requests.post(URL_ROOT + "/commment", json={'content': content, 'parent': None, 'post_id': post_id},
                          headers={'Authorization': self.token})
        self.assertEqual(r.status_code, 200)
        comment_id = r.text
        comment_ids.append(comment_id)
        r = requests.get(URL_ROOT + "/comment/" + comment_id)
        self.assertEqual(r.status_code, 200)
        comment = json.loads(r.text)
        self.assertEqual(comment.content, content)
        self.assertEqual(comment.author_id, self.user_id)
        self.assertEqual(comment.id, comment_id)

        # Test that comment chain works.
        parent_id = comment_id
        content = 'Mannen som inte är barn'
        r = requests.post(URL_ROOT + "/commment", json={'content': content, 'parent': parent_id, 'post_id': post_id},
                          headers={'Authorization': self.token})
        self.assertEqual(r.status_code, 200)
        comment_id = r.text
        comment_ids.append(comment_id)
        r = requests.get(URL_ROOT + "/comment/" + comment_id)
        self.assertEqual(r.status_code, 200)
        comment = json.loads(r.text)
        self.assertEqual(comment.content, content)
        self.assertEqual(comment.author_id, self.user_id)
        self.assertEqual(comment.id, comment_id)
        r = requests.get(URL_ROOT + "/comments/chain/" + parent_id)
        self.assertEqual(r.status_code, 200)
        comments = json.loads(r.text)
        self.assertEqual(comments[0].id, comment_id)

        # Test reacting to comment
        reaction = '1'
        r = requests.post(URL_ROOT + "/comment/react", json={'comment_id': comment_id, 'reaction': reaction},
                          headers={'Authorization': self.token})
        self.assertEqual(r.status_code, 200)
        r = requests.get(URL_ROOT + "/comment/" + comment_id)
        self.assertEqual(r.status_code, 200)
        self.assertEqual(json.loads(r.text).upvotes, 1)

        # TODO: Need to be able to remove reactions

        # Test deleting all previous comments.
        for comment_id in comment_ids:
            r = requests.post(URL_ROOT + "/comment/delete/" + comment_id,
                              headers={'Authorization': self.token})
            self.assertEqual(r.status_code, 200)
            r = requests.get(URL_ROOT + "/comments/" + comment_id,
                             headers={'Authorization': self.token})
            self.assertEqual(r.status_code, 404)
            r = requests.post(URL_ROOT + "/post/delete/" + post_id,
                              headers={'Authorization': self.token})
            self.assertEqual(r.status_code, 200)
            # TODO: Should test deleting comments with a unauthorized account.
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
