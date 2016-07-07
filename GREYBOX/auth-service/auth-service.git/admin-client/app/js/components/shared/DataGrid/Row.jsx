'use strict';

var React = require('react/addons');
var classSet = React.addons.classSet;
var Cell = require('./Cell.jsx');

var Row = React.createClass({
    render: function () {
        var classes = classSet({
            'info': this.props.selected,
            'Datagrid_selected_row': this.props.selected
        });

        /*jshint ignore:start */
        var cells = this.props.dataValues.map(
            (val, i) => <Cell dataValue={ val } key={ i } />
        );

        return (
            <tr onClick={ this.props.onRecordSelect } className={ classes } >
                { cells }
            </tr>
        );
        /*jshint ignore:end */
    }
});

module.exports = Row;
