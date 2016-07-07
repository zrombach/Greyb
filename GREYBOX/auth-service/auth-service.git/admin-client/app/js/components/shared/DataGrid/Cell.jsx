'use strict';

var React = require('react');

var Cell = React.createClass({
    render: function () {
        /*jshint ignore:start */
        return (
            <td>
                { this.props.dataValue }
            </td>
        );
        /*jshint ignore:end */
    }
});

module.exports = Cell;
