'use strict';

/***
 * Exported functions to be used in the testing scripts.
 */
module.exports = {
  uploadImageBody,
  genNewUser,
  genNewUserReply,
  selectUser,
  selectUserSkewed,
  genNewChannel,
  selectChannelFromUser,
  selectChannelFromUserSkewed,
  selectChannelFromChannelLst,
  selectChannelFromChannelLstSkewed,
  selectUserfromUserLst,
  genNewMessage,
  selectImagesIdFromMsgList,
  random50,
  random70
}


const Faker = require('faker')
const fs = require('fs')
const path = require('path')

var imagesIds = []
var images = []
var users = []

// All endpoints starting with the following prefixes will be aggregated in the same for the statistics
var statsRegExpr = [ [/.*\/rest\/media\/.*/,"GET","/rest/media/*"],
			[/.*\/rest\/media/,"POST","/rest/media"],
			[/.*\/rest\/user\/.*\/channels/,"GET","/rest/user/*/channels"],
			[/.*\/rest\/user\/.*/,"GET","/rest/user/*"],
			[/.*\/rest\/user\/.*\/subscribe\/.*/,"POST","/rest/user/*/subscribe/*"],
			[/.*\/rest\/user\/.*\/unsubscribe\/.*/,"DELETE","/rest/user/*/unsubscribe/*"],
			[/.*\/rest\/user\/auth/,"POST","/rest/user/auth"],
			[/.*\/rest\/user/,"POST","/rest/user"],
			[/.*\/rest\/channel\/.*\/add\/.*/,"POST","/rest/channel/*/add/*"],
			[/.*\/rest\/channel\/.*\/remove\/.*/,"DELETE","/rest/channel/*/remove/*"],
			[/.*\/rest\/channel/,"POST","/rest/channel"],
			[/.*\/rest\/channel\/.*\/messages.*/,"GET","/rest/channel/*/messages"],
			[/.*\/rest\/channel\/.*\/users.*/,"GET","/rest/channel/*/users"],
			[/.*\/rest\/channel\/.*/,"GET","/rest/channel/*"],
			[/.*\/rest\/channel\/.*/,"DELETE","/rest/channel/*"],
			[/.*\/rest\/messages/,"POST","/rest/messages"],
			[/.*\/rest\/messages\/.*/,"GET","/rest/messages/*"]
	]

// Function used to compress statistics
global.myProcessEndpoint = function( str, method) {
	var i = 0;
	for( i = 0; i < statsRegExpr.length; i++) {
		if( method == statsRegExpr[i][1] && str.match(statsRegExpr[i][0]) != null)
			return method + ":" + statsRegExpr[i][2];
	}
	return method + ":" + str;
}

// Auxiliary function to select an element from an array
Array.prototype.sample = function(){
	   return this[Math.floor(Math.random()*this.length)]
}

// Auxiliary function to select an element from an array
Array.prototype.sampleSkewed = function(){
	return this[randomSkewed(this.length)]
}

// Returns a random value, from 0 to val
function random( val){
	return Math.floor(Math.random() * val)
}

// Returns a random value, from 0 to val
function randomSkewed( val){
	let beta = Math.pow(Math.sin(Math.random()*Math.PI/2),2)
	let beta_left = (beta < 0.5) ? 2*beta : 2*(1-beta);
	return Math.floor(beta_left * val)
}


// Loads data about images from disk
function loadData() {
	var basedir
	if( fs.existsSync( '/images')) 
		basedir = '/images'
	else
		basedir =  'images'	
	fs.readdirSync(basedir).forEach( file => {
		if( path.extname(file) === ".jpeg") {
			var img  = fs.readFileSync(basedir + "/" + file)
			images.push( img)
		}
	})
	var str;
	if( fs.existsSync('users.data')) {
		str = fs.readFileSync('users.data','utf8')
		users = JSON.parse(str)
	} 
}

loadData();

/**
 * Sets the body to an image, when using images.
 */
function uploadImageBody(requestParams, context, ee, next) {
	requestParams.body = images.sample()
	return next()
}

/**
 * Process reply of the download of an image. 
 * Update the next image to read.
 */
function processUploadReply(requestParams, response, context, ee, next) {
	if( typeof response.body !== 'undefined' && response.body.length > 0) {
		imagesIds.push(response.body)
	}
    return next()
}

/**
 * Select an image to download.
 */
function selectImageToDownload(context, events, done) {
	if( imagesIds.length > 0) {
		context.vars.imageId = imagesIds.sample()
	} else {
		delete context.vars.imageId
	}
	return done()
}


/**
 * Generate data for a new user using Faker
 */
function genNewUser(context, events, done) {
	const first = `${Faker.name.firstName()}`
	const last = `${Faker.name.lastName()}`
	context.vars.id = first + "." + last
	context.vars.name = first + " " + last
	context.vars.pwd = `${Faker.internet.password()}`
	return done()
}


/**
 * Process reply for of new users to store the id on file
 */
function genNewUserReply(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let u = JSON.parse( response.body)
		users.push(u)
		fs.writeFileSync('users.data', JSON.stringify(users));
	}
    return next()
}

/**
 * Select user
 */
function selectUser(context, events, done) {
	if( users.length > 0) {
		let user = users.sample()
		context.vars.user = user.id
		context.vars.pwd = user.pwd
	} else {
		delete context.vars.user
		delete context.vars.pwd
	}
	return done()
}


/**
 * Select user
 */
function selectUserSkewed(context, events, done) {
	if( users.length > 0) {
		let user = users.sampleSkewed()
		context.vars.user = user.id
		context.vars.pwd = user.pwd
	} else {
		delete context.vars.user
		delete context.vars.pwd
	}
	return done()
}

/**
 * Generate data for a new channel
 */
function genNewChannel(context, events, done) {
	context.vars.channelName = `${Faker.random.word()}`
	context.vars.publicChannel = Math.random() < 0.2
	return done()
}


/**
 * Select a channel from the list of channelIds in a user
 */
function selectChannelFromUser(context, events, done) {
	if( typeof context.vars.userObj !== 'undefined' && context.vars.userObj.channelIds  !== 'undefined' &&
				context.vars.userObj.channelIds.length > 0)
		context.vars.channelId = context.vars.userObj.channelIds.sample()
	else 
		delete context.vars.channelId
	return done()
}

/**
 * Select a channel from the list of channelIds in a user
 */
function selectChannelFromUserSkewed(context, events, done) {
	if( typeof context.vars.userObj !== 'undefined' && context.vars.userObj.channelIds  !== 'undefined' &&
				context.vars.userObj.channelIds.length > 0)
		context.vars.channelId = context.vars.userObj.channelIds.sampleSkewed()
	else 
		delete context.vars.channelId
	return done()
}

/**
 * Select a channel from the list of channelIds in a user
 */
function selectChannelFromChannelLst(context, events, done) {
	if( typeof context.vars.channelLst !== 'undefined' && context.vars.channelLst.length > 0)
		context.vars.channelId = context.vars.channelLst.sample()
	else 
		delete context.vars.channelId
	return done()
}

/**
 * Select a channel from the list of channelIds in a user
 */
 function selectUserfromUserLst(context, events, done) {
	if( typeof context.vars.userLst !== 'undefined' && context.vars.userLst.length > 0)
		context.vars.user = context.vars.userLst.sample()
	return done()
}

/**
 * Select a channel from the list of channelIds in a user
 */
function selectChannelFromChannelLstSkewed(context, events, done) {
	if( typeof context.vars.channelLst !== 'undefined' && context.vars.channelLst.length > 0)
		context.vars.channelId = context.vars.channelLst.sampleSkewed()
	else 
		delete context.vars.channelId
	return done()
}


/**
 * Generate data for a new message
 */
function genNewMessage(context, events, done) {
	context.vars.msgText = `${Faker.lorem.paragraph()}`
	if( Math.random() < 0.05) {
		context.vars.hasImage = true
	} else {
		delete context.vars.hasImage
	}
	context.vars.imageId = null
	return done()
}

/**
 * Select imageIds from msgList
 */
function selectImagesIdFromMsgList(context, events, done) {
	let imageIdLst = []
	if( typeof context.vars.msgList !== 'undefined') {
		let msg
		for( msg in context.vars.msgList) {
			if( msg.imageId != null){
				if( ! imageIdLst.includes( msg.imageId))
					imageIdLst.push(msg.imageId)
			}
		}
	}
	context.vars.imageIdLst = imageIdLst
	return done()
}

/**
 * Return true with probability 50% 
 */
function random50(context, next) {
  const continueLooping = Math.random() < 0.5
  return next(continueLooping);
}

/**
 * Return true with probability 50% 
 */
function random70(context, next) {
  const continueLooping = Math.random() < 0.7
  return next(continueLooping);
}

function extractCookie(requestParams, response, context, ee, next) {
    if( response.statusCode >= 200 && response.statusCode < 300)  {
        for( let header of response.rawHeaders) {
            if( header.startsWith("scc:session")) {
                context.vars.mycookie = header.split(';')[0];
            }
        }
    }
    return next()
}




