'use strict';

var React = require('react');
var ColumnHeader = require('./ColumnHeader.jsx');

var HeaderRow = React.createClass({
    render: function () {
        /*jshint ignore:start */
        var colHeaders = this.props.columns.map((col, i) =>
            <ColumnHeader title={ col.title }
                handleSort={ this.props.handleSort.bind(null, col.property) }
                sortOrder={ (this.props.sortBy === col.property) && this.props.sortOrder }
                key={ i } />
        );

        return <thead><tr>{colHeaders}</tr></thead>;
        /*jshint ignore:end */
    }
});

module.exports = HeaderRow;
