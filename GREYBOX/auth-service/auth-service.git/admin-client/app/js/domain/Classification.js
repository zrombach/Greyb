'use strict';

var t = require('tcomb-form');

var Classification = t.enums.of([
    'SECRET',
    'TOP SECRET',
    'UNCLASSIFIED',
    'CONFIDENTIAL'
], 'Classification');

module.exports = Classification;
