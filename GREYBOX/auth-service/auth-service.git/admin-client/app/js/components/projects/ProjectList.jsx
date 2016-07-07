'use strict';

var React = require('react');
var { connect } = require('reflux');
var DataGrid = require('../shared/DataGrid');
var GroupActions = require('../../actions/Group.js');
var ProjectActions = require('../../actions/Project.js');
var { Navigation, State } = require('react-router');
var { getProjectNameFromGroupName } = require('../../util/Group.js');
var { ProjectManagementStore } = require('../../stores');
var { setActiveGroup, setActiveProject } = require('../../actions/ProjectManagement.js');

var ProjectList = React.createClass({
    mixins: [ Navigation, State, connect(ProjectManagementStore) ],

    render: function() {
        /* jshint ignore:start */
        return (
            <div>
                { this.renderProjectGrid() }
                { this.state.activeProject && this.renderGroupList() }
                { this.state.activeProject && this.state.activeGroup && this.renderUserList() }
            </div>
        );
        /* jshint ignore:end */
    },

    renderProjectGrid: function () {
        var columns = [
            { property: 'name', title: 'Project Name' }
        ];

        var buttons = [
            {
                name: 'Add',
                onClick: this.handleAddProject,
                type: 'primary'
            }, {
                name: 'Remove',
                onClick: this.handleRemoveProject,
                disabledUntilRecordSelect: true
            }
        ];

        /* jshint ignore:start */
        return (
            <DataGrid records={ this.props.projects }
                columns={ columns }
                heading="Projects"
                description="Most interactions for authentication are done in the context of a project.  These are added to the 'real' system using provided tools."
                onRecordSelect={ this.handleSelectProject }
                multiSelect={ false }
                selectedRecords={ this.state.activeProject ? [this.state.activeProject] : [] }
                buttons={ buttons } />
        );
        /* jshint ignore:end */
    },

    renderGroupList: function () {
        var groups = this.props.groups.filter(g => getProjectNameFromGroupName(g.name) === this.state.activeProject.name);

        var columns = [
            { property: 'dn', title: 'DN' },
            { property: 'name', title: 'Group Name' },
            { property: 'displayName', title: 'Display Name' },
            { property: 'description', title: 'Description' }
        ];

        var buttons = [
            {
                name: 'Add',
                type: 'primary',
                onClick: this.handleAddGroup
            }, {
                name: 'Edit',
                disabledUntilRecordSelect: true,
                onClick: this.handleEditGroup
            }, {
                name: 'Remove',
                disabledUntilRecordSelect: true,
                onClick: this.handleDeleteGroup
            }
        ];

        var heading = 'Groups for ' + this.state.activeProject.name;

        /* jshint ignore:start */
        return (
            <DataGrid records={ groups }
                columns={ columns }
                heading= { heading }
                description="Project 'Groups' are useful for mapping roles to users within your application."
                onRecordSelect={ this.handleSelectGroup }
                multiSelect={ false }
                selectedRecords={ this.state.activeGroup ? [this.state.activeGroup] : [] }
                buttons={ buttons} />
        );
        /* jshint ignore:end */
    },

    renderUserList: function () {

        var groups = this.props.groups.filter(g => g.name === this.state.activeGroup.name);        

        var users = [];
        if (groups.length > 0 && groups[0].members) {
            users = this.props.users.filter(u => groups[0].members.indexOf(u.dn) > -1);
        }

        var columns = [
            {property: 'dn', title: 'User Name'},
            {property: 'uid', title: 'UID'},
            {property: 'lastName', title: 'Last Name'},
            {property: 'fullName', title: 'Full Name'}
        ];

        var buttons = [
            {
                name: 'Add',
                onClick: this.handleAddUsers,
                type: 'primary'
            }, {
                name: 'Remove',
                onClick: this.handleRemoveUsers,
                disabledUntilRecordSelect: true
            }
        ];

        var heading = 'Users for ' + this.state.activeGroup.name;
        /* jshint ignore:start */
        return (
            <DataGrid records={ users }
                columns={ columns }
                heading= { heading }
                description="Users mapped to the selected group"
                multiSelect
                buttons={ buttons } />
        );
        /* jshint ignore:end */
    },

    handleRemoveProject: function (projects) {
        ProjectActions.delete(projects[0]);
    },

    handleAddProject: function () {
        this.transitionTo('projectForm');
    },

    handleSelectProject: function (projects) {
        setActiveProject(projects[0]);
    },

    handleAddGroup: function () {
        this.transitionTo('groupForm', null, { projectName: this.state.activeProject.name });
    },

    handleEditGroup: function () {
        this.transitionTo('groupForm', { groupName: this.state.activeGroup.name });
    },

    handleSelectGroup: function (groups) {
        setActiveGroup(groups[0]);
    },

    handleDeleteGroup: function () {
        GroupActions.delete(this.state.activeGroup);
    },

    handleAddUsers: function () {
        this.transitionTo('selectUsers', null, { groupName: this.state.activeGroup.name });
    },

    handleRemoveUsers: function (users) {
        GroupActions.removeUsers(this.state.activeGroup, users);
    }
});

module.exports = ProjectList;
