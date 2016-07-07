'use strict';

var React = require('react');
var { Navigation } = require('react-router');
var t = require('tcomb-form');
var { save } = require('../../actions/Project.js');
var InputForm = require('../shared/InputForm.jsx');
var ProjectType = require('../../domain/Project.js');

var ProjectView = React.createClass({
    mixins: [ Navigation ],

    render: function () {

        var options = {
            auto: 'labels'
        };

        var form = t.form.create(ProjectType, options);

        /* jshint ignore:start */
        return <InputForm tcombForm={ form } onSubmit={ this.handleSave } />;
        /* jshint ignore:end */
    },

    handleSave: function (project) {
        save(project);
        this.transitionTo('projects');
    }
});

module.exports = ProjectView;
