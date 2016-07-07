'use strict';

var Reflux = require('reflux');

var ProjectManagmentActions = Reflux.createActions([
    'setActiveGroup', 'setActiveProject'
]);

module.exports = ProjectManagmentActions;
