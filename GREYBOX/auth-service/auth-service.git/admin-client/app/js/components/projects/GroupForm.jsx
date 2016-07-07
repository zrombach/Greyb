'use strict';

var React = require('react');
var { Navigation, State } = require('react-router');
var t = require('tcomb-form');
var { connect } = require('reflux');
var _ = require('lodash');
var { add, save } = require('../../actions/Group.js');
var InputForm = require('../shared/InputForm.jsx');
var Group = require('../../domain/Group.js');
var cx = require('react/lib/cx');

/*
 * User Input for adding/editing a group. If adding a group, the projectName
 * query parameter must be supplied. If editing a group, the groupName query
 * parameter is required.
 */


// the custom template

var GroupView = React.createClass({
    mixins: [ Navigation, State ],

    getInitialState: function () {
        return { groupToEdit: null,
            members: null };
    },

    hideField: function(locals) {

      var formGroupClasses = {
            'form-group': true,
            'has-feedback': true, // required for the icon
            'has-error': locals.hasError
      };

      return (
    /*jshint ignore:start */

          <input
            className="control-label"
            name={locals.name}
            placeholder={locals.placeholder}
            type="hidden"
            value={locals.value} />

        /*jshint ignore:end */
      );
    },

    render: function () {

        var props = {
                onSubmit: this.handleSave
            },
            options = {
                auto: 'labels',
                fields: {
                    dn: {
                        label: 'DN'
                    },
                    members: {
                        templates: {
                            list: this.hideField
                        }
                    }
                }
            };

        if (this.state.groupToEdit) { //disable name field and populate form
            options.fields.name = {
                disabled: true
            };

            props.value = this.state.groupToEdit;
        }

        var form = t.form.create(Group, options);
        /* jshint ignore:start */
        return <InputForm tcombForm={ form } { ...props } />;
        /* jshint ignore:end */
    },

    componentWillMount: function () {
        this.prepareState();
    },

    componentWillReceiveProps: function () {
        this.prepareState();
    },

    handleSave: function (group) {
        if (this.state.groupToEdit) {
            group = Group.update(group, {
                members: {
                    '$set': this.state.members
                }
            });
            save(group);
        } else {
            group = Group.update(group, {
                name: {
                    '$set': this.getQuery().projectName + '!' + group.name
                }
            });

            add(group);
        }

        this.transitionTo('projects');
    },

    //ensure one of groupName or projectName (but not both) are defined and is valid
    prepareState: function () {
        var { groupName } = this.getParams(), { projectName } = this.getQuery();

        if (groupName ? projectName : !projectName) {
            this.transitionTo('projects');
        }

        if (groupName) {
            var groupToEdit = _.find(this.props.groups, g => g.name === groupName);
            if (!groupToEdit) {
                this.transitionTo('projects');
            } else {
                this.setState({ groupToEdit: groupToEdit, members: groupToEdit.members });
            }

            return;
        }

        if (projectName) {
            var project = _.find(this.props.projects, p => p.name === projectName);
            if (!project) {
                this.transitionTo('projects');
            }
        }

    }
});

module.exports = GroupView;
