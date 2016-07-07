'use strict';

var StoreFactory = require('./common/StoreFactory');
var actions = require('../actions/Project.js');

module.exports = StoreFactory.createSimpleStore(
    actions,
    'name'
);
