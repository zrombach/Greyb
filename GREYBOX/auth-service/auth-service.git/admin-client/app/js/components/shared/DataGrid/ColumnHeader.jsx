'use strict';

var React = require('react');

var ColumnHeader = React.createClass({
    render: function () {
        /*jshint ignore:start */
        return (
            <th onClick={ this.props.handleSort }>
                { this.props.title }
                { this.props.sortOrder && this.renderSortIcon() }
            </th>
        );
        /*jshint ignore:end */
    },

    renderSortIcon: function () {
        var sortIconClass = 'fa fa-sort-' + this.props.sortOrder;
        var sortIconStyle = { float: 'right' };

        /*jshint ignore:start */
        return <i style={ sortIconStyle } className={ sortIconClass }></i>;
        /*jshint ignore:end */
    }
});

module.exports = ColumnHeader;
