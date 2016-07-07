'use strict';

var React = require('react');
var { RouteHandler } = require('react-router');

var UserManagement = React.createClass({
    render: function () {
        /* jshint ignore:start */
        return <RouteHandler />;
        /* jshint ignore:end */
    }
});

module.exports = UserManagement;
