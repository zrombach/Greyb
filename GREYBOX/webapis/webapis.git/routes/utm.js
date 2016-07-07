var extend  = require('util')._extend;
var express = require('express');
var crypto  = require('crypto');
var router  = express.Router();

var fields = {
  base: {
    dn                      : "string",
    systemName              : "string",
    userQueryIntent         : "string",
    userSelectedAuthorities : "array",
    eventDate               : "string"
  },
  child: {
    parentId: "string"
  },
  parent: {
    justification           : "string",
    requiresPostQueryReview : "boolean",
    missionId               : "number",
    classification          : "string",
    queryStartDate          : "string",
    queryEndDate            : "string",
    uspFlag                 : "boolean",
    spcmaFlag               : "boolean"
  }
}

function guid() {
  return crypto.randomBytes(32).toString('hex').toUpperCase();
}

function handleGet(req, res, next) {
    res.send('GET method unsupported');
};

function handlePut(req, res, next) {
    var validation = validateRequestBody(req);

    if (validation.missingField !== null) {
      var err    = new Error("Missing required field '" + validation.missingField + "'");
      err.status = 422;
      next(err);
      return;
    } else if (validation.invalidType) {
      var err    = new Error("The request sent by the client was syntactically incorrect");
      err.status = 400;
      next(err);
      return;
    } else {
      var result = {
        parentAuditRecordId: guid()
      };

      res.send(result);
    }
};
function validateRequestBody(request, next) {
    var requiredFields;
    var errors = {
      missingField : null,
      invalidType  : false 
    };
    var requestKeys   = Object.keys(request.body);

    if (requestKeys.indexOf("parentId") < 0) {
      requiredFields = extend({}, fields.base);
      extend(requiredFields, fields.parent);
    } else {
      requiredFields = extend({}, fields.base);
      extend(requiredFields, fields.child);
    }

    var requiredKeys = Object.keys(requiredFields);

    for (var i in requiredKeys) {
      if (requestKeys.indexOf(requiredKeys[i]) < 0) {
        errors.missingField = requiredKeys[i];
        break;
      }

      switch(requiredFields[requiredKeys[i]]) {
        case "array":
          if (!Array.isArray(request.body[requiredKeys[i]])) {
            errors.invalidType = true;
          }
          break;
        case "number":
          if ((parseInt(request.body[requiredKeys[i]]) < 0) || false) {
            errors.invalidType = true;
          }
          break;
        case "boolean":
          var requestValue = request.body[requiredKeys[i]];
          if (typeof requestValue === "string") {
            if (requestValue.toLowerCase() !== "true" && requestValue.toLowerCase() !== "false") {
              errors.invalidType = true;
            }
            break;
          } 
        default:
          if (typeof request.body[requiredKeys[i]] !== requiredFields[requiredKeys[i]]) {
            errors.invalidType = true;
          }
      }
    }

    return errors;
};

router.get("/audit", handleGet);
router.put("/audit", handlePut);

module.exports = router;
