'use strict';

var React = require('react');
var Row = require('./Row.jsx');
var HeaderRow = require('./HeaderRow.jsx');

function orderedPick (props, obj) {
    return props.map(prop => obj[prop]);
}

var Table = React.createClass({
    render: function () {
        var propNames = this.props.columns.map(col => col.property);
        var propsFilter = orderedPick.bind(null, propNames);

        /*jshint ignore:start */
        var rows = this.props.records.map((rec, i) =>
            <Row dataValues={ propsFilter(rec) }
                key={ i }
                selected={ this.props.isSelected(rec) }
                onRecordSelect={ this.props.onRecordSelect.bind(null, rec) } />
        );

        return (
            <table className="table table-striped table-hover Datagrid_table">
                <HeaderRow columns={ this.props.columns }
                    handleSort={ this.props.handleSort }
                    sortBy={ this.props.sortBy }
                    sortOrder={ this.props.sortOrder } />
                <tbody>
                    { rows }
                </tbody>
            </table>
        );
        /*jshint ignore:end */
    }
});

module.exports = Table;
