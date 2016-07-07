'use strict';

var Reflux = require('reflux');
var $ = require('jquery');
var UserActions = require('./User.js');
var ProjectActions = require('./Project.js');

var AppActions = Reflux.createActions([ 'initApp', 'appReady' ]);

AppActions.initApp.listen(() => {
    UserActions.fetchUsers();
    ProjectActions.fetchAll();
});

Reflux.joinTrailing(UserActions.fetched, ProjectActions.fetched).listen(AppActions.appReady);

module.exports = AppActions;
