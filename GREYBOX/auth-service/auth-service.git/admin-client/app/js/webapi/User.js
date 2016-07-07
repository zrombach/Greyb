'use strict';

var $ = require('jquery');
var t = require('tcomb-form');
var User = require('../domain/User');
var defaults = require('../domain/defaults');

var UserApi = {
    fetchUsers: function () {
        var url = API_URL + '/extras/users';
        return $.getJSON(url).then(users =>
            users.map(user => new User({ ...defaults.User, ...user }))
        );
    },

    saveUser: function (data) {
        var url = API_URL + '/extras/users/' + data.dn;
        return $.ajax({
            type: 'PUT',
            url: url,
            data: JSON.stringify(data),
            dataType: 'json',
            contentType: 'application/json'
        }).then(user => new User({ ...defaults.User, ...user }));
    },

    deleteUser: function(data) {

        var url = API_URL + '/extras/users/' + data.dn;

        return $.ajax({
            type: 'DELETE',
            url: url,
            dataType: 'json',
            contentType: 'application/json'
        }).always(function(data) {
            // cause this to complete before returning, otherwise we may get back OPTIONS responses, not DELETE
        });
    }
};

module.exports = UserApi;
