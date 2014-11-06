import unittest
import logging

import webapp2
import json
import cloudstorage as gcs
import os
import urllib
import cgi
import urlparse

from google.appengine.api import users
from google.appengine.api import app_identity
from google.appengine.api.images import get_serving_url
from google.appengine.api import search
from google.appengine.datastore.datastore_query import Cursor

from datetime import datetime
from datetime import timedelta

from google.appengine.api import memcache
from google.appengine.ext import ndb
from google.appengine.ext import db
from google.appengine.ext import testbed
from google.appengine.ext import blobstore
from google.appengine.api import mail
from google.appengine.ext.webapp import blobstore_handlers
from google.appengine.datastore import datastore_stub_util



def stream_key(streamId="XXX"):
    return ndb.Key('Stream', streamId )

def blobstore_key(imgFileName="default_img_file_name"):
  blobstore_img_filename = "/gs/" + imgFileName
  return blobstore.create_gs_key(blobstore_img_filename)

def default_bucket_name():
  return os.environ.get('BUCKET_NAME', app_identity.get_default_gcs_bucket_name())


class StreamModel(ndb.Model):

  streamId = ndb.StringProperty(indexed=True)
  streamName = ndb.StringProperty(indexed=True)
  subscriberList = ndb.StringProperty(repeated=True, indexed=True)
  tagList = ndb.StringProperty(repeated=True, indexed=True)
  coverImgUrl = ndb.StringProperty(indexed=False)
  inviteMsg = ndb.StringProperty(indexed=False)
  viewCount = ndb.IntegerProperty()
  pictureCount = ndb.IntegerProperty()
  owner = ndb.StringProperty(indexed=True)
  pictureUpdateDate = ndb.DateTimeProperty(indexed=True)
  streamAccessDate = ndb.DateTimeProperty(auto_now=True, indexed=True)

def GetEntityViaMemcache(entity_key):
  """Get entity from memcache if available, from datastore if not."""
  entity = memcache.get(entity_key)
  if entity is not None:
    return entity
  entity = TestModel.get(entity_key)
  if entity is not None:
    memcache.set(entity_key, entity)
  return entity

class StreamTestCase(unittest.TestCase):

  def setUp(self):
    self.testbed = testbed.Testbed()
    self.testbed.activate()
    self.testbed.init_datastore_v3_stub()
    self.testbed.init_memcache_stub()

  def tearDown(self):
    self.testbed.deactivate()


  #Create Stream Test
  def testCreateStream(self):
    streamDtls = StreamModel(parent=stream_key("abc"))
    streamDtls.viewCount = 100
    streamDtls.inviteMsg = "TEST MESSAGE"
    streamDtls.put()

    stream_query = StreamModel.query(ancestor=stream_key("abc")).order(-StreamModel.streamAccessDate)
    streamRespData = stream_query.fetch(10)
    self.assertEqual(1, len(streamRespData))
  
  
  #View Single Stream Test
  def testViewSingleStream(self):
    streamDtls = StreamModel(parent=stream_key("xyz"))
    streamDtls.viewCount = 100
    streamDtls.streamName = "XYZ"
    streamDtls.inviteMsg = "TEST MESSAGE 1"
    streamDtls.put()
   
    streamDtls = StreamModel(parent=stream_key("zzz"))
    streamDtls.viewCount = 100
    streamDtls.streamName = "ZZZ"
    streamDtls.inviteMsg = "TEST MESSAGE 2"
    streamDtls.put()

    query = StreamModel.query(ancestor=stream_key("zzz"))
    results = query.fetch(10)
    print "Length of Results:", len(results)
    for result in results:
        print "RESULT VALUE IS: {", result, "}"
    self.assertEqual(1, len(results))
    self.assertEqual("ZZZ", results[0].streamName)

  #View All Stream Test
  def testViewAllStream(self):
    streamDtls = StreamModel(parent=stream_key("abc"))
    streamDtls.viewCount = 100
    streamDtls.streamName = "abc"
    streamDtls.inviteMsg = "TEST MESSAGE 1"
    streamDtls.put()

    streamDtls = StreamModel(parent=stream_key("def"))
    streamDtls.viewCount = 100
    streamDtls.streamName = "def"
    streamDtls.inviteMsg = "TEST MESSAGE 2"
    streamDtls.put()

    streamDtls = StreamModel(parent=stream_key("ghi"))
    streamDtls.viewCount = 100
    streamDtls.streamName = "ghi"
    streamDtls.inviteMsg = "TEST MESSAGE 3"
    streamDtls.put()

    results = StreamModel.query().fetch(10)
    print "Length of Results:", len(results)
    for result in results:
        print "RESULT VALUE IS: {", result, "}"
    
    self.assertEqual(3, len(results))
    self.assertEqual("abc", results[0].streamName)
    self.assertEqual("def", results[1].streamName)
    self.assertEqual("ghi", results[2].streamName)


 #Create Stream Test
  def testDeleteStream(self):
    streamDtls = StreamModel(parent=stream_key("abc"))
    streamDtls.viewCount = 100
    streamDtls.inviteMsg = "TEST MESSAGE"
    streamDtls.put()

    stream_query = StreamModel.query(ancestor=stream_key("abc")).order(-StreamModel.streamAccessDate)
    streamRespData = stream_query.fetch(10)
    self.assertEqual(1, len(streamRespData))

    delete_stream_query = StreamModel.query(ancestor=stream_key("abc"))
    delete_stream = delete_stream_query.fetch()
    print "Lenght of Delete Stream is ", len(delete_stream)
    self.assertEqual(1, len(delete_stream))

    for stream in delete_stream: 
       stream.key.delete()
       logging.debug("IMage Stream is Deleted")

    delete_stream = delete_stream_query.fetch()
    self.assertEqual(0, len(delete_stream))

  #Subscribe Stream Test
  def testSubscribeStream(self):

    streamDtls = StreamModel(parent=stream_key("subscribe_stream_id"))
    streamDtls.viewCount = 100
    streamDtls.streamId = "subscribe_stream_id"
    streamDtls.inviteMsg = "TEST MESSAGE"
    streamDtls.put()

    subscribe_stream_query = StreamModel.query(StreamModel.streamId == "subscribe_stream_id")
    subscribe_stream = subscribe_stream_query.fetch()
    subscriberList = subscribe_stream[0].subscriberList
    subscriberList.append(str("XXX"))
    subscribe_stream[0].subscriberList = subscriberList
    subscribe_stream[0].put()

    updated_subscribe_stream_query = StreamModel.query(StreamModel.streamId == "subscribe_stream_id")
    updated_subscribe_stream = updated_subscribe_stream_query.fetch()
    updated_subscriberList = updated_subscribe_stream[0].subscriberList

    self.assertEqual(len(subscriberList),len(updated_subscriberList))

  #Subscribe Stream Test
  def testUnsubscribeStream(self):

    streamDtls = StreamModel(parent=stream_key("subscribe_stream_id"))
    streamDtls.viewCount = 100
    streamDtls.streamId = "unsubscribe_stream_id"
    streamDtls.inviteMsg = "TEST MESSAGE"
    streamDtls.subscriberList = ["subscriberList"]
    streamDtls.put()

    unsubscribe_stream_query = StreamModel.query(StreamModel.streamId == "unsubscribe_stream_id")
    unsubscribe_stream = unsubscribe_stream_query.fetch()
    subscriberList = unsubscribe_stream[0].subscriberList
    if(str(subscriberList[0]) == "subscriberList"):
        subscriberList[0] = ""
 
    logging.debug("Updated Subscriber List: %s", subscriberList)
    unsubscribe_stream[0].subscriberList = subscriberList
    unsubscribe_stream[0].put()


    updated_unsubscribe_stream_query = StreamModel.query(StreamModel.streamId == "unsubscribe_stream_id")
    logging.debug("Executing the Query to Unsubscribe from the Image Stream...")
    updated_unsubscribe_stream = updated_unsubscribe_stream_query.fetch()
    updated_subscriberList = updated_unsubscribe_stream[0].subscriberList

    self.assertEqual("",updated_subscriberList[0])



#Mail Test Case
class MailTestCase(unittest.TestCase):

  def setUp(self):
    self.testbed = testbed.Testbed()
    self.testbed.activate()
    self.testbed.init_mail_stub()
    self.mail_stub = self.testbed.get_stub(testbed.MAIL_SERVICE_NAME)

  def tearDown(self):
    self.testbed.deactivate()

  def testMailSent(self):
    mail.send_mail(to='alice@example.com',
                   subject='This is a test',
                   sender='bob@example.com',
                   body='This is a test e-mail')
    messages = self.mail_stub.get_sent_messages(to='alice@example.com')
    self.assertEqual(1, len(messages))
    self.assertEqual('alice@example.com', messages[0].to)


#Blobstore Test Case
class BlobstoreTestCase(unittest.TestCase):
 
  def setUp(self):
    self.testbed = testbed.Testbed()
    self.testbed.activate()
    self.testbed.init_blobstore_stub()
   #self.mail_stub = self.testbed.get_stub(testbed.MAIL_SERVICE_NAME)

  def tearDown(self):
    self.testbed.deactivate()

  def testBlostoreImageTest(self):

    initialBucketName = default_bucket_name()
    upload_url = blobstore.create_upload_url('/uploadImg/' + "input_stream_id", gs_bucket_name=default_bucket_name())
    print "Upload URL is: ", upload_url

    self.assertEqual("http://testbed.example.com/_ah/upload/agpjb25uZXh1c3AxciILEhVfX0Jsb2JVcGxvYWRTZXNzaW9uX18YgICAgICAgAoM", upload_url)


#Document Index Test Case for Search Purposes
class DocumentIndexTestCase(unittest.TestCase):

  def setUp(self):
    self.testbed = testbed.Testbed()
    self.testbed.activate()
    self.testbed.init_all_stubs()

  def tearDown(self):
    self.testbed.deactivate()


  def testDocumentIndexTest(self):
      INDEX_NAME = 'streamsearch'
      index = search.Index(name=INDEX_NAME)

      logging.debug("Setting the Fields and the Data into the Index")
      fields = [
            search.TextField(name='STREAM_ID', value="input_stream_id"),
            search.TextField(name='STREAM_NAME', value="input_stream_name"),
            search.TextField(name='TAG_LIST', value="input_tags"),
           ]

      d = search.Document(doc_id="input_stream_id", fields=fields)
      add_result = search.Index(name=INDEX_NAME).put(d)

      print "Add result is ", add_result
      search_results = index.search("input_stream_name")
      returned_count = len(search_results.results)
      self.assertEqual(1, returned_count)
 


if __name__ == '__main__':
    unittest.main()