'use strict';

var t = require('tcomb-form');

var Project = t.struct({
    name: t.Str
});

module.exports = Project;
