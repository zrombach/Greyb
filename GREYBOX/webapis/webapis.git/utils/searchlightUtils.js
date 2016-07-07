var MongoClient = require('mongodb').MongoClient,
    assert      = require('assert'),
    _           = require('lodash');

var url = "mongodb://mongo:27017/mongo-user";

var searchlightUtils = {
  findAndFormat: function(uid, callback) {
    var query;
    var self = this;
    var searchlightResponse = {
      jsonrpc: "2.0",
      id: 1,
      result: {
        user: {
          format: "card",
          user: [],
          records: []
        }
      }
    }

    var userNotFound = {
      code: -32000,
      message: "No match for user(s) [ " + uid + " ]",
      data: null
    }

    if (Array.isArray(uid)) {
      var queryArr = _.map(uid, function(user) {
        return {"uid": user};
      });
      query = {"$or": queryArr};
    } else {
      query = {"uid": uid};
    }

    MongoClient.connect(url, function(err, db) {
      assert.equal(null, err);

      var cursor   = db.collection('users').find(query);
      var response = searchlightResponse;

      cursor.each(function(err, user) {
        assert.equal(null, err);

        if (user !== null) {
          response.result.user.user = typeof uid === "string" ? [ uid ] : uid;
          response.result.user.records.push(self.formatUser(user));
        } else {
          if (response.result.user.user.length == 0) {
            response.result = null;
            response.error  = userNotFound;
          }
          callback(response);
          db.close();
        }
      });
    });
  },

  formatUser: function(user) {
    var searchlightUser = {
      uid: user.uid + ".xxx.ic.gov",
      sid: user.uid,
      name: user.fullName,
      identity: {
        id: {
          uid: user.uid + ".xxx.ic.gov",
          sid: user.uid,
          unix: "#####",
          dn: user.dn,
          certificates: {
            sign: [],
            encrypt: []
          }
        },
        name: {
          full: user.fullName,
          given: user.firstName,
          sur: user.lastName,
          cn: "",
          title: null,
          rank: null
        },
        contact: {
          email: "",
          phone: {}
        },
        location: {
          building: "",
          room: "",
          suite: ""
        },
        org: {
          company: "",
          unit: "FAKE123",
          current: "F"
        }
      }
    };

    return searchlightUser;
  }
}

module.exports = searchlightUtils
