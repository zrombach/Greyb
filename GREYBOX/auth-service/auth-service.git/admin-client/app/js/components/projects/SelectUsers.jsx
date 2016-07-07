'use strict';

var React = require('react');
var { connect } = require('reflux');
var _ = require('lodash');
var { Navigation, State } = require('react-router');
var DataGrid = require('../shared/DataGrid');
var { addUsers } = require('../../actions/Group.js');

var SelectUserView = React.createClass({
    mixins: [ Navigation, State ],

    getInitialState: function () {
        return { activeGroup: null };
    },

    render: function () {
        var columns = [
            { property: 'dn', title: 'User Name' },
            { property: 'uid', title: 'UID' },
            { property: 'lastName', title: 'Last Name' },
            { property: 'fullName', title: 'Full Name' }
        ];

        var buttons = [{
            type: 'primary',
            name: 'Add',
            onClick: this.handleAddUsers
        }];
        
        var getUserFromDN = function( dn ) {
            return _.find(this.props.users, {'dn': dn});
        };

        var usersInGroup = [], usersNotInGroup = [];
        if (this.state.activeGroup) {
            if (this.state.activeGroup.members) {                
                usersInGroup = this.state.activeGroup.members.map( getUserFromDN, this );
            } 
        }
        usersNotInGroup = _.difference(this.props.users, usersInGroup);

        /* jshint ignore:start */
        return (
            <DataGrid columns={ columns }
                records={ usersNotInGroup }
                description="Select a user to add to group"
                heading= { 'Add a User to Group ' + this.state.activeGroup.name }
                multiSelect
                buttons={ buttons } />
        );
        /* jshint ignore:end */
    },

    handleAddUsers: function (users) {
        addUsers(this.state.activeGroup, users);
        this.transitionTo('projects');
    },

    componentWillMount: function () {
        this.prepareState();
    },

    componentWillReceiveProps: function () {
        this.prepareState();
    },

    prepareState: function () {
        var activeGroup = _.find(this.props.groups, g => g.name === this.getQuery().groupName);
        if (activeGroup) {
            this.setState({ activeGroup: activeGroup });
        } else {
            this.transitionTo('projects');
        }
    }
});

module.exports = SelectUserView;
