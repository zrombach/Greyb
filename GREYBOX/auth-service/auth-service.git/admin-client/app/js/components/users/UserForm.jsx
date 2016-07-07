'use strict';

var React = require('react');
var { connect, ListenerMixin } = require('reflux');
var t = require('tcomb-form');
var _ = require('lodash');
var User = require('../../domain/User.js');
var actions = require('../../actions/User.js');
var InputForm = require('../shared/InputForm.jsx');
var { Navigation, State } = require('react-router');
var { GlobalUserStore } = require('../../stores');

/**
 * View for editing user information or adding a new user to the system.
 */
var UserForm = React.createClass({
    mixins: [
        connect(GlobalUserStore, 'users'),
        Navigation, State
    ],

    render: function() {
        var { userToEdit } = this.state;

        var options = {
            auto: 'labels',
            fields: {
                dn: { label: 'DN' },
                dutyOrg: { label: 'Duty Org (optional)'},
                uid: { label: 'UID' },
                fullName: { help: 'Full (common) name'},
                grantBy: { help: 'Entities that granted user clearance' },
                title: { help: 'Official title' },
                coi: { label: 'COI' }
            }
        };

        if (userToEdit) {
            options.fields.dn.disabled = true;
        }

        var userForm = t.form.create(User, options);

        /* jshint ignore:start */
        return (
            <InputForm tcombForm={ userForm }
                value={ userToEdit }
                onSubmit={ this.handleSave } />
        );
        /* jshint ignore:end */
    },

    componentWillMount: function () {
        this.prepareState();
    },

    componentWillReceiveProps: function () {
        this.prepareState();
    },

    handleSave: function (user) {
        actions.saveUser(user);
        this.transitionTo('users');
    },

    prepareState: function () {
        var { userDn } = this.getParams();

        if (userDn) {
            var userToEdit = _.find(this.state.users, user => user.dn === userDn);
            if (userToEdit) {
                this.setState({ userToEdit: userToEdit });
            } else {
                this.transitionTo('users');
            }
        }
    }
});

module.exports = UserForm;
