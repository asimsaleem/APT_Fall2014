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

import jinja2
import webapp2
import logging
import json

from datetime import datetime
from datetime import timedelta


#Internal Files
import storagemodel
import screenhandler
import requesthandler
import main

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





###################################################################
class ErrorScreenHandler(webapp2.RequestHandler):

	def get(self):
		logging.debug("Error Encountered somewhere in the Application....")
		errorMessage = self.request.get("err")
		logging.debug("Error Message is: %s", errorMessage)

		template_values = {
							'error_msg' : errorMessage,
						  }

		template = JINJA_ENVIRONMENT.get_template("template/error.html")
		self.response.write(template.render(template_values))
###################################################################



###################################################################
#This is where all the Mapping between the screen and the handlers is controlled
application = webapp2.WSGIApplication([
    ('/', screenhandler.LoginScreenHandler),
    #('/loginStream', screenhandler.LoginScreenHandler),
    ('/createStream', screenhandler.CreateStreamScreenHandler),
    ('/manageStream', screenhandler.ManageStreamScreenHandler),
    ('/viewSingleStream', screenhandler.ViewSingleStreamScreenHandler),
    ('/viewAllStream', screenhandler.ViewAllStreamScreenHandler),
    ('/searchStream', screenhandler.SearchStreamScreenHandler),
    ('/searchTypeAheadStream', screenhandler.SearchTypeAheadStreamScreenHandler),
    ('/geoViewStream', screenhandler.GeoViewStreamScreenHandler),
    ('/pictureLocations', screenhandler.PictureLocationsScreenHandler),


    ('/trendingStream', screenhandler.TrendingStreamScreenHandler),
    ('/unsubscribeStream', screenhandler.UnsubscribeStreamScreenHandler),
    ('/subscribeStream', screenhandler.SubscribeStreamScreenHandler),
    ('/deleteStream', screenhandler.DeleteStreamScreenHandler),
    ('/emailOption', screenhandler.UpdateEmailSchedulerOptionsScreenHandler),
    ('/emailSchedule', screenhandler.InitialEmailOptionsScreenHandler),
    ('/uploadURLHandler', screenhandler.UploadUrlScreenHandler),


    ('/socialStream', screenhandler.SocialStreamScreenHandler),
    ('/successSocialStream', screenhandler.SocialStreamLoginScreenHandler),
    ('/errorStream', ErrorScreenHandler),

    ('/create', screenhandler.CreateStreamScreenRequestHandler),
    ('/manage', requesthandler.ManageStreamRequestHandler),
 	('/viewSingle', requesthandler.ViewSingleStreamRequestHandler),
 	('/viewAll', requesthandler.ViewAllStreamRequestHandler),
    ('/search', screenhandler.SearchStreamScreenRequestHandler),
    ('/trending', requesthandler.TrendingStreamRequestHandler),
    ('/delete', requesthandler.DeleteStreamRequestHandler),

    ('/unsubscribe', requesthandler.UnsubscribeStreamRequestHandler),
 	('/subscribe',requesthandler.SubscribeStreamRequestHandler),   
 	('/uploadImg/([^/]+)?', requesthandler.UploadImageToStreamRequestHandler),
    ('/uploadImgMulti/([^/]+)?/([^/]+)?/([^/]+)?', requesthandler.UploadMultipleImageToStreamRequestHandler),
 
    ('/updateTrendingStream', requesthandler.UpdateTrendingStreamRequestHandler),
    ('/mail5MinsStream', screenhandler.Send5MinuteEmailStreamScreenHandler),
    ('/mailHourlyStream', screenhandler.SendHourlyEmailStreamScreenHandler),
    ('/mailDailyStream', screenhandler.SendDailyEmailStreamScreenHandler),

    ('/login', screenhandler.LoginMobileHandler),
    ('/mystreams', screenhandler.ManageStreamMobileScreenHandler),
    ('/mysubscribedstreams', screenhandler.MySubscribedStreamMobileHandler),
    ('/mobileSearch', screenhandler.SearchStreamMobileHandler),
    ('/nearbySearch', screenhandler.NearbyStreamMobileHandler),
    ('/viewStream', screenhandler.ViewSingleStreamMobileHandler),
    ('/fileStream', screenhandler.FileUploadMobileHandler),


], debug=True)
###################################################################


