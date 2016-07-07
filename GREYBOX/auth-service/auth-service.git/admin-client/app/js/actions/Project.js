'use strict';

var Reflux = require('reflux');
var ProjectApi = require('../webapi/Project.js');
var GroupActions = require('./Group.js');
var _ = require('lodash');
var Group = require('../domain/Group.js');

var ProjectActions = Reflux.createActions([
    'fetchAll',
    'save',
    'saved',
    'setActive',
    'fetch',
    'fetched',
    'delete',
    'deleted',
    'failedDelete'
]);

ProjectActions.fetchAll.listen(() => {
    ProjectApi.fetchAll().then(data => {
        ProjectActions.fetched(data.projects);
        GroupActions.fetched(data.groups);
    });
});

ProjectActions.save.listen(data => {
    ProjectApi.save(data).then(ProjectActions.saved);
});

ProjectActions.fetch.listen(() => {
    ProjectApi.fetchAll().then(ProjectActions.fetched);
});

ProjectActions.delete.listen(project => {
    ProjectApi.delete(project.name)
        .then(
            // TODO: figure out why if this isn't wrapped in a function call, it'll get called even if
            //  the .fail should be called.  But if is, is handled appropriately...
            function() {
                ProjectActions.deleted(project);
            }
        )
        .fail( function() {
            // TODO: provide some information to user
            ProjectActions.failedDelete(project);
        });
});

module.exports = ProjectActions;
