'use strict';

var { Route, DefaultRoute } = require('react-router');
var App = require('./App.jsx');
var React = require('react');
var { UserList, UserForm, UserManagement } = require('./users');
var { ProjectManagement, ProjectForm, ProjectList, GroupForm, SelectUsers } = require('./projects');

/* jshint ignore:start */
module.exports = (
    <Route path="/" handler={ App }>
        <Route name="projects" path="/" handler={ ProjectManagement }>
            <DefaultRoute handler={ ProjectList } />
            <Route name="projectForm" path="/project" handler={ ProjectForm } />
            <Route name="groupForm" path="/group/:groupName?" handler={ GroupForm } />
            <Route name="selectUsers" path="/members" handler={ SelectUsers } />
        </Route>

        <Route name="users" handler={ UserManagement } >
            <DefaultRoute handler={ UserList } />
            <Route name="userForm" path="/user/:userDn?" handler={ UserForm } />
        </Route>
    </Route>
);
/* jshint ignore:end */
