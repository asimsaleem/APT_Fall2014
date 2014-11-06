import cloudstorage as gcs
import os
import urllib
import cgi
import urlparse

from google.appengine.api import users
from google.appengine.ext import ndb
from google.appengine.ext.webapp import template
from google.appengine.ext import blobstore
from google.appengine.ext.webapp import blobstore_handlers
from google.appengine.api import app_identity
from google.appengine.api.images import get_serving_url
from google.appengine.api import mail
from google.appengine.api import search
from google.appengine.datastore.datastore_query import Cursor
from google.appengine.api import images
#from googlemaps import GoogleMaps
from random import randint

import jinja2
import webapp2
import logging
import json
import math
import random

from datetime import datetime
from datetime import timedelta

#Internal Files
import storagemodel
import screenhandler


######################################################################
INDEX_NAME = 'streamsearch'
INDEX_TYPEAHEAD = 'autocomplete'

DEFAULT_STREAM_NAME = "default_stream_name"
def stream_key(stream_name=DEFAULT_STREAM_NAME):
	return ndb.Key('Stream', stream_name)

my_default_retry_params = gcs.RetryParams(initial_delay=0.2,
                                          max_delay=5.0,
                                          backoff_factor=2,
                                          max_retry_period=15)

gcs.set_default_retry_params(my_default_retry_params)

def default_bucket_name():
	return os.environ.get('BUCKET_NAME', app_identity.get_default_gcs_bucket_name())

DEFAULT_IMG_FILE_NAME = "default_img_file_name" #Don't do it for now
def blobstore_key(imgFileName):
	#Blobstore API requires extra /gs to distinguish against blobstore files
	blobstore_img_filename = "/gs/" + imgFileName
	return blobstore.create_gs_key(blobstore_img_filename)
######################################################################


######################################################################
class CreateStreamRequestHandler:

	def __init__(self, input_json_data):
		self.input_json_data = input_json_data

	def processCreateStream(self):

		status = ""
		logging.debug("Inside the method to Process Create Stream")
		print "JSON", self.input_json_data
		parsed_json = json.loads(self.input_json_data)
		logging.debug("Parsed JSON is %s", parsed_json)

		#Read the Contents of the JSON Object into Local Variables
		input_stream_id = parsed_json['streamId']
		input_stream_name = parsed_json['streamName']
		input_subscribers = parsed_json['subscriberList']
		input_tags = parsed_json['tagList']
		input_cover_img_url = parsed_json['coverImgUrl']
		input_invite_msg = parsed_json['inviteMsg']
		input_owner_email = parsed_json['ownerEmail']
		input_owner_nickname = parsed_json['ownerNickname']

		self.input_stream_name = input_stream_name

		logging.debug("Input Stream Name Being Processed is: %s", input_stream_name)

		#Execute Query to check if Record exists already in the NDB
		existing_stream_query = storagemodel.Stream.query(storagemodel.Stream.streamId == input_stream_id)
		logging.debug("Executing the Query to Check if Record already exists with the specified Stream Name...")
		existing_stream = existing_stream_query.fetch()
		if existing_stream:
			logging.debug("Record Already Exists with the Provide Stream Name: %s", input_stream_name)
			status = "ERROR"
		else: 
			#Create an Entry for the stream in the NDB
			stream = storagemodel.Stream(parent=stream_key(input_stream_id))

			stream.streamId = input_stream_id
			stream.streamName = input_stream_name
			stream.subscriberList = input_subscribers.split(',')
			stream.tagList = input_tags.split(',')
			stream.coverImgUrl = input_cover_img_url
			stream.inviteMsg = input_invite_msg
			stream.viewCount = 0 #Initializing the View Counter to 0
			stream.pictureCount = 0 #Initialize the Pic Counter to 0
			stream.owner = input_owner_email
			#stream.streamAccessDate - This will be automatically updated every time the link is clicked

			#TODO: This might need to be moved and then made part of the JSON Input
			logging.info("Stream Owner is: %s", users.get_current_user())
			stream.put()   #Inserting into the NDB now


			#Add an Entry for the User in the Scheduler table too with the Default as None
			user_email_query = storagemodel.UserSchedulerStream.query(storagemodel.UserSchedulerStream.ownerEmailId == str(input_owner_email))
			user_email_data = user_email_query.fetch()
			logging.debug("Number of Owner Stream Records returned: %s", len(user_email_data))

			if not user_email_data:
				logging.debug("Empty. So add the Users Email Address to the USer Scheduler Table")
				user_email_schedule = storagemodel.UserSchedulerStream(ownerEmailId = str(input_owner_email))
				user_email_schedule.ownerNickname = str(input_owner_nickname)
				user_email_schedule.emailSchedule = "none"
				logging.debug("Adding default User Schedule to the Table")
				user_email_schedule.put()
			else:
				logging.debug("Not Empty. User already available in the table. Don't update the details")


			#Create or Update the Index
			logging.debug("Setting up the Index name as SearchStream")
			index = search.Index(name=INDEX_NAME)
			logging.debug("Setting the Fields and the Data into the Index")
			fields = [
						search.TextField(name='STREAM_ID', value=input_stream_id),
						search.TextField(name='STREAM_NAME', value=input_stream_name),
						search.TextField(name='TAG_LIST', value=input_tags),
						search.TextField(name='SUBSCRIBER_LIST', value=input_subscribers),
						search.TextField(name='COVER_IMG_URL', value=input_cover_img_url),
						search.TextField(name='OWNER', value=input_owner_email)
					 ]

			#Create a Document to Index using the Data set into the Fields above
			logging.debug("Setting up Data to create the Document With the streamNme as the Primary Key as it will be unique")
			d = search.Document(doc_id=input_stream_id, fields=fields)

			#Add the Document to the Index
			logging.debug("Adding the Document to the Index now....")
			try:
	  			add_result = search.Index(name=INDEX_NAME).put(d)
			except search.Error:
	 			logging.debug("Error Encountered while adding to the Index")


	 		#Add an Entry into the INDEX_TYPEAHEAD index too for the Typeahead feature
			index = search.Index(name=INDEX_TYPEAHEAD)
			doc_id = input_stream_id
			name = ','.join(CreateStreamRequestHandler.tokenize_autocomplete(self))
			document = search.Document(doc_id=doc_id,fields=[search.TextField(name='name', value=name)])
			index.put(document)


			#Redirect to the ManageStream screen after Put
			#self.redirect("/manageStream")
			status = "SUCCESS"

		logging.debug("Status of the Creation Process is %s", status)

		#Create a JSON Object to Return the Status 
		output_json_data = json.dumps({
							"status" : status
						   })

		return output_json_data

	def tokenize_autocomplete(self):

		phrase = self.input_stream_name.replace(" ", '')
		print "Phrase is", phrase

		a = []
		for word in phrase.split():
			j = 1
			while True:
				for i in range(len(word) - j + 1):
					a.append(word[i:i + j])
				if j == len(word):
					break
				j += 1
		print "A returned is: ", a
		return a
#####################################################################


######################################################################
#Use this to process the Delete and Unsubscribe options
class ManageStreamRequestHandler:

	def __init__(self, input_json_data):
		self.input_json_data = input_json_data

	def getManagedStreamData(self):

		parsed_json = json.loads(self.input_json_data)
		logging.debug("Parsed JSON is %s", parsed_json)

		#Read the Contents of the JSON Object into Local Variables
		input_owner_email = parsed_json['ownerEmail']

		#Streams I own query
		user = users.get_current_user()
		streams_owner_query = storagemodel.Stream.query(storagemodel.Stream.owner == str(input_owner_email))
		logging.debug("User Details is: %s", input_owner_email)
		my_streams = streams_owner_query.fetch()
		logging.debug("Number of Owner Stream Records returned: ", len(my_streams))

		
		my_stream_list = []
		if my_streams:
			for my_stream in my_streams:
				my_stream_dict = {}
				my_stream_dict["streamId"] = my_stream.streamId
				my_stream_dict["streamName"] = my_stream.streamName
				my_stream_dict["coverImgUrl"] = my_stream.coverImgUrl
				my_stream_dict["inviteMsg"] = my_stream.inviteMsg
				my_stream_dict["owner"] = my_stream.owner
				my_stream_dict["pictureCount"] = my_stream.pictureCount
				my_stream_dict["viewCount"] = my_stream.viewCount
				if my_stream.pictureUpdateDate: 
					my_stream_dict["pictureUpdateDate"] = str(my_stream.pictureUpdateDate.strftime('%m/%d/%Y'))
				if my_stream.streamAccessDate:
					my_stream_dict["streamAccessDate"] = str(my_stream.streamAccessDate.strftime('%m/%d/%Y'))
				my_stream_dict["subscriberList"] = my_stream.subscriberList
				my_stream_dict["tagList"] = my_stream.tagList
				my_stream_list.append(my_stream_dict)

		logging.debug("My Streams: List of Items in My Stream are : %s", my_stream_list)


		#Streams I subscribe query
		
		subscriberEmail = str(input_owner_email)
		logging.debug("Email Id I used is: %s", subscriberEmail)
		streams_subscribed_query = storagemodel.Stream.query(ndb.OR(storagemodel.Stream.subscriberList == subscriberEmail))
		subscribed_streams = streams_subscribed_query.fetch()
		logging.debug("Number of Subscribed Stream Records returned: %s", len(subscribed_streams))
		subscribed_stream_list = []
		if subscribed_streams:
			for subscribed_stream in subscribed_streams:
				subscribed_stream_dict = {}
				subscribed_stream_dict["streamId"] = subscribed_stream.streamId
				subscribed_stream_dict["streamName"] = subscribed_stream.streamName
				subscribed_stream_dict["coverImgUrl"] = subscribed_stream.coverImgUrl
				subscribed_stream_dict["inviteMsg"] = subscribed_stream.inviteMsg
				subscribed_stream_dict["owner"] = subscribed_stream.owner
				subscribed_stream_dict["pictureCount"] = subscribed_stream.pictureCount
				subscribed_stream_dict["viewCount"] = subscribed_stream.viewCount
				if subscribed_stream.pictureUpdateDate:
					subscribed_stream_dict["pictureUpdateDate"] = str(subscribed_stream.pictureUpdateDate.strftime('%m/%d/%Y'))
				if subscribed_stream.streamAccessDate:
					subscribed_stream_dict["streamAccessDate"] = str(subscribed_stream.streamAccessDate.strftime('%m/%d/%Y'))
				subscribed_stream_dict["subscriberList"] = subscribed_stream.subscriberList
				subscribed_stream_dict["tagList"] = subscribed_stream.tagList
				subscribed_stream_list.append(subscribed_stream_dict)
		

		logging.debug("Size of Owned Streams: %s", len(my_stream_list))
		logging.debug("Size of Subscriber Streams: %s", len(subscribed_stream_list))

		output_json_data = json.dumps({
										"owned_streams" : my_stream_list,
										"subscribed_streams" :  subscribed_stream_list
									  })
		
		return output_json_data
######################################################################

###################################################################
class UserRequestHandler:

	def __init__(self, input_json_data):
		self.input_json_data = input_json_data

	def processCreateUser(self):
		logging.debug("Inside the Method to Create the User")

		parsed_json = json.loads(self.input_json_data)
		logging.debug("Parsed JSON is %s", parsed_json)

		#Read the Contents of the JSON Object into Local Variables
		user_id = parsed_json['userId']
		user_pwd = parsed_json['userPwd']

		logging.debug("User id : %s", user_id)
		logging.debug("User Pwd: %s", user_pwd)

		user_details = storagemodel.UserDetailsStream(userId = str(user_id))
		user_details.user_pwd = str(user_pwd)
		logging.debug("Adding new User Details to the Table")
		user_details.put()


	def verifyUserCredentails(self):

		logging.debug("Verify Credentials for the Provided details")
		logging.debug("Inside the Method to Create the User")

		parsed_json = json.loads(self.input_json_data)
		logging.debug("Parsed JSON is %s", parsed_json)

		#Read the Contents of the JSON Object into Local Variables
		user_id = parsed_json['userId']
		user_pwd = parsed_json['userPwd']

		logging.debug("User id : %s", user_id)
		logging.debug("User Pwd: %s", user_pwd)

		user_details_query = storagemodel.UserDetailsStream.query(storagemodel.UserDetailsStream.userId == str(user_id))
		logging.debug("Executing the Query to verify if the User Details exists???")
		user_details_result = user_details_query.fetch()
		logging.debug("Number of Records returned is %s", user_details_result)
		if user_details_result:
			logging.debug("User Details Exists")
			for user in user_details_result:
				userPwd = user.userPwd
				
			if userPwd == user_pwd:
				logging.debug("Password MATCH too.....SUCCESSFULLY Validated")
				credentials_status = "SUCCESS"
			else:
				logging.debug("Password does not MATCH. Validation Failed")
				credentials_status = "FAILURE"
		else:
			logging.debug("No User Details Exists")
			credentials_status = "FAILURE"
		'''
		output_json_data = json.dumps({
										"status" : credentials_status,
									  })
		'''
		logging.debug("CRedentials Status is %s", credentials_status)
		return credentials_status


###################################################################


###################################################################
class ViewSingleStreamRequestHandler:
	
	def __init__(self, input_json_data):
		self.input_json_data = input_json_data

	def displaySingleStreamContent(self):

		logging.debug("Inside the method to displaySingleStreamContent<><><><><><>")

		parsed_json = json.loads(self.input_json_data)
		logging.debug("Parsed JSON is %s", parsed_json)

		#Read the Contents of the JSON Object into Local Variables
		input_stream_id = parsed_json['streamId']
		cursor_value = parsed_json['cursorValue']

		#Make a call to the Stream NDB to increment the View Count
		view_stream_count_query = storagemodel.Stream.query(storagemodel.Stream.streamId == input_stream_id)
		logging.debug("Executing the Query to retrieve the Image Count")
		view_stream = view_stream_count_query.fetch()
		logging.debug("Number of Records returned is %s", view_stream)

		viewCount = 0
		for stream in view_stream:
			viewCount = view_stream[0].viewCount
			viewCount = viewCount + 1
		
		print "View Count is:", viewCount
		logging.debug("View Count to Update is: %s", viewCount)
		view_stream[0].viewCount = viewCount
		view_stream[0].put() #Update the Entry in NDB
		
		#Implementation of Forward Cursor Logic
		logging.debug("Implementing the Cursors Logic")
		curs = Cursor(urlsafe=cursor_value)
		uploaded_imgs_query = storagemodel.PhotoStream.query(storagemodel.PhotoStream.streamId == input_stream_id).order(-storagemodel.PhotoStream.pictureUpdateDate)
		uploaded_photos, next_curs, more = uploaded_imgs_query.fetch_page(16, start_cursor=curs)
		logging.debug("No. of Records Returned is %s", len(uploaded_photos))
		
		cursor_value = ""
		if more and next_curs:
			logging.debug("More Records are available. Setting the next cursor value for the button")
			cursor_value = next_curs.urlsafe()
			logging.debug("Cursor Value is %s", cursor_value)


		#Get the already uploaded images - Old Logic
		imgUrlList = []
		for uploaded_photo in uploaded_photos:
			logging.debug("Uploaded Photo List is: %s", uploaded_photo.photoList)
			for photo in uploaded_photo.photoList:
				imgSourceUrl = get_serving_url(photo)
				imgUrlList.append(imgSourceUrl) # + "=s32-c"

		#Create a List of URLs using the stored blobKeys
		logging.debug("Image Urls are: %s", imgUrlList)

		output_json_data = json.dumps({
										"streamId" : input_stream_id,
										"streamName" : input_stream_id,
										"uploadedImgsUrl" :  imgUrlList,
										"cursorValue" : cursor_value, 
									  })

		return output_json_data

###################################################################

######################################################################
class UploadImageToStreamRequestHandler(blobstore_handlers.BlobstoreUploadHandler):

	def post(self, streamName):

		logging.debug("After Uploading Image to the GCS")
		input_stream_name = streamName

		#Formulate the input_Stream-id here
		input_stream_id = input_stream_name.replace(" ", "")
		logging.debug("Input Stream Id Is: %s", input_stream_id)

		upload_files = self.get_uploads('file')  # 'file' is file upload field in the form
		#logging.debug("UPLOAD FILE IS: %s"  + upload_files)
		blob_info = upload_files[0]
		blob_key = blob_info.key()

		logging.debug("Default Bucket Name is--------------->%s", default_bucket_name())
		logging.debug("Blob Key Retrieved is: %s", blob_key)
		
		logging.debug("STREAM NAME IS: %s", streamName)
		photoStream = storagemodel.PhotoStream(parent=stream_key(input_stream_id))

		photoStream.streamId = input_stream_id
		photoStream.streamName = input_stream_name
		photoList = []
		photoList.append(blob_key)
		photoStream.photoList = photoList

		logging.debug("Stream Owner is: %s", photoStream)
		photoStream.put()

		#Get the Picture count from the PhotoStream NDB
		logging.debug("Executing the Pic Count Query....")
		pic_count_query = storagemodel.PhotoStream.query(storagemodel.PhotoStream.streamId == input_stream_id)
		pic_count_stream = pic_count_query.fetch()
		logging.debug("Number of Records returned is %s", len(pic_count_stream))
		picCount = len(pic_count_stream)
		logging.debug("Current Pic Count is %s", picCount)

		#Make a call to the Stream NDB to increment the View Count
		view_stream_count_query = storagemodel.Stream.query(storagemodel.Stream.streamId == input_stream_id)
		logging.debug("Executing the Query to retrieve the Image Count")
		view_stream = view_stream_count_query.fetch()
		logging.debug("Number of Records returned is %s", view_stream)
		logging.debug("View Count to Update is: %s", picCount)
		#Create an Entry for the stream in the NDB
		view_stream[0].pictureCount = picCount
		view_stream[0].pictureUpdateDate = datetime.now().replace(microsecond = 0)
		view_stream[0].put()

		logging.debug("Redirecting to the single stream page....")
		#self.redirect("/viewSingleStream?streamName=" + input_stream_name)
######################################################################


###################################################################
class ViewAllStreamRequestHandler:
	
	def __init__(self, input_json_data):
		self.input_json_data = input_json_data

	def displayAllStreamContent(self):

		#Get the already uploaded images 
		all_streams_query = storagemodel.Stream.query().order(-storagemodel.Stream.streamCreateDate)
		logging.debug("Executing the Image Query")

		all_streams = all_streams_query.fetch(100)
		logging.debug("List of Items in the Stream are: %s", all_streams)

		all_stream_list = []
		if all_streams:
			for all_stream in all_streams:
				all_stream_dict = {}
				all_stream_dict["streamId"] = all_stream.streamId
				all_stream_dict["streamName"] = all_stream.streamName
				all_stream_dict["coverImgUrl"] = all_stream.coverImgUrl
				all_stream_dict["inviteMsg"] = all_stream.inviteMsg
				all_stream_dict["owner"] = all_stream.owner
				all_stream_dict["pictureCount"] = all_stream.pictureCount
				all_stream_dict["viewCount"] = all_stream.viewCount
				all_stream_dict["pictureUpdateDate"] = str(all_stream.pictureUpdateDate)
				all_stream_dict["streamAccessDate"] = str(all_stream.streamAccessDate)
				all_stream_dict["subscriberList"] = all_stream.subscriberList
				all_stream_dict["tagList"] = all_stream.tagList
				all_stream_list.append(all_stream_dict)

		output_json_data = json.dumps({
								"allStreams" : all_stream_list
							  })
			
		return output_json_data
#####################################################################



###################################################################
class SearchStreamRequestHandler:

	def __init__(self, input_json_data):
		self.input_json_data = input_json_data


	def searchAllStreamContent(self):
		
		parsed_json = json.loads(self.input_json_data)
		logging.debug("Parsed JSON is %s", parsed_json)

		#Read the Contents of the JSON Object into Local Variables
		searchTerm = parsed_json['searchTerm']
		logging.debug("Search Term: %s", searchTerm)

		#Search for the Search Term in the Stream name and Tags
		#Get the already uploaded images 
		logging.debug("Executing the Search Query")
		result_stream_id_list = []
		try:
  			index = search.Index(INDEX_NAME)
  			search_results = index.search(searchTerm)
  			returned_count = len(search_results.results)
  			number_found = search_results.number_found
  			logging.debug("Returned Count is %s", returned_count)
  			logging.debug("Number Found is %s", number_found)
  	
  			if not search_results:
  				logging.debug("Search Results were Empty")
  			else:
	  			for doc in search_results:
	  				logging.debug("Doc Returned is %s", doc)
	  				doc_id = doc.doc_id
	  				result_stream_id_list.append(doc_id)
	  				fields = doc.fields
	  				logging.debug("Doc Id is: %s", doc_id)
	  				logging.debug("Fields is: %s", fields)

  		except search.Error:
  			logging.debug("Error encountered while attempting to retrieve records for search term %s", searchTerm)


  		searched_streams = []
  		if not result_stream_id_list:
  			logging.debug("No Results. So We can set the rest to EMPTY")
  		else:
  			#Execute a query using the various stream names that have been returned
  			for stream_id in result_stream_id_list:
  				logging.debug("Stream Name value returned is: %s", stream_id)
  				search_stream_query = storagemodel.Stream.query(storagemodel.Stream.streamId == stream_id)
				logging.debug("Executing the Query to retrieve the Stream...")
				search_stream = search_stream_query.fetch()
				if search_stream:
					searched_streams.append(search_stream[0])
				else:
					logging.debug("Search Results Found but there was no stream of that type")


		search_stream_list = []
		if searched_streams:
			for search_stream in searched_streams:
				search_stream_dict = {}
				search_stream_dict["streamId"] = search_stream.streamId
				search_stream_dict["streamName"] = search_stream.streamName
				search_stream_dict["coverImgUrl"] = search_stream.coverImgUrl
				search_stream_dict["inviteMsg"] = search_stream.inviteMsg
				search_stream_dict["owner"] = search_stream.owner
				search_stream_dict["pictureCount"] = search_stream.pictureCount
				search_stream_dict["pictureUpdateDate"] = str(search_stream.pictureUpdateDate)
				search_stream_dict["streamAccessDate"] = str(search_stream.streamAccessDate)
				search_stream_dict["subscriberList"] = search_stream.subscriberList
				search_stream_dict["tagList"] = search_stream.tagList
				search_stream_list.append(search_stream_dict)


		results_count = len(search_stream_list)
		logging.debug("Number or Records in the Count: %s", results_count)
		output_json_data = json.dumps({
								'searchedStreams' : search_stream_list,
								'resultsCount' : results_count,
								'searchTerm' : searchTerm,
							  })
		logging.debug("Returning the OUTPUT JSON DATA: %s", output_json_data)
		return output_json_data
	
###################################################################


###################################################################
class NearbyStreamRequestHandler:

	def __init__(self, input_json_data):
		self.input_json_data = input_json_data

	def getAllNearbyStreamContent(self):
		
		parsed_json = json.loads(self.input_json_data)
		logging.debug("NearbyStreamRequestHandler: Parsed JSON is %s", parsed_json)

		#Read the Contents of the JSON Object into Local Variables
		current_latitude = parsed_json['current_lat']
		current_longitude = parsed_json['current_lon']
		searchTerm = parsed_json['searchTerm']

		logging.debug("NearbyStreamRequestHandler: Current Latitude is: %s", current_latitude)
		logging.debug("NearbyStreamRequestHandler: Current Longitude is: %s", current_longitude)
		logging.debug("NearbyStreamRequestHandler: Search Term: %s", searchTerm)

		#Search for the Search Term in the Stream name and Tags
		#Get the already uploaded images 
		logging.debug("NearbyStreamRequestHandler: Executing the Search Query")
		result_stream_id_list = []
		try:
  			index = search.Index(INDEX_NAME)
  			search_results = index.search(searchTerm)
  			returned_count = len(search_results.results)
  			number_found = search_results.number_found
  			logging.debug("NearbyStreamRequestHandler: Returned Count is %s", returned_count)
  			logging.debug("NearbyStreamRequestHandler: Number Found is %s", number_found)
  	
  			if not search_results:
  				logging.debug("NearbyStreamRequestHandler: Search Results were Empty")
  			else:
	  			for doc in search_results:
	  				logging.debug("NearbyStreamRequestHandler: Doc Returned is %s", doc)
	  				doc_id = doc.doc_id
	  				result_stream_id_list.append(doc_id)
	  				fields = doc.fields
	  				logging.debug("NearbyStreamRequestHandler:Doc Id is: %s", doc_id)
	  				logging.debug("NearbyStreamRequestHandler:Fields is: %s", fields)

  		except search.Error:
  			logging.debug("NearbyStreamRequestHandler:Error encountered while attempting to retrieve records for search term %s", searchTerm)


  		searched_streams = []
  		if not result_stream_id_list:
  			logging.debug("NearbyStreamRequestHandler:No Results. So We can set the rest to EMPTY")
  		else:
  			#Execute a query using the various stream names that have been returned
  			for stream_id in result_stream_id_list:
  				logging.debug("NearbyStreamRequestHandler:Stream Name value returned is: %s", stream_id)
  				search_stream_query = storagemodel.Stream.query(storagemodel.Stream.streamId == stream_id)
				logging.debug("NearbyStreamRequestHandler:Executing the Query to retrieve the Stream...")
				search_stream = search_stream_query.fetch()
				if search_stream:
					searched_streams.append(search_stream[0])
				else:
					logging.debug("NearbyStreamRequestHandler:Search Results Found but there was no stream of that type")


		search_stream_list = []
		if searched_streams:
			for search_stream in searched_streams:
				search_stream_dict = {}
				search_stream_dict["streamId"] = search_stream.streamId
				search_stream_dict["streamName"] = search_stream.streamName
				search_stream_dict["coverImgUrl"] = search_stream.coverImgUrl
				search_stream_dict["inviteMsg"] = search_stream.inviteMsg
				search_stream_dict["owner"] = search_stream.owner
				search_stream_dict["pictureCount"] = search_stream.pictureCount
				search_stream_dict["pictureUpdateDate"] = str(search_stream.pictureUpdateDate)
				search_stream_dict["streamAccessDate"] = str(search_stream.streamAccessDate)
				search_stream_dict["subscriberList"] = search_stream.subscriberList
				search_stream_dict["tagList"] = search_stream.tagList
				#TODO: Calculate the distance based on the value of the Current location passed in and a random val here
				search_stream_dict["distance"] = random.randrange(100, 1000, 2)
				search_stream_list.append(search_stream_dict)


		results_count = len(search_stream_list)
		logging.debug("NearbyStreamRequestHandler:Number or Records in the Count: %s", results_count)
		output_json_data = json.dumps({
								'searchedStreams' : search_stream_list,
								'resultsCount' : results_count,
								'searchTerm' : searchTerm,
							  })
		logging.debug("NearbyStreamRequestHandler: Returning the OUTPUT JSON DATA: %s", output_json_data)
		return output_json_data
	
###################################################################


###################################################################
class SearchTypeAheadRequestHandler:

	def __init__(self, input_json_data):
		self.input_json_data = input_json_data


	def fetchTypeAheadSuggestions(self):

		logging.debug("INSIDE THE SearchTypeAheadRequestHandler Method")
		logging.debug("JSON FROM REQUEST IS: %s", self.input_json_data)
		search_type_ahead_stream_json = self.input_json_data
		parsed_json = json.loads(search_type_ahead_stream_json)

		typeahead_term = parsed_json['typeaheadterm']

		#Search In the Doc Index
		index = search.Index(name=INDEX_TYPEAHEAD)
		typeahead_results = index.search(typeahead_term)
		print "Type Ahead Results are: ", typeahead_results

		typeahead_results_list = []
		for result in typeahead_results:
			doc_id = result.doc_id
			typeahead_results_list.append(doc_id)

		print "TypeAhead List is ", typeahead_results_list

		output_json_data = json.dumps({
								'typeaheadterms' : typeahead_results_list,
							  })

		return output_json_data

###################################################################




###################################################################
class TrendingStreamRequestHandler:

	def __init__(self, input_json_data):
		self.input_json_data = input_json_data

	def loadTrendingStreams(self):

		#Make a call to the Stream NDB to increment the View Count
		trending_stream_count_query = storagemodel.TrendingStream.query().order(-storagemodel.TrendingStream.viewCount)
		trending_streams = trending_stream_count_query.fetch()
		logging.debug("Number of Records returned is %s", trending_streams)


		trending_stream_list = []
		if trending_streams:
			for trending_stream in trending_streams:
				trending_stream_dict = {}
				trending_stream_dict["streamId"] = trending_stream.streamId
				trending_stream_dict["streamName"] = trending_stream.streamName
				trending_stream_dict["coverImgUrl"] = trending_stream.coverImgUrl
				trending_stream_dict["viewCount"] = trending_stream.viewCount
				trending_stream_list.append(trending_stream_dict)


		output_json_data = json.dumps({
								'trendingStreams' : trending_stream_list
							})
				
		return output_json_data

###################################################################


#####################################################################
class DeleteStreamRequestHandler:

	def __init__(self, input_json_data):
		self.input_json_data = input_json_data

	def deleteStream(self):
		logging.debug("INSIDE THE DeleteStreamRequestHandler Method")
		logging.debug("JSON FROM REQQUEST IS: %s", self.input_json_data)
		delete_stream_list_json = self.input_json_data
		parsed_json = json.loads(delete_stream_list_json)

		user = users.get_current_user()

		stream_name_list = parsed_json['streamNames']
		stream_id_list = []
		for stream_name in stream_name_list:
			stream_id_list.append(stream_name.replace(" ", ""))


		logging.debug("Stream Name List Loop Start ")
		for delete_stream_id in stream_id_list:
			logging.debug ("Item is: %s", delete_stream_id) 
			delete_stream_query = storagemodel.Stream.query(storagemodel.Stream.streamId == delete_stream_id)
			logging.debug("Executing the Query to Delete from the Image Stream...")
			delete_stream = delete_stream_query.fetch()
			
			for stream in delete_stream: 
				stream.key.delete()
				logging.debug("IMage Stream is Deleted")

			photos_delete_stream_query = storagemodel.PhotoStream.query(storagemodel.PhotoStream.streamId == delete_stream_id)
			logging.debug("Executing the Query to Delete from the Photo Stream...")
			photo_delete_stream = photos_delete_stream_query.fetch()
			
			for photo in photo_delete_stream:
				photo.key.delete()
				logging.debug("Photo Delete is done")

		output_json_data = json.dumps({
								"status" : "SUCCESS"
							  })
			
		return output_json_data
#####################################################################

######################################################################
class UnsubscribeStreamRequestHandler:

	def __init__(self, input_json_data):
		self.input_json_data = input_json_data


	def unsubscribeFromStream(self):

		logging.debug("INSIDE THE UnsubscribeStreamRequestHandler Method")
		logging.debug("JSON FROM REQQUEST IS: %s", self.input_json_data)
		unsubscribe_stream_list_json = self.input_json_data
		parsed_json = json.loads(unsubscribe_stream_list_json)

		user = users.get_current_user()

		stream_name_list = parsed_json['streamNames']
		stream_id_list = []
		for stream_name in stream_name_list:
			stream_id_list.append(stream_name.replace(" ", ""))


		logging.debug("Stream Name List Loop Start ")
		for unsubscribe_stream_id in stream_id_list:
			
			logging.debug ("Item is: %s", unsubscribe_stream_id) 
			unsubscribe_stream_query = storagemodel.Stream.query(storagemodel.Stream.streamId == unsubscribe_stream_id)
			logging.debug("Executing the Query to Unsubscribe from the Image Stream...")
			unsubscribe_stream = unsubscribe_stream_query.fetch()
			subscriberList = unsubscribe_stream[0].subscriberList
			logging.debug("Initial Subscriber List: %s", subscriberList)
			logging.debug("User Email Being Processed is: %s", user.email())
			for subscriberCnt in range(len(subscriberList)):
				logging.debug("Subscriber Email is: %s", subscriberList[subscriberCnt])
				if(str(subscriberList[subscriberCnt]) == str(user.email())):
					logging.debug("Subcribed MAtch FOund")
					subscriberList[subscriberCnt] = ""
				else:
					continue

			logging.debug("Updated Subscriber List: %s", subscriberList)
			unsubscribe_stream[0].subscriberList = subscriberList
			unsubscribe_stream[0].put()

		output_json_data = json.dumps({
								"status" : "SUCCESS"
							  })
			
		return output_json_data
######################################################################


######################################################################
class SubscribeStreamRequestHandler:

	def __init__(self, input_json_data):
		self.input_json_data = input_json_data


	def subscribeToStream(self):

		logging.debug("INSIDE THE SubscribeStreamRequestHandler Method")
		logging.debug("JSON FROM REQQUEST IS: %s", self.input_json_data)
		subscribe_stream_list_json = self.input_json_data
		parsed_json = json.loads(subscribe_stream_list_json)

		user = users.get_current_user()

		subscribe_stream_name = parsed_json['streamName']
		logging.debug("Stream Name is: %s ", subscribe_stream_name)
		subscribe_stream_id = subscribe_stream_name.replace(" ", "")

			
		subscribe_stream_query = storagemodel.Stream.query(storagemodel.Stream.streamId == subscribe_stream_id)
		logging.debug("Executing the Query to Unsubscribe from the Image Stream...")
		subscribe_stream = subscribe_stream_query.fetch()
		subscriberList = subscribe_stream[0].subscriberList
		logging.debug("Initial Subscriber List: %s", subscriberList)
		logging.debug("User Email Being Processed is: %s", user.email())
		subscriberList.append(str(user.email()))
		subscriberList = list(set(subscriberList))
	
		logging.debug("Updated Subscriber List: %s", subscriberList)
		subscribe_stream[0].subscriberList = subscriberList
		subscribe_stream[0].put()



		#Add an Entry for the Subscriber in the User Scheduler Also
		user_email_query = storagemodel.UserSchedulerStream.query(storagemodel.UserSchedulerStream.ownerEmailId == str(user.email()))
		user_email_data = user_email_query.fetch()
		logging.debug("Number of Owner Stream Records returned: %s", len(user_email_data))

		if not user_email_data:
			logging.debug("Empty. So add the Users Email Address to the USer Scheduler Table")
			user_email_schedule = storagemodel.UserSchedulerStream(ownerEmailId = str(user.email()))
			user_email_schedule.ownerNickname = str(user.nickname())
			user_email_schedule.emailSchedule = "none"
			logging.debug("Adding default User Schedule to the Table")
			user_email_schedule.put()
		else:
			logging.debug("Not Empty. User already available in the table. Don't update the details")






		output_json_data = json.dumps({
								"status" : "SUCCESS"
							  })
			
		return output_json_data
######################################################################

######################################################################
##This is Executed by the Cron Job so it can remain here
class UpdateTrendingStreamRequestHandler(webapp2.RequestHandler):

	def get(self):
		logging.debug("Inside the Update Trending Stream Request Handler")
		trending_streams_query = storagemodel.Stream.query().order(-storagemodel.Stream.streamAccessDate)
		logging.debug("Executing the Trending Stream Query")

		trending_streams = trending_streams_query.fetch()
		logging.debug("List of Items in the Stream are: %s", trending_streams)

		finalized_trending_streams = []
		for stream in trending_streams:
			savedUpdatedTime = stream.streamAccessDate
			logging.debug("Saved Time is: %s", savedUpdatedTime)
			#currentTime = datetime.now().replace(second=0, microsecond = 0) + timedelta( hours = 2)
			currentTime = datetime.now()
			logging.debug("Current Time is: %s", currentTime)
			if (currentTime - savedUpdatedTime) > timedelta(hours = 1):
				logging.debug("More than an Hour have passed. So ignore this...")
			else:
				logging.debug("Less than an Hour have passed. Use this")
				finalized_trending_streams.append(stream)

			if(len(finalized_trending_streams) == 3):
				logging.debug("3 Streams have been identified for Display. Break from the Loop")
				break;

		
		#Make a call to the Stream NDB to increment the View Count
		#Delete the Records from the TrendStream table first
		delete_trend_stream_query = storagemodel.TrendingStream.query()
		if not delete_trend_stream_query:
			logging.debug("Nothing to Delete")
		else:	
			logging.debug("Executing the Query to Delete from the Trend Stream...")
			delete_stream = delete_trend_stream_query.fetch()
			logging.debug("No. of Trend Stream Records to delete: %s", len(delete_stream))
			for stream in delete_stream: 
				stream.key.delete()
				logging.debug("Trend Stream is Deleted")

		#Add the New Trend Stream
		logging.debug("Finalized Tending Stream is: %s", finalized_trending_streams)
		logging.debug("Size of Finalized Stream is: %s", len(finalized_trending_streams))
		for trend in finalized_trending_streams:
			stream = storagemodel.TrendingStream(parent=stream_key(trend.streamName))	
			stream.streamName = trend.streamName							
			stream.viewCount = trend.viewCount
			stream.coverImgUrl = trend.coverImgUrl
			logging.debug("Inserting view count : %s", trend.viewCount)
			stream.put()
####################################################################

####################################################################
class UpdateEmailSchedulerOptionsRequestHandler:

	def __init__(self, input_json_data):
		self.input_json_data = input_json_data


	def updateEmailSchedule(self):

		logging.debug("INSIDE THE UpdateEmailSchedulerOptionsRequestHandler Method")
		logging.debug("JSON FROM REQUEST IS: %s", self.input_json_data)
		update_email_json = self.input_json_data
		parsed_json = json.loads(update_email_json)

		subscriber_email = parsed_json['email']
		logging.debug("Subscriber Email Is: %s ", subscriber_email)
			
		user_email_query = storagemodel.UserSchedulerStream.query(storagemodel.UserSchedulerStream.ownerEmailId == str(subscriber_email))
		user_email_data = user_email_query.fetch()
		logging.debug("Number of Owner Stream Records returned: ", len(user_email_data))
		if user_email_data:
			option = parsed_json['options']
			logging.debug("Option Selected is: %s", option)
			if option == 'none':
				#Update Email Option to None in the DB
				logging.debug("Option is NONE")
			elif option == 'everyFive':
				#Update Email Option to Every 5 Hours in the DB
				logging.debug("Option is Every Five")
			elif option == 'everyHr':
				#Update Email Option to Every 1 Hour in the DB
				logging.debug("Option is Every Hour")
			elif option == 'everyDay':			
				#Update Email Option to Every Day in the DB
				 logging.debug("Option is Every Day")

			user_email_data[0].emailSchedule = option
			logging.debug("Adding Updated User Schedule to the Table")
			user_email_data[0].put()
			
		logging.debug("Rendering the Trending Stream Page Again")
		output_json_data = json.dumps({
								"status" : "SUCCESS"
							  })
			
		return output_json_data
####################################################################


####################################################################
class InitialEmailOptionsRequestHandler:

	def __init__(self, input_json_data):
		self.input_json_data = input_json_data


	def loadInitialEmailSchedule(self):
		
		logging.debug("Inside the Method to get the Initial Email Options for Prepopulating the screen")
		update_email_json = self.input_json_data
		parsed_json = json.loads(update_email_json)

		subscriber_email = parsed_json['email']
		logging.debug("Subscriber Email Is: %s ", subscriber_email)

		option = "none"
		#Add an Entry for the User in the Scheduler table too with the Default as None
		user_email_query = storagemodel.UserSchedulerStream.query(storagemodel.UserSchedulerStream.ownerEmailId == str(subscriber_email))
		user_email_data = user_email_query.fetch()
		logging.debug("Number of Owner Stream Records returned: %s", len(user_email_data))
		if user_email_data:
			option = user_email_data[0].emailSchedule
			logging.debug("Option Selected is: %s", option)
		
		logging.debug("Attempting to reroute back to Ajax Call")
		output_json_data = json.dumps({
									'schedulerOption' : option
								   })
		logging.debug("Return Data is: %s", output_json_data)
		
		return output_json_data
###################################################################


###################################################################
class SendEMailStreamRequestHandler:

	def __init__(self, input_json_data):
		self.input_json_data = input_json_data

	def get5MinutesEmailSubscriberList(self):

		logging.debug("Inside get5MinutesEmailSubscriberList")
		logging.debug("Check in the DB for all the Users whose Email Preference is 5 Minutes")
		five_min_query = storagemodel.UserSchedulerStream.query(storagemodel.UserSchedulerStream.emailSchedule == "everyFive")
		five_min_user_list = five_min_query.fetch()
		logging.debug("Number of Records returned is %s", len(five_min_user_list))
		logging.debug("Data Returned is: %s", five_min_user_list)

		user_list = []
		for user in five_min_user_list:
			user_dict = {}
			user_dict["ownerEmailId"] = user.ownerEmailId
			user_dict["ownerNickname"] = user.ownerNickname
			user_list.append(user_dict)

		output_json_data = json.dumps({
									'emailList' : user_list
								   })
		return output_json_data


	def getHourlyEmailSubscriberList(self):
		
		logging.debug("Inside the MailHourlyStreamRequestHandler")
		logging.debug("Check in the DB for all the Users whose Email Preference is Hourly")
		hourly_query = storagemodel.UserSchedulerStream.query(storagemodel.UserSchedulerStream.emailSchedule == "everyHr")
		hourly_query_user_list = hourly_query.fetch()
		logging.debug("Number of Records returned is %s", len(hourly_query_user_list))
		logging.debug("Data Returned is: %s", hourly_query_user_list)

		user_list = []
		for user in hourly_query_user_list:
			user_dict = {}
			user_dict["ownerEmailId"] = user.ownerEmailId
			user_dict["ownerNickname"] = user.ownerNickname
			user_list.append(user_dict)


		output_json_data = json.dumps({
									'emailList' : user_list
								   })
		return output_json_data


	def getDailyEmailSubscriberList(self):

		logging.debug("Inside the MailDailyStreamRequestHandler")
		logging.debug("Check in the DB for all the Users whose Email Preference is Daily")
		daily_query = storagemodel.UserSchedulerStream.query(storagemodel.UserSchedulerStream.emailSchedule == "everyDay")
		daily_query_user_list = daily_query.fetch()
		logging.debug("Number of Records returned is %s", len(daily_query_user_list))
		logging.debug("Data Returned is: %s", daily_query_user_list)

		user_list = []
		for user in daily_query_user_list:
			user_dict = {}
			user_dict["ownerEmailId"] = user.ownerEmailId
			user_dict["ownerNickname"] = user.ownerNickname
			user_list.append(user_dict)

		output_json_data = json.dumps({
									'emailList' : user_list
								   })
		return output_json_data
#################################################################

###################################################################
class PicLocationStreamRequestHandler:

	def __init__(self, input_json_data):
		self.input_json_data = input_json_data

	def getPicLocationStream(self):
		pic_list_json = self.input_json_data
		parsed_json = json.loads(pic_list_json)

		stream_name = parsed_json['streamName']
		input_stream_id = stream_name.replace(" ", "")

		json_start_date = parsed_json['startDate']
		json_end_date = parsed_json['endDate']

		print "Stream is ", stream_name, " and Start Date is ", json_start_date, " and End Date is ", json_end_date
		#Wed Jan 01 2014 02:00:00 GMT-0600 (CST)

		tmp_strt_date,time = json_start_date.split("T")
		tmp_end_date,time = json_end_date.split("T")
		start_date = datetime.strptime(tmp_strt_date.replace('"',''), '%Y-%m-%d')
		end_date = datetime.strptime(tmp_end_date.replace('"',''), '%Y-%m-%d')

		print "Start Date is now: ", start_date
		print "End Date is now: ", end_date

		#Execute a Quey on the Database to get all the Pictures corresponding to this stream
		tagged_imgs_query = storagemodel.PhotoStream.query(storagemodel.PhotoStream.streamId == input_stream_id,
															 storagemodel.PhotoStream.pictureUpdateDate >= start_date,
															 storagemodel.PhotoStream.pictureUpdateDate <= end_date).order(-storagemodel.PhotoStream.pictureUpdateDate)
		tagged_photos = tagged_imgs_query.fetch()
		logging.debug("No. of Records Returned is %s", len(tagged_photos))
		print "No. of Records returned is: ", len(tagged_photos)
		location_list = []
		for photo in tagged_photos:
			location_dict = {}
			if not photo.latitude:
				logging.debug("Setting Default Latitude")
				location_dict["latitude"] = float(30.429147099999998) + random.random()
			elif photo.latitude == 'undefined':
				logging.debug("Setting Default Latitude")
				location_dict["latitude"] = float(30.429147099999998) + random.random()
			else:
				location_dict["latitude"] = float(photo.latitude) + random.random()
			
			if not photo.longitude:
				logging.debug("Setting Default Longitude")
				location_dict["longitude"] = float(-97.6974627) + random.random()
			elif photo.longitude == 'undefined':
				logging.debug("Setting Default Longitude")
				location_dict["longitude"] = float(-97.6974627) + random.random()
			else:
				location_dict["longitude"] = float(photo.longitude) +  random.random()
			
			location_dict["imgUrl"] = get_serving_url(photo.photoList[0])
			location_list.append(location_dict)

		output_json_data = json.dumps({"markers": location_list})
		print "OUTPUT JSON DATA IS ", output_json_data
		return output_json_data
###################################################################

###################################################################
class UploadMultipleImageToStreamRequestHandler(blobstore_handlers.BlobstoreUploadHandler):

	def post(self,streamName,latitude, longitude):

		logging.debug("After Uploading Image to the GCS")
		print "After uploading the Img to GCS......STREAM NAME", streamName
		input_stream_name = streamName

		#input_latitude = self.request("lat")
		#input_longitude = self.request("lon")
		if not latitude or not longitude:
			logging.debug("NOT Latitude is: %s", latitude, "Longitude %s", longitude)
			input_latitude = 30.429147099999998 	
			input_longitude = -97.6974627
		elif latitude =='undefined' or longitude == 'undefined':
			logging.debug("UNDEFINED Latitude is: %s", latitude, "Longitude %s", longitude)
			input_latitude = 30.429147099999998 	
			input_longitude = -97.6974627
		else:
			logging.debug("YES Latitude is: %s", latitude, "Longitude %s", longitude)
			input_latitude = latitude
			input_longitude = longitude

		print "Latitude is: {", input_latitude, "} and Longitude is {", input_longitude, "}"
		logging.debug("Latitude is: {%s", input_latitude, "} and Longitude is {%s", input_longitude, "}")

		#input_latitude = random.uniform(-180, 180)
		#input_longitude = random.uniform(-180, 180)
		#print "Randomized Latitude is: {", input_latitude, "} and Longitude is {", input_longitude, "}"
		upload = self.get_uploads()[0]
		print "Upload is",upload
		blob_key=upload.key()
		print "Blob key is", blob_key

		#Formulate the input_Stream-id here
		input_stream_id = input_stream_name.replace(" ", "")

		logging.debug("Default Bucket Name is--------------->%s", default_bucket_name())
		logging.debug("Blob Key Retrieved is: %s", blob_key)
		
		logging.debug("STREAM NAME IS: %s", streamName)
		photoStream = storagemodel.PhotoStream(parent=stream_key(input_stream_id))

		photoStream.streamId = input_stream_id
		photoStream.streamName = input_stream_name
		photoStream.latitude = str(input_latitude)
		photoStream.longitude = str(input_longitude)
		photoList = []
		photoList.append(blob_key)
		photoStream.photoList = photoList

		logging.debug("Stream Owner is: %s", photoStream)
		photoStream.put()

		#Get the Picture count from the PhotoStream NDB
		logging.debug("Executing the Pic Count Query....")
		pic_count_query = storagemodel.PhotoStream.query(storagemodel.PhotoStream.streamId == input_stream_id)
		pic_count_stream = pic_count_query.fetch()
		logging.debug("Number of Records returned is %s", len(pic_count_stream))
		picCount = len(pic_count_stream)
		logging.debug("Current Pic Count is %s", picCount)

		#Make a call to the Stream NDB to increment the View Count
		view_stream_count_query = storagemodel.Stream.query(storagemodel.Stream.streamId == input_stream_id)
		logging.debug("Executing the Query to retrieve the Image Count")
		view_stream = view_stream_count_query.fetch()
		logging.debug("Number of Records returned is %s", view_stream)
		logging.debug("View Count to Update is: %s", picCount)
		#Create an Entry for the stream in the NDB
		view_stream[0].pictureCount = picCount
		view_stream[0].pictureUpdateDate = datetime.now().replace(microsecond = 0)
		view_stream[0].put()

		logging.debug("Redirecting to the single stream page....")
		resultsList = []
	 	resultVal = {}
		resultVal['url'] = images.get_serving_url(blob_key,
													secure_url=self.request.host_url.startswith(
			    									'https'
													))
		THUMBNAIL_MODIFICATOR = '=s80'
		resultVal['thumbnailUrl'] = resultVal['url'] + "=s10"
		print "thumbnailUrl", resultVal['thumbnailUrl']
		resultsList.append(resultVal)
		result = {'files': resultsList}
		s = json.dumps(result, separators=(',', ':'))
		if 'application/json' in self.request.headers.get('Accept'):
			self.response.headers['Content-Type'] = 'application/json'

		print "JSON OUTPUT IS", s
		self.response.write(s)

		#self.redirect("/viewSingleStream?streamName=" + input_stream_name)
###################################################################

