'use strict';

var React = require('react');
var { RouteHandler } = require('react-router');
var { connect } = require('reflux');
var { GlobalGroupStore, GlobalUserStore, GlobalProjectStore } = require('../../stores');

var ProjectManagement = React.createClass({
    mixins: [
        connect(GlobalProjectStore, 'projects'),
        connect(GlobalGroupStore, 'groups'),
        connect(GlobalUserStore, 'users')
    ],

    render: function () {
        /* jshint ignore:start */
        return (
            <RouteHandler { ...this.state } />
        );
        /* jshint ignore:end */
    }
});

module.exports = ProjectManagement;
