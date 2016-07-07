'use strict';

var Reflux = require('reflux');
var ProjectManagementActions = require('../actions/ProjectManagement.js');
var assign = require('object-assign');

var activeGroup = null;
var activeProject = null;

var ProjectManagementStore = Reflux.createStore({
    listenables: ProjectManagementActions,

    onSetActiveGroup: function (group) {
        activeGroup = group;
        this.trigger(this.getGroupState());
    },

    onSetActiveProject: function (project) {
        if (activeProject !== project)  {
            activeProject = project;
            activeGroup = null;     
        }
        this.trigger(this.getProjectState());
    },

    getInitialState: function () {
        return assign(
            this.getGroupState(),
            this.getProjectState()
        );
    },

    getGroupState: function () {
        return {
            activeGroup: activeGroup
        };
    },

    getProjectState: function (projects) {
        return {
            activeProject: activeProject,
            activeGroup: activeGroup        
        };
    }
});

module.exports = ProjectManagementStore;
