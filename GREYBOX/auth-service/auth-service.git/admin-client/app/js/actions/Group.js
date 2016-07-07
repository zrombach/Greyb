'use strict';

var Reflux = require('reflux');
var GroupApi = require('../webapi/Group.js');

var GroupActions = Reflux.createActions([
    'save',
    'saved',
    'saveFailed',
    'addUsers',
    'usersAdded',
    'removeUsers',
    'usersRemoved',
    'setActive',
    'add',
    'delete',
    'deleted',
    'failedDelete',
    'fetched',
    'fetch'
]);

GroupActions.add.listen(group => {
    GroupApi.add(group).then(GroupActions.saved.bind(null, group))
        .fail(GroupActions.saveFailed);
});

GroupActions.save.listen(group => {
    GroupApi.save(group).then(GroupActions.saved.bind(null, group))
        .fail(GroupActions.saveFailed)
});

GroupActions.delete.listen(group => {
    GroupApi.delete(group)
        .then(GroupActions.deleted.bind(null, group))
        .fail( 
            // TODO: provide some information to user
            GroupActions.failedDelete.bind(null, group)
        );
});

GroupActions.addUsers.listen((group, users) => {
    GroupApi.addUsers(group, users).then(GroupActions.usersAdded.bind(null, group, users));
});

GroupActions.removeUsers.listen((group, users) => {
    GroupApi.removeUsers(group, users).then(GroupActions.usersRemoved.bind(null, group, users));
});

module.exports = GroupActions;
