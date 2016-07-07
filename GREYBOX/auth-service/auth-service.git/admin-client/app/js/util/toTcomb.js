'use strict';

var t = require('tcomb-form');
var tjs = require('tcomb-json-schema');

/**
 * Convert JSON schema document into domain class defined by tcomb types.
 * @param {object} schema JSON schema object.
 */
function toTcomb(schema) {
    var params = {};
    var required = {};

    // Parse required array into hash
    for (var i in (schema.required || {})) {
        required[schema.required[i]] = true;
    }

    for (var key in schema.properties) {
        if (/^_.*/.test(key) !== true) { // Ignore "private" properties
            var prop = schema.properties[key];

            // Create tcomb domain representation
            params[key] = tjs(prop);

            // Check if this field should be optional.
            // NOTE: 'maybe' lists are NOT supported by tcomb-forms.
            if (!required[key] && prop.type !== 'array') {
                params[key] = t.maybe(params[key]);
            }

        }
    }

    return t.struct(params);
}

module.exports = toTcomb;
