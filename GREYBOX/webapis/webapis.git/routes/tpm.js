var express       = require('express');
var router        = express.Router();
var REQUIRED_KEYS = ["action", "project", "userDN", "eventTime"];
var USER_ACTIONS  = ["createdBookmark", "deletedBookmark", "export", "feedback", "follow",
  "login", "searchedMultimedia", "searchReports", "share", "subscribe", "subscriptiondelivery",
  "subscriptionSent", "subscriptionView", "unfollow", "unsubscribe", "viewedGSH",
  "viewedMultimedia", "viewedReport", "viewHistory", "visited"];

function handleEvents(req, res, next) {
  if (validateQueryParams(req.body)) {
    if (validateUserAction(req.body.action)) {
      res.send("Event Logged Sucessfully");
    } else {
      var err = new Error();
      err.status = 400;
      next(err);
    }
  } else {
    var err = new Error();
    err.status = 400;
    next(err);
  }
};

function handleStatus(req, res, next) {
  var response = {
    status: "up"
  };

  res.send(response);
};

function validateQueryParams(params) {
  for (var i in REQUIRED_KEYS) {
    if (Object.keys(params).indexOf(REQUIRED_KEYS[i]) < 0) {
      return false;
    }
  }

  return true;
};

function validateUserAction(action) {
    if (USER_ACTIONS.indexOf(action) < 0) {
      return false;
    }
    return true;
};

router.post("/", handleEvents);
router.get("/status", handleStatus);

module.exports = router;
