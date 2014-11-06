from google.appengine.ext import ndb

class Stream(ndb.Model):

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
	streamCreateDate = ndb.DateTimeProperty(auto_now_add=True, indexed=True)
	streamAccessDate = ndb.DateTimeProperty(auto_now=True, indexed=True)

class PhotoStream(ndb.Model):

	streamId = ndb.StringProperty(indexed=True)
	streamName = ndb.StringProperty(indexed=True)
	photoList = ndb.BlobKeyProperty(repeated=True, indexed=False)
	latitude = ndb.StringProperty(indexed=False)
	longitude = ndb.StringProperty(indexed=False)
	comments = ndb.StringProperty(indexed=False)
	pictureUpdateDate = ndb.DateTimeProperty(indexed=True, auto_now_add=True)


class UserSchedulerStream(ndb.Model):

	ownerEmailId = ndb.StringProperty(indexed=True)
	ownerNickname = ndb.StringProperty(indexed=False)
	emailSchedule = ndb.StringProperty(indexed=True)


class TrendingStream(ndb.Model):

	streamId = ndb.StringProperty(indexed=True)
	streamName = ndb.StringProperty(indexed=True)
	viewCount = ndb.IntegerProperty(indexed=True)
	coverImgUrl = ndb.StringProperty(indexed=False)


class UserDetailsStream(ndb.Model):

	userId = ndb.StringProperty(indexed=True)
	userPwd = ndb.StringProperty(indexed=True)
