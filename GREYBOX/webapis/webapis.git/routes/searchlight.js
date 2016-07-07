var express          = require('express'),
    router           = express.Router(),
    searchlightUtils = require('../utils/searchlightUtils'),
    _                = require('lodash');

function handleOwnUser(req, res, next) {
  var cert = req.connection.getPeerCertificate();

	var format = req.query.format;
	if (!format || format != "json") {
		var err = new Error('Unsupported format');
		err.status = 400;
		next(err);
		return;
	}

  var cnArr = cert.subject.CN.split(",")[0].split(" ");
  var uid   = cnArr[cnArr.length - 1];

  searchlightUtils.findAndFormat(uid, function(user) {
    res.send(user);
  });
};

function handleSpecificUser (req, res, next) {
	var username = req.params.username;
	console.log("Returning data for " + username);

	var format = req.query.format;
	if (!format || format != 'json') {
		var err = new Error('Unsupported format');
		err.status = 400;
		next(err);
		return;
	}

  searchlightUtils.findAndFormat(username, function(user) {
    res.send(user);
  });
};

function handleMultipleUsers (req, res, next) {
  var usernames = _.uniq(req.params.usernames.split(','));

  searchlightUtils.findAndFormat(usernames, function(users) {
    res.send(users);
  });
}

router.get("/", handleOwnUser);

router.get('/list=:usernames', handleMultipleUsers);

router.get('/:username', handleSpecificUser);

module.exports = router;
