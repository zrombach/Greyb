'use strict';

var $ = require('jquery');
var Group = require('../domain/Group.js');

function groupBaseUrl(group) {
    return API_URL + '/extras/groups/' + group.name;
}

var _updateUsers = function (group, users, action) {
    return $.ajax({
        type: action,
        url: groupBaseUrl(group) + '/members',
        data: JSON.stringify(users.map(u => u.dn)),
        dataType: 'json',
        contentType: 'application/json'
    }).always(function(data) {
        // cause this to complete before returning, in case of any OPTIONS calls
    });
};
    
var GroupApi = {
    save: function(group) {
        return $.ajax({
            type: 'PUT',
            url: groupBaseUrl(group),
            data: JSON.stringify(group),
            dataType: 'json',
            contentType: 'application/json'
        });
    },

    // Add a new group to the system
    add: function (group) {
        return this.save(group);
    },

    delete: function(group) {
        return $.ajax({
            type: 'DELETE',
            url: groupBaseUrl(group),
            dataType: 'json'
        }).always(function(data) {
            // cause this to complete before returning, in case of any OPTIONS calls
        });
    },

    addUsers: function (group, users) {
        return _updateUsers(group, users, 'POST');
    },

    removeUsers: function (group, users) {    
        return _updateUsers(group, users, 'DELETE');
    }, 
    
    mockCall: function (group) {
        var deferred = $.Deferred(),
        promise = deferred.promise();

        deferred.resolve(group);
        return promise;
    }
};

module.exports = GroupApi;
