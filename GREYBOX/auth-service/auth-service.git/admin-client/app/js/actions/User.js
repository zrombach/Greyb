'use strict';

var Reflux = require('reflux');
var UserApi = require('../webapi/User.js');

var UserActions = Reflux.createActions([
    'fetchUsers',
    'fetched',
    'saveUser',
    'saved',
    'saveUserFailed',
    'delete',
    'deleted',
    'failedDelete'
]);

UserActions.fetchUsers.listen(() => {
    UserApi.fetchUsers().then(UserActions.fetched);
});

UserActions.saveUser.listen(data => {
    UserApi.saveUser(data).then(UserActions.saved)
        .fail(UserActions.saveUserFailed);
});

UserActions.delete.listen(user => {
    // delete returns a response that includes no data -  need to pass along the original deleted user
	UserApi.deleteUser(user)
		.then(UserActions.deleted.bind(null, user))
		.fail(UserActions.failedDelete.bind(null,user));
});

module.exports = UserActions;
