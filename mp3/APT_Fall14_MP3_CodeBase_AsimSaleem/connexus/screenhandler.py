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
from google.appengine.datastore.datastore_query import Cursor


import jinja2
import webapp2
import logging
import json

from datetime import datetime
from datetime import timedelta


#Internal Files
import storagemodel
import requesthandler 

#from requesthandler import *

JINJA_ENVIRONMENT = jinja2.Environment(
	loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
	extensions=['jinja2.ext.autoescape'],
	autoescape=True)

######################################################################
INDEX_NAME = 'streamsearch'

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
	#blobstore_img_filename = "/gs" + imgFileName
	#Blobstore API requires extra /gs to distinguish against blobstore files
	blobstore_img_filename = "/gs/" + imgFileName
	return blobstore.create_gs_key(blobstore_img_filename)
######################################################################



######################################################################
#Login Handler will be used to control the Gmail Login for the User
class LoginScreenHandler(webapp2.RequestHandler):

    def get(self):
    	logging.debug("Inside the Login Screen Handler")
        user = users.get_current_user()
        nick_name = ""
        login_url = ""
        logout_url = ""
        if user:
            nick_name = user.nickname()
            logging.debug("Nick Name is: %s", nick_name)
            logout_url = users.create_logout_url('/')
            logging.debug("Logout URL is: %s", logout_url)  
        else:
            login_url = users.create_login_url('/')
            logging.debug("Login URL is: %s", login_url)

        template_values = {
        					'login_user_nickname' : nick_name,
        					'logout_url' : logout_url,
        					'login_url' : login_url,
        				  }

        if logout_url: #Indicates that the User Logged in 
			#template = JINJA_ENVIRONMENT.get_template("template/manageStream.html")
			self.redirect("/manageStream")
        elif login_url:
			template = JINJA_ENVIRONMENT.get_template("template/login.html")
			self.response.write(template.render(template_values))


	'''
	def post(self):

		logging.debug("Inside POST Method for Login Handler....")
		userId = self.request.get("userId")
    	userPwd = self.request.get("userPwd")
    	#cgi.escape(self.request.get('content')
    	logging.debug("User Id = %s", userId)
    	logging.debug("User Pwd= %s", userPwd)
    	logging.debug("Creating the Input JSON object")
    	#Create a JSON Object using the Info read from the Post Command
    	input_json_data =  json.dumps(
 								{
 								 "userId" : userId,
      							 "userPwd" : userPwd,
      							},
   								sort_keys=True,
   								indent=4, 
							    separators=(',', ': ')
  								)
    	logging.debug("About to invoke the Request Handler for the Create User Functionality....")
    	createUserRequestHandler = requesthandler.CreateUserRequestHandler(input_json_data)
    	output_json_data = createUserRequestHandler.processCreateUser()
    '''

######################################################################

######################################################################
class LoginMobileHandler(webapp2.RequestHandler):

	def get(self):
		logging.debug("Inside Login Mobile Handler....")
		userId = self.request.get("userId")
		userPwd = self.request.get("userPwd")
		logging.debug("User Id: %s", userId)
		logging.debug("User Pwd %s", userPwd)

		if userId == '':
			logging.debug("User Id is Missing")
			status_msg = "UIDM" #User Id Missing
		elif userPwd == '':
			logging.debug("Pwd is Missing")
			status_msg = "PWDM" #Password Missing
		else:
			input_json_data =  json.dumps({
	 								 "userId" : userId,
	      							 "userPwd" : userPwd,
	      							})
			logging.debug("About to invoke the Request Handler for the Validate User Functionality....")
			userRequestHandler = requesthandler.UserRequestHandler(input_json_data)
			status_msg = userRequestHandler.verifyUserCredentails()

		logging.debug("Output Status_MSG is %s", status_msg)
		#Invoke Request Handler to check if the User Id exists in the Table
		#If yes, then return SUCCESS
		output_json_data =  json.dumps({
							 "statusMsg" : status_msg,
							})
		self.response.write(output_json_data)
######################################################################



######################################################################
#Create Stream is where the User would create a New Stream
class CreateStreamScreenHandler(webapp2.RequestHandler):

	logging.getLogger().setLevel(logging.DEBUG)
	def get(self):	

		template = JINJA_ENVIRONMENT.get_template("template/createStream.html")
		self.response.write(template.render())



#Create Stream is where the User would create a New Stream
class CreateStreamScreenRequestHandler(webapp2.RequestHandler):

	logging.getLogger().setLevel(logging.DEBUG)
	
	def post(self):	

		input_stream_Name = self.request.get("streamName")
		input_stream_id = input_stream_Name.replace(" ", "")
		input_subscribers = self.request.get("subscribers")
		input_stream_tags = self.request.get("tagStream")
		input_invite_msg = self.request.get("inviteMsg")
		input_cover_img_url = self.request.get("coverImgUrl")

		input_owner_email = ""
		input_owner_nickname = ""
		if users.get_current_user():
			input_owner_email = users.get_current_user().email()
			input_owner_nickname = users.get_current_user().nickname()
		else:
			self.redirect(users.create_login_url(self.request.uri))


		#Create a JSON Object using the Info read from the Post Command
		input_json_data =  json.dumps(
 								{
 								 "streamId" : input_stream_id,
      							 "streamName" : input_stream_Name,
      							 "subscriberList"  : input_subscribers,
      							 "tagList" : input_stream_tags,
      							 "inviteMsg" : input_invite_msg,
      							 "coverImgUrl" : input_cover_img_url,
      							 "ownerEmail" : input_owner_email,
      							 "ownerNickname" : input_owner_nickname
    							},
   								sort_keys=True,
   								indent=4, 
							    separators=(',', ': ')
  								)

		logging.debug("INPUT JSON DATA: %s", input_json_data)

		#After Creating the JSON Data. Pass it to the Request Handler for further Processing
		logging.debug("About to invoke the Request Handler for the CreateStream Functionality....")
		createStreamRequestHandler = requesthandler.CreateStreamRequestHandler(input_json_data)
		output_json_data = createStreamRequestHandler.processCreateStream()

		logging.debug("Output JSON Data is %s", output_json_data)

		parsed_output_json  = json.loads(output_json_data)

		if parsed_output_json['status'] == "SUCCESS":
			#Redirect to the ManageStream screen after Put
			logging.debug("Redirecting to the Manage Page")
			self.redirect("/manageStream")
		elif parsed_output_json['status'] == "ERROR":
			logging.debug("Redirecting to the Error Page")
			self.redirect("/errorStream")
######################################################################


######################################################################
#Manage Stream is used to display the list of Streams available to the logged in User
class ManageStreamScreenHandler(webapp2.RequestHandler):

	def get(self): 

		logging.debug("Inside the ManageStream Handler Method..")
		owner_email = users.get_current_user().email()
		logging.debug("Owner Email To be used: %s", owner_email)
		
		#Create a JSON Object using the Info read from the Post Command
		input_json_data =  json.dumps(
									  {
      							 		"ownerEmail" : owner_email
      							 	  })

		managedStreamRequestHandler = requesthandler.ManageStreamRequestHandler(input_json_data)
		output_json_data = managedStreamRequestHandler.getManagedStreamData()

		parsed_output_json = json.loads(output_json_data)

		logging.debug("JSON Object Output as is: %s", output_json_data)
		logging.debug("Parsed Output Json Data is: %s", parsed_output_json)

		my_streams = parsed_output_json['owned_streams']
		subscribed_streams = parsed_output_json['subscribed_streams']

		logging.debug("My Stream is: %s", my_streams)
		logging.debug("Sub Stream is: %s", subscribed_streams)

		#Data Sent to the UI
		template_values = {
							'my_streams' : my_streams,
							'subscribed_streams' : subscribed_streams, 
						  }

		template = JINJA_ENVIRONMENT.get_template("template/manageStream.html")
		self.response.write(template.render(template_values))
######################################################################


######################################################################
class ManageStreamMobileScreenHandler(webapp2.RequestHandler):

	def get(self): 

		logging.debug("Inside the ManageStream Handler Method..")
		#owner_email = users.get_current_user().email()
		owner_email = self.request.get("userEmail")
		logging.debug("Owner Email To be used: %s", owner_email)
		
		#Create a JSON Object using the Info read from the Post Command
		input_json_data =  json.dumps(
									  {
      							 		"ownerEmail" : owner_email
      							 	  })

		managedStreamRequestHandler = requesthandler.ManageStreamRequestHandler(input_json_data)
		output_json_data = managedStreamRequestHandler.getManagedStreamData()
		logging.debug("Manages Stream Response Data is %s", output_json_data)
		self.response.write(output_json_data)


		'''
		parsed_output_json = json.loads(output_json_data)

		logging.debug("JSON Object Output as is: %s", output_json_data)
		logging.debug("Parsed Output Json Data is: %s", parsed_output_json)

		my_streams = parsed_output_json['owned_streams']
		subscribed_streams = parsed_output_json['subscribed_streams']

		logging.debug("My Stream is: %s", my_streams)
		logging.debug("Sub Stream is: %s", subscribed_streams)

		#Data Sent to the UI
		template_values = {
							'my_streams' : my_streams,
							'subscribed_streams' : subscribed_streams, 
						  }

		template = JINJA_ENVIRONMENT.get_template("template/manageStream.html")
		self.response.write(template.render(template_values))
		'''

######################################################################

#Manage Stream is used to display the list of Streams available to the logged in User
class MySubscribedStreamMobileHandler(webapp2.RequestHandler):

	def get(self): 

		logging.debug("Inside the MySubscribedStreamMobileHandler Handler Method..")
		owner_email =  self.request.get("userEmail") #users.get_current_user().email()
		logging.debug("Owner Email To be used: %s", owner_email)
		
		#Create a JSON Object using the Info read from the Post Command
		input_json_data =  json.dumps(
									  {
      							 		"ownerEmail" : owner_email
      							 	  })

		managedStreamRequestHandler = requesthandler.ManageStreamRequestHandler(input_json_data)
		output_json_data = managedStreamRequestHandler.getManagedStreamData()

		logging.debug("My Subscribed Stream Handler Returning JSON %s", output_json_data)

		self.response.write(output_json_data)
######################################################################



######################################################################
#View Single Stream is used to display a Single Stream to the User
class ViewSingleStreamScreenHandler(webapp2.RequestHandler):

	def get(self): 

		input_stream_name = self.request.get("streamName")
		input_stream_id = input_stream_name.replace(" ", "")
	
		logging.debug("My Stream Name is: %s", input_stream_name)
		print 'Stream Name is......>>>>>>>', input_stream_name
		cursor_value = self.request.get("cursor")

		#Create a JSON Object using the Info read from the Post Command
		input_json_data =  json.dumps(
									  {
									  	"streamId" : input_stream_id,
       							 		"streamName" : input_stream_name,
      							 		"cursorValue" : cursor_value
      							 	  })

		print "JSON Data is", input_json_data

		logging.debug("Input JSON Data is %s", input_json_data)
		viewSingleStreamRequestHandler = requesthandler.ViewSingleStreamRequestHandler(input_json_data)
		output_json_data = viewSingleStreamRequestHandler.displaySingleStreamContent()
		logging.debug("Output JSON Data is %s", output_json_data)

		parsed_output_json = json.loads(output_json_data)
		logging.debug("Parsed Output JSON Data is %s", parsed_output_json)

		#upload_url = parsed_output_json["uploadUrl"]
		input_stream_name = parsed_output_json["streamName"]
		img_url_list = parsed_output_json["uploadedImgsUrl"]
		cursor_value = parsed_output_json["cursorValue"]
	
		template_values = {
					'streamName' : input_stream_name,
					#'upload_url' : upload_url,
					'uploaded_imgs_url' : img_url_list,
					'cursorValue' : cursor_value 
				  }


		template = JINJA_ENVIRONMENT.get_template("template/viewSingleStream.html")
		self.response.write(template.render(template_values))
######################################################################


######################################################################
#View Single Stream is used to display a Single Stream to the User
class ViewSingleStreamMobileHandler(webapp2.RequestHandler):

	def get(self): 
		logging.debug("ViewSingleStreamMobileHandler - START")
		input_stream_id = self.request.get("streamId")
	
		logging.debug("My Stream ID is: %s", input_stream_id)
		cursor_value = self.request.get("cursor")
		logging.debug("My cursor is: %s", cursor_value)

		#Create a JSON Object using the Info read from the Post Command
		input_json_data =  json.dumps({
									  	"streamId" : input_stream_id,
       							 		"cursorValue" : cursor_value
      							 	  })

		print "JSON Data is", input_json_data

		logging.debug("Input JSON Data is %s", input_json_data)
		viewSingleStreamRequestHandler = requesthandler.ViewSingleStreamRequestHandler(input_json_data)
		output_json_data = viewSingleStreamRequestHandler.displaySingleStreamContent()
		logging.debug("Output JSON Data is %s", output_json_data)
		logging.debug("ViewSingleStreamMobileHandler - END")

		self.response.write(output_json_data)
######################################################################

######################################################################
#View All Streams is used to Display all the streams
class ViewAllStreamScreenHandler(webapp2.RequestHandler):

	def get(self): 

		#Get the already uploaded images 
		logging.debug("Executing the Image Query")

		#Create a JSON Object using the Info read from the Post Command
		input_json_data =  json.dumps(
									  {
      							 		"jsonData" : ""
      							 	  })

		viewAllStreamRequestHandler = requesthandler.ViewAllStreamRequestHandler(input_json_data)
		output_json_data = viewAllStreamRequestHandler.displayAllStreamContent()
		logging.debug("Output JSON Data is %s", output_json_data)

		parsed_output_json = json.loads(output_json_data)
		logging.debug("Parsed Output JSON Data is %s", parsed_output_json)

		all_streams = parsed_output_json["allStreams"]
		logging.debug("Parsed OUTPUT Json is %s", all_streams)
		template_values = {
					'all_streams' : all_streams,
					}

		template = JINJA_ENVIRONMENT.get_template("template/viewAllStream.html")
		#self.response.write(template.render(template_values))
		self.response.write(output_json_data)
#####################################################################


#####################################################################
#Search Stream
class SearchStreamScreenHandler(webapp2.RequestHandler):

	def get(self): 

		template = JINJA_ENVIRONMENT.get_template("template/searchStream.html")
		self.response.write(template.render())



#Create Stream is where the User would create a New Stream
class SearchStreamScreenRequestHandler(webapp2.RequestHandler):

	def post(self):

		searchTerm = self.request.get("searchTerm")
		logging.debug("SearchStreamScreenRequestHandler Search Term: %s", searchTerm)

		#Create a JSON Object using the Info read from the Post Command
		input_json_data =  json.dumps(
									  {
	  							 		"searchTerm" : searchTerm
	  							 	  })

		searchStreamRequestHandler = requesthandler.SearchStreamRequestHandler(input_json_data)
		output_json_data = searchStreamRequestHandler.searchAllStreamContent()
		logging.debug("SearchStreamScreenRequestHandler Output JSON Data is %s", output_json_data)

		parsed_output_json = json.loads(output_json_data)
		logging.debug("SearchStreamScreenRequestHandler Parsed Output JSON Data is %s", parsed_output_json)

		searched_streams = parsed_output_json["searchedStreams"]
		results_count = parsed_output_json["resultsCount"]
		search_term = parsed_output_json["searchTerm"]
		logging.debug("SearchStreamScreenRequestHandler Parsed OUTPUT Json is %s", searched_streams)

		template_values = {
					'searched_streams' : searched_streams,
					'results_count' : results_count,
					'search_term' : search_term,
					}

		#If Successful, redirect to the Management Page
		template = JINJA_ENVIRONMENT.get_template("template/searchStream.html")
		self.response.write(template.render(template_values))
#####################################################################

#####################################################################
#Create Stream is where the User would create a New Stream
class SearchStreamMobileHandler(webapp2.RequestHandler):

	def post(self):

		searchTerm = self.request.get("searchTerm")
		logging.debug("SearchStreamMobileHandler Search Term: %s", searchTerm)

		#Create a JSON Object using the Info read from the Post Command
		input_json_data =  json.dumps({
	  							 		"searchTerm" : searchTerm
	  							 	  })

		searchStreamRequestHandler = requesthandler.SearchStreamRequestHandler(input_json_data)
		output_json_data = searchStreamRequestHandler.searchAllStreamContent()
		logging.debug("SearchStreamMobileHandler Output JSON Data is %s", output_json_data)
		self.response.write(output_json_data)
#####################################################################


#####################################################################
#Create Stream is where the User would create a New Stream
class NearbyStreamMobileHandler(webapp2.RequestHandler):

	def post(self):

		searchTerm = self.request.get("searchTerm")
		logging.debug("NearbyStreamMobileHandler Search Term: %s", searchTerm)
		current_latitude = self.request.get("cLat")
		current_longitude = self.request.get("cLon")

		logging.debug("NearbyStreamMobileHandler Current Latitude is: %s", current_latitude)
		logging.debug("NearbyStreamMobileHandler Current Longitude is: %s", current_longitude)


		#Create a JSON Object using the Info read from the Post Command
		input_json_data =  json.dumps({
	  							 		"current_lat" : current_latitude,
	  							 		"current_lon" : current_longitude,
	  							 		"searchTerm" : searchTerm
	  							 	  })

		nearbyStreamRequestHandler = requesthandler.NearbyStreamRequestHandler(input_json_data)
		output_json_data = nearbyStreamRequestHandler.getAllNearbyStreamContent()
		logging.debug("NearbyStreamMobileHandler Output JSON Data is %s", output_json_data)
		self.response.write(output_json_data)
#####################################################################


#####################################################################
class SearchTypeAheadStreamScreenHandler(webapp2.RequestHandler):

	def get(self):
		searchTerm = self.request.get("term")
		print "Inside the Search Type ahead method with the Search Term", searchTerm

 		#Create a JSON Object using the Info read from the Post Command
		input_json_data =  json.dumps(
									  {
      							 		"typeaheadterm" : searchTerm
      							 	  })

		#Make a Call to the Request Handler Now to provide the Search Results
		searchTypeAheadRequestHandler = requesthandler.SearchTypeAheadRequestHandler(input_json_data)
		output_json_data = searchTypeAheadRequestHandler.fetchTypeAheadSuggestions()
		print "Output JSON Data is", output_json_data
		#self.response.headers['Content-Type'] = 'application/json'   
		logging.debug("Output JSON Data is %s", output_json_data)

		parsed_output_json = json.loads(output_json_data)
		logging.debug("Parsed Output JSON Data is %s", parsed_output_json)

		typeahead_streams = parsed_output_json["typeaheadterms"]
		print "typeahead_streams is", typeahead_streams
		typeahead_list = []
		for typeaheadterm in typeahead_streams:
			typeaheadterm_dict = {}
			typeaheadterm_dict["id"] = typeaheadterm
			typeaheadterm_dict["label"] = typeaheadterm
			typeaheadterm_dict["value"] = typeaheadterm
			typeahead_list.append(typeaheadterm_dict)

		self.response.write(json.dumps(typeahead_list))
#####################################################################


#####################################################################
class TrendingStreamScreenHandler(webapp2.RequestHandler):

	def get(self): 
	
		#Create a JSON Object using the Info read from the Post Command
		input_json_data =  json.dumps(
									  {
      							 		"jsonData" : ""
      							 	  })

		trendingStreamRequestHandler = requesthandler.TrendingStreamRequestHandler(input_json_data)
		output_json_data = trendingStreamRequestHandler.loadTrendingStreams()
		logging.debug("Output JSON Data is %s", output_json_data)

		parsed_output_json = json.loads(output_json_data)
		logging.debug("Parsed Output JSON Data is %s", parsed_output_json)

		trending_streams = parsed_output_json["trendingStreams"]
		logging.debug("Parsed OUTPUT Json is %s", trending_streams)

		template_values = {
					'trendingStreams' : trending_streams,
					}
	
		template = JINJA_ENVIRONMENT.get_template("template/trendingStream.html")
		self.response.write(template.render(template_values))


class UpdateEmailSchedulerOptionsScreenHandler(webapp2.RequestHandler):

	def post(self):

		user = users.get_current_user()
		user_email = user.email()
		logging.debug("User Email Being Processed is: %s", user_email)

		if not mail.is_email_valid(user.email()):
			logging.debug("Invalid Email Address %s", user_email)
		else:
			logging.debug("Valid Email Address %s", user_email)
			option = self.request.get('options')
			#Create a JSON Object using the Info read from the Post Command
			input_json_data =  json.dumps(
									  {
	  							 		"email" : user_email,
	  							 		"options": option
	  							 	  })
		

			updateEmailSchedulerOptionsRequestHandler = requesthandler.UpdateEmailSchedulerOptionsRequestHandler(input_json_data)
			output_json_data = updateEmailSchedulerOptionsRequestHandler.updateEmailSchedule()
			logging.debug("Output JSON Data is %s", output_json_data)

		logging.debug("Rendering the Trending Stream Page Again")
		self.redirect("/trendingStream")


class InitialEmailOptionsScreenHandler(webapp2.RequestHandler):

	def post(self):
		
		user = users.get_current_user()
		logging.debug("Inside the Method to get the Initial Email Options for Prepopulating the screen")
		user_email = user.email()
		logging.debug("User Email Being Processed is: %s", user_email)

		option = "none"
		if not mail.is_email_valid(user_email):
			logging.debug("Invalid Email Address %s", user_email)
		else:
			logging.debug("Valid Email Address %s", user_email)
			#Create a JSON Object using the Info read from the Post Command
			input_json_data =  json.dumps(
									  {
	  							 		"email" : user_email,
	  							 	  })
			
			initialEmailOptionsRequestHandler = requesthandler.InitialEmailOptionsRequestHandler(input_json_data)
			output_json_data = initialEmailOptionsRequestHandler.loadInitialEmailSchedule()
			logging.debug("Output JSON Data is %s", output_json_data)
		
		self.response.write(output_json_data)


#################################################################
class Send5MinuteEmailStreamScreenHandler(webapp2.RequestHandler):

	def get(self):

		user = users.get_current_user()

		logging.debug("Inside Send5MinuteEmailStreamScreenHandler. Other logs should show up too....")
		logging.debug("Check in the DB for all the Users whose Email Preference is 5 Minutes")

		#Create a JSON Object using the Info read from the Post Command
		input_json_data =  json.dumps({
      							 		"jsonData" : ""
      							 	  })

		sendEMailStreamRequestHandler_5mins = requesthandler.SendEMailStreamRequestHandler(input_json_data)
		output_json_data = sendEMailStreamRequestHandler_5mins.get5MinutesEmailSubscriberList()
		logging.debug("Output JSON Data is %s", output_json_data)

		parsed_output_json = json.loads(output_json_data)
		logging.debug("Parsed Output JSON Data is %s", parsed_output_json)

		email_list = parsed_output_json["emailList"]
		logging.debug("Email List for 5 Mins is %s", email_list)

		email_subject = "Your 5 Minutes Photo Stream Trend Update"
		if email_list:
			#Get the Latest Trending Streams
			logging.debug("Get the Latest Trending Streams from the NDB")
			trendingStreamRequestHandler = requesthandler.TrendingStreamRequestHandler(input_json_data)
			trending_output_json_data = trendingStreamRequestHandler.loadTrendingStreams()
			logging.debug("Output Trending JSON Data is %s", trending_output_json_data)

			trending_parsed_output_json = json.loads(trending_output_json_data)
			logging.debug("Parsed Output Trending JSON Data is %s", trending_parsed_output_json)

			trending_streams = trending_parsed_output_json["trendingStreams"]
			logging.debug("Trending Stream is %s", trending_streams)

			email_content = ""
			if trending_streams:
				for stream in trending_streams:
					email_img = '<p>' + '<img src="' + str(stream["coverImgUrl"]) + '" alt="' + str(stream["streamName"]) + '" height="150" width="150" >' 
					email_txt = '  Stream "' + str(stream["streamName"]) + '" has been viewed ' + str(stream["viewCount"]) + ' time(s) in the past 1 hour' + '</p>'
					email_content = email_content + email_img + email_txt


				email_body = "<html>" + "<body>" + email_content	+ "</body>" + "</html>"
				logging.debug("Generated HTML is: %s", email_body)

				logging.debug("Email List is %s", email_list)
				for email in email_list:
					sender_address = "Connexus Trending News <asimsaleem.p@gmail.com>"
					user_address = email["ownerEmailId"] #"asimsaleem.p@gmail.com"
					logging.debug("user Address is %s", user_address)
					subject = email_subject
					body = email_body
					html = email_body
					logging.debug("Sending the Email Now....")
					mail.send_mail(sender=sender_address,
	                 			   to=user_address,
	                 			   subject=email_subject,
	                 			   body="",
	                 			   html=html)
	          		

class SendHourlyEmailStreamScreenHandler(webapp2.RequestHandler):

	def get(self):

		user = users.get_current_user()

		logging.debug("Inside SendHourlyEmailStreamScreenHandler")
		logging.debug("Check in the DB for all the Users whose Email Preference is 1 Hour")

		#Create a JSON Object using the Info read from the Post Command
		input_json_data =  json.dumps({
      							 		"jsonData" : ""
      							 	  })

		sendEMailStreamRequestHandler_1Hour = requesthandler.SendEMailStreamRequestHandler(input_json_data)
		output_json_data = sendEMailStreamRequestHandler_1Hour.getHourlyEmailSubscriberList()
		logging.debug("Output JSON Data is %s", output_json_data)

		parsed_output_json = json.loads(output_json_data)
		logging.debug("Parsed Output JSON Data is %s", parsed_output_json)

		email_list = parsed_output_json["emailList"]
		logging.debug("Parsed OUTPUT Json is %s", email_list)

		email_subject = "Your Hourly Photo Stream Trend Update"
		if email_list:

			#Get the Latest Trending Streams
			logging.debug("Get the Latest Trending Streams from the NDB")
			trendingStreamRequestHandler = requesthandler.TrendingStreamRequestHandler(input_json_data)
			trending_output_json_data = trendingStreamRequestHandler.loadTrendingStreams()
			logging.debug("Output Trending JSON Data is %s", trending_output_json_data)

			trending_parsed_output_json = json.loads(trending_output_json_data)
			logging.debug("Parsed Output Trending JSON Data is %s", trending_parsed_output_json)

			trending_streams = trending_parsed_output_json["trendingStreams"]
			logging.debug("Trending Stream is %s", trending_streams)

			email_content = ""
			if trending_streams:
				for stream in trending_streams:
					email_img = '<p>' + '<img src="' + str(stream["coverImgUrl"]) + '" alt="' + str(stream["streamName"]) + '" height="150" width="150" >' 
					email_txt = '  Stream "' + str(stream["streamName"]) + '" has been viewed ' + str(stream["viewCount"]) + ' time(s) in the past 1 hour' + '</p>'
					email_content = email_content + email_img + email_txt

				email_body = "<html>" + "<body>" + email_content	+ "</body>" + "</html>"
				logging.debug("Generated HTML is: %s", email_body)
				logging.debug("Email List is %s", email_list)
				for email in email_list:
					sender_address = "Connexus Trending News <asimsaleem.p@gmail.com>"
					user_address = email["ownerEmailId"] #"asimsaleem.p@gmail.com"
					logging.debug("user Address is %s", user_address)
					subject = email_subject
					body = email_body
					html = email_body
					logging.debug("Sending the Email Now....")
					mail.send_mail(sender=sender_address,
	                 			   to=user_address,
	                 			   subject=email_subject,
	                 			   body="",
	                 			   html=html)


class SendDailyEmailStreamScreenHandler(webapp2.RequestHandler):

	def get(self):

		user = users.get_current_user()
		logging.debug("Inside SendDailyEmailStreamScreenHandler")
		logging.debug("Check in the DB for all the Users whose Email Preference is Daily")

		#Create a JSON Object using the Info read from the Post Command
		input_json_data =  json.dumps(
									  {
      							 		"jsonData" : ""
      							 	  })

		sendEMailStreamRequestHandler_Daily = requesthandler.SendEMailStreamRequestHandler(input_json_data)
		output_json_data = sendEMailStreamRequestHandler_Daily.getDailyEmailSubscriberList()
		logging.debug("Output JSON Data is %s", output_json_data)

		parsed_output_json = json.loads(output_json_data)
		logging.debug("Parsed Output JSON Data is %s", parsed_output_json)

		email_list = parsed_output_json["emailList"]
		logging.debug("Parsed OUTPUT Json is %s", email_list)

		email_subject = "Your Daily Photo Stream Trend Update"
		if email_list:

			#Get the Latest Trending Streams
			logging.debug("Get the Latest Trending Streams from the NDB")
			trendingStreamRequestHandler = requesthandler.TrendingStreamRequestHandler(input_json_data)
			trending_output_json_data = trendingStreamRequestHandler.loadTrendingStreams()
			logging.debug("Output Trending JSON Data is %s", trending_output_json_data)

			trending_parsed_output_json = json.loads(trending_output_json_data)
			logging.debug("Parsed Output Trending JSON Data is %s", trending_parsed_output_json)

			trending_streams = trending_parsed_output_json["trendingStreams"]
			logging.debug("Trending Stream is %s", trending_streams)

			email_content = ""
			if trending_streams:
				for stream in trending_streams:
					email_img = '<p>' + '<img src="' + str(stream["coverImgUrl"]) + '" alt="' + str(stream["streamName"]) + '" height="150" width="150" >' 
					email_txt = '  Stream "' + str(stream["streamName"]) + '" has been viewed ' + str(stream["viewCount"]) + ' time(s) in the past 1 hour' + '</p>'
					email_content = email_content + email_img + email_txt

				email_body = "<html>" + "<body>" + email_content	+ "</body>" + "</html>"
				logging.debug("Generated HTML is: %s", email_body)

				logging.debug("Email List is %s", email_list)
				for email in email_list:
					sender_address = "Connexus Trending News <asimsaleem.p@gmail.com>"
					user_address = email["ownerEmailId"] #"asimsaleem.p@gmail.com"
					logging.debug("user Address is %s", user_address)
					subject = email_subject
					body = email_body
					html = email_body
					logging.debug("Sending the Email Now....")
					mail.send_mail(sender=sender_address,
	                 			   to=user_address,
	                 			   subject=email_subject,
	                 			   body="",
	                 			   html=html)

###################################################################

#####################################################################
class UnsubscribeStreamScreenHandler(webapp2.RequestHandler):

	def post(self):

		logging.debug("INSIDE THE UnsubscribeStreamScreenHandler Method")
		logging.debug("JSON FROM REQQUEST IS: %s", self.request.get('json'))
		input_json_data= self.request.get('json')

		unsubscribeStreamRequestHandler = requesthandler.UnsubscribeStreamRequestHandler(input_json_data)
		output_json_data = unsubscribeStreamRequestHandler.unsubscribeFromStream()
		logging.debug("Output JSON Data is %s", output_json_data)

		parsed_output_json = json.loads(output_json_data)
		logging.debug("Parsed Output JSON Data is %s", parsed_output_json)



class SubscribeStreamScreenHandler(webapp2.RequestHandler):

	def post(self):

		logging.debug("INSIDE THE SubscribeStreamScreenHandler Method")
		logging.debug("JSON FROM REQQUEST IS: %s", self.request.get('json'))
		input_json_data= self.request.get('json')
		
		subscribeStreamRequestHandler = requesthandler.SubscribeStreamRequestHandler(input_json_data)
		output_json_data = subscribeStreamRequestHandler.subscribeToStream()
		logging.debug("Output JSON Data is %s", output_json_data)

		parsed_output_json = json.loads(output_json_data)
		logging.debug("Parsed Output JSON Data is %s", parsed_output_json)
#####################################################################


#####################################################################
class DeleteStreamScreenHandler(webapp2.RequestHandler):

	def post(self):
		logging.debug("INSIDE THE DeleteStreamRequestHandler Method")
		logging.debug("JSON FROM REQQUEST IS: %s", self.request.get('json'))
		input_json_data = self.request.get('json')

		deleteStreamRequestHandler = requesthandler.DeleteStreamRequestHandler(input_json_data)
		output_json_data = deleteStreamRequestHandler.deleteStream()
		logging.debug("Output JSON Data is %s", output_json_data)

		parsed_output_json = json.loads(output_json_data)
		logging.debug("Parsed Output JSON Data is %s", parsed_output_json)

#####################################################################


#####################################################################
class UploadUrlScreenHandler(webapp2.RequestHandler):

	def get(self):
		input_stream_name = self.request.get("streamName")
		input_stream_id = input_stream_name.replace(" ", "")
		input_latitude = self.request.get("lat")
		input_longitude = self.request.get("lon")

		print "Input Stream Name is : ", input_stream_name 
		print "XXXX Latitude is: ", input_latitude
		print "YYYY Longitude is: ", input_longitude

		logging.debug("Inside the method to get the UPLOAD_URL")
		#upload_url = blobstore.create_upload_url('/uploadImgMulti/' + input_stream_name , gs_bucket_name=default_bucket_name())
		redir_url = '/uploadImgMulti/' + input_stream_id + "/" + input_latitude + "/" + input_longitude
		upload_url = blobstore.create_upload_url(redir_url , gs_bucket_name=default_bucket_name())
				
		self.response.headers['Content-Type'] = 'application/json'
		self.response.out.write('"' + upload_url + '"')
#####################################################################

###################################################################
class GeoViewStreamScreenHandler(webapp2.RequestHandler):

	def post(self):
		input_stream_name = streamName
		print "Input Stream Name is : ", input_stream_name 

		print "Inside GEO VIew Stream Handler"
		template = JINJA_ENVIRONMENT.get_template("template/geoViewStream.html")
		self.response.write(template.render())

	def get(self):

		print "GETTTT Inside GEO VIew Stream Handler"
		input_stream_name = self.request.get("streamName")
		print "XXXXXXXX Input Stream Name is : ", input_stream_name 
		template_values = {
			'stream_name' : input_stream_name,
		}
		template = JINJA_ENVIRONMENT.get_template("template/geoViewStream.html")
		self.response.write(template.render(template_values))


class PictureLocationsScreenHandler(webapp2.RequestHandler):

	def get(self):

		print "Need to go get the picture locations"
		print "Stream Name passed is: ", self.request.get("streamName")

		streamName = self.request.get("streamName")
		startDate = self.request.get("startDate")
		endDate = self.request.get("endDate")
		input_json_data =  json.dumps(
									  {
      							 		"streamName" : streamName,
      							 		"startDate" : startDate,
      							 		"endDate" : endDate,
      							 	  })

		picLocationStreamRequestHandler = requesthandler.PicLocationStreamRequestHandler(input_json_data)
		output_json_data = picLocationStreamRequestHandler.getPicLocationStream()
		logging.debug("Output JSON Data is %s", output_json_data)
		print "Output JSON Data is: ", output_json_data


		parsed_output_json = json.loads(output_json_data)
		logging.debug("Parsed Output JSON Data is %s", parsed_output_json)

		self.response.write(output_json_data)
###################################################################

###################################################################
class FileUploadMobileHandler(webapp2.RequestHandler):

	def get(self):
		input_stream_id = self.request.get("streamName").replace(" ", "")
		logging.debug("Input STREAM ID is : %s", input_stream_id)
		redir_url = '/uploadImg/' + input_stream_id 
		upload_url = blobstore.create_upload_url(redir_url , gs_bucket_name=default_bucket_name())
				
		#self.response.headers['Content-Type'] = 'application/json'
		logging.debug("UPLOAD URL TO BE USED IS: %s", upload_url)
		self.response.out.write(upload_url)


	def post(self):

		logging.debug("Inside the Method for FILE UPLOAD HANDLING")
		
		#upload_files = self.get_uploads('file')  # 'file' is file upload field in the form
		upload_file_name = self.request.get("fileName")
		logging.debug("upload_files name is %s", upload_file_name)
		upload_files = self.request.get("file")
		logging.debug("upload_filesis %s", upload_files)

		upload = self.request.get('file')
		logging.debug("upload is %s", upload)
###################################################################



###################################################################
class SocialStreamScreenHandler(webapp2.RequestHandler):

	def get(self): 

		template = JINJA_ENVIRONMENT.get_template("template/socialStream.html")
		self.response.write(template.render())
###################################################################


###################################################################
class SocialStreamLoginScreenHandler(webapp2.RequestHandler):

	def get(self): 

		template = JINJA_ENVIRONMENT.get_template("template/successSocialStream.html")
		self.response.write(template.render())
###################################################################