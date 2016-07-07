'use strict';

var $ = require('jquery');
var Group = require('../domain/Group.js');
var Project = require('../domain/Project.js');
var _ = require('lodash');
var defaults = require('../domain/defaults');

var ProjectApi = {
    fetchAll: function() {
        var url = API_URL + '/extras/projects';
        return $.getJSON(url).then(projects => {
            //there's no way to lazily load groups per project in the API.
            //This is the only place we can do it without n+1 ajax requests
            return {
                projects: projects.map(p => new Project(p)),
                groups: _.flatten(projects.map(p => (p.projectGroups || []).map(g => new Group({ ...defaults.Group, ...g }))))
            };
        });
    },

    save: function(data) {
        var url = API_URL + '/extras/projects';
        return $.ajax({
            type: 'POST',
            url: url,
            data: JSON.stringify(data),
            dataType: 'json',
            contentType: 'application/json'
        });
    },

    delete: function(projectName) {
        var url = API_URL + '/extras/projects/' + projectName;

        // since using DELETE, may need to await full OPTIONS preflight sequence
        return $.ajax({
            type: 'DELETE',
            url: url,
            dataType: 'json',
            contentType: 'application/json'
        }).always(function(data) {
            // cause this to complete before returning
        });
    }


};

module.exports = ProjectApi;
