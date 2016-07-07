'use strict';

var Reflux = require('reflux');
var actions = require('../actions/Group.js');
var _ = require('lodash');
var { update } = require('react/addons').addons;
var projectActions = require('../actions/Project.js');
var { getProjectNameFromGroupName } = require('../util/Group.js');
var assign = require('object-assign');

var cache = [];

var GlobalGroupStore = Reflux.createStore({
    listenables: [actions, { projectDeleted: projectActions.deleted }],

    onFetched: function (groups) {
        this.updateStore(groups);
    },

    onSaved: function (group) {
        this.updateStore([group]);
    },

    onDeleted: function (group) {
        var i = _.findIndex(cache, g => g.name === group.name);
        if (i > -1) {
            cache = update(cache, { $splice: [[i, 1]]});
        }
        this.trigger(this.getState());
    },

    onProjectDeleted: function (project) {
        cache = cache.filter(g => getProjectNameFromGroupName(g.name) !== project.name);
        this.trigger(this.getState());
    },

    getInitialState: function () {
        return this.getState();
    },

    getState: function () {
        return cache;
    },

    updateStore: function (groups) {
        groups.forEach(gNew => {
            var i = _.findIndex(cache, gCache => gCache.name === gNew.name);
            cache = i > -1 ?
                update(cache, { $splice: [[i, 1, gNew]]})
                : update(cache, { $push: [gNew] });
        });

        this.trigger(this.getState());
    },

    onUsersAdded: function(group, users) {
        var editableGroup = assign({}, group);
        editableGroup.members = _.union(group.members, _.map(users, 'dn'));
        this.updateStore([editableGroup]);
    },

    onUsersRemoved: function(group, users) {        
        if (group.members) {
            var editableGroup = assign({}, group);
            editableGroup.members = _.filter(group.members, function(member) { return !_.find( users, {'dn': member}); });
            this.updateStore([editableGroup]);
        }
        
    }
});

module.exports = GlobalGroupStore;
