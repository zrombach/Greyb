'use strict';

var React = require('react');
var { Link, RouteHandler, State } = require('react-router');

var App = React.createClass({
    mixins: [State],

    checkActiveTab: function(routePath, routeType) {
         if (routePath === '/' || routePath === '/group') {
             routePath = '/project';
         }
        
         if (routePath.indexOf(routeType)===1) {
             return 'active';
         } else {
             return '';
         }
        return '';
    },

    render: function () {
        var activeRoute = this.getPathname();

        /*jshint ignore:start */
        return (
            <div>
                <nav>
                    <ul className="nav nav-tabs">
                        <li className={ this.checkActiveTab(activeRoute, 'project')}><Link to="projects">Projects</Link></li>
                        <li className={ this.checkActiveTab(activeRoute, 'user')}><Link to="users">Users</Link></li>
                    </ul>
                </nav>
                <RouteHandler />
            </div>
        );
        /*jshint ignore:end */
    }
});

module.exports = App;
