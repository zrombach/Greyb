'use strict';

var StoreFactory = require('./common/StoreFactory');
var actions = require('../actions/User.js');

module.exports = StoreFactory.createSimpleStore(
    actions,
    'dn'
);
