'use strict';

if (!Object.assign) {
    Object.assign = require('object-assign');
}

var promise = require('es6-promise');
promise.polyfill();
require('babel-core/polyfill');

var React = require('react');
var $ = require('jquery');
var App = require('./components/App.jsx');
var { initApp, appReady } = require('./actions/App.js');
var _ = require('lodash');
var Routes = require('./components/Routes.jsx');
var Router = require('react-router');

//Enable React developer tools
window.React = React;

$.ajaxSetup({
    dataFilter: function(raw, type) {
        if (type === 'json') {
            raw = raw.startsWith('/*') && raw.endsWith('*/') ? raw.slice(2, -2) : raw;
        }
        return raw;
    }
});

// Enable withCredentials for all requests
$.ajaxPrefilter(function (options, originalOptions, jqXHR) {
    options.xhrFields = {
        withCredentials: true
    };
});

appReady.listen(_.once(function () {
    require('../styles/main.scss');

    Router.run(Routes, function (Handler) {
        React.render(<Handler />, document.getElementById('main'));
    });
}));

initApp();
