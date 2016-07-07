'use strict';

var t = require('tcomb-form');
var Classification = require('./Classification.js');

/**
 * Defines a group that may be entered into and/or retrieved from the authentication
 * system.
 */
var Group = t.struct({
    dn: t.Str,
    name: t.Str,
    displayName: t.maybe(t.Str),
    description: t.maybe(t.Str),
    clearance: t.maybe(Classification),
    formalAccesses: t.list(t.Str),
    members: t.list(t.Str),
    private: t.maybe(t.Bool),
    visible: t.maybe(t.Bool)
}, 'Group');

module.exports = Group;
