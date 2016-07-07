'use strict';

var React = require('react');
var Reflux = require('reflux');
var { Navigation } = require('react-router');
var DataGrid = require('../shared/DataGrid');
var { GlobalUserStore } = require('../../stores');
var UserActions = require('../../actions/User.js');

/**
 * View for list of users in the system.
 */
var UserList = React.createClass({
    mixins: [ Reflux.connect(GlobalUserStore, 'users'), Navigation ],

    render: function() {
        var columns = [
            { property: 'dn', title: 'User Name' },
            { property: 'uid', title: 'UID' },
            { property: 'lastName', title: 'Last Name' },
            { property: 'fullName', title: 'Full Name' }
        ];

        var buttons = [
            {
                name: 'Add',
                type: 'primary',
                onClick: this.handleAddUser
            }, {
                name: 'Edit',
                disabledUntilRecordSelect: true,
                onClick: this.handleEditUser
            }, {
                name: 'Remove',
                disabledUntilRecordSelect: true,
                onClick: this.handleDeleteUser
            }
        ];

        /* jshint ignore:start */
        return (
            <div>
                <DataGrid ref="userGrid"
                    columns={ columns }
                    records={ this.state.users }
                    heading="Users"
                    description="Users are provided through corporate services and are not editable through the authorization APIs.  However, for purposes of this stubbed application, enter users of interest to you."
                    multiSelect={ false }
                    buttons={ buttons } />
            </div>
        );
        /* jshint ignore:end */
    },

    handleAddUser: function () {
        this.transitionTo('userForm');
    },

    handleEditUser: function (users) {
        this.transitionTo('userForm', { userDn: this.refs.userGrid.getSelectedRecords()[0].dn });
    },

    handleDeleteUser: function (users) {
        UserActions.delete(users[0]);
    }
});

module.exports = UserList;
