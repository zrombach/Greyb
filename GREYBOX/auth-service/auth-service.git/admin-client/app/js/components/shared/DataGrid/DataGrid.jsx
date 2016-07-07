'use strict';

var React = require('react');
var { object, arrayOf, func, string, oneOf, shape, bool } = React.PropTypes;
var Table = require('./Table.jsx');
var Button = require('./Button.jsx');
var _ = require('lodash');

var DataGrid = React.createClass({
    propTypes: {
        /*
        * The array of records that the grid will display
        */
        records: arrayOf(object).isRequired,

        /*
        * Configuration objects for each column that should appear in the grid.
        * Each configuration must consist of 'property', which is the name of
        * the property in the record this column will display and 'title' which
        * is the display name of the property that will appear in the table
        * header row.
        */
        columns: arrayOf(shape({
            property: string.isRequired,
            title: string.isRequired
        })).isRequired,

        /*
        * If provided, will appear as the grid's title
        */
        heading: string,

        /*
        * If provided, will appear in the header of the grid
        */
        description: string,

        /*
        * Name of the property in the record to sort by.
        */
        sortBy: string,

        /*
        * The initial sort order of the grid - 'asc' by default.
        */
        sortOrder: oneOf(['asc', 'desc']),

        /*
        * The records that are initially selected - defaults to none
        */
        selectedRecords: arrayOf(object),

        /*
        * Can more than one record be selected at a time? Defaults to true.
        */
        multiSelect: bool,

        buttons: arrayOf(shape({
            name: string.isRequired,
            type: oneOf(['default', 'primary', 'warning', 'danger', 'info']),
            disabledUntilRecordSelect: bool,
            onClick: func
        }))
    },

    getDefaultProps: function () {
        return {
            sortOrder: 'asc',
            multiSelect: true,
            selectedRecords: []
        };
    },

    getInitialState: function () {
        var props = Object.assign({
            sortBy: this.props.columns[0].property
        }, this.props);

        return Object.assign(props, this.getSortedRecords(props));
    },

    render: function() {
        /*jshint ignore:start */
        return (
            <div className="panel panel-default">
                { this.props.heading &&
                    <div className="panel-heading">
                        <h2 className="panel-title">{ this.props.heading }</h2>
                    </div>
                }

                { this.props.description &&
                    <div className="panel-body">
                        <p>{ this.props.description }</p>
                    </div>
                }

                <Table columns={ this.props.columns }
                    records={ this.state.records }
                    handleSort={ this.handleSort }
                    sortOrder={ this.state.sortOrder }
                    sortBy={ this.state.sortBy }
                    onRecordSelect={ this.handleSelect }
                    isSelected={ this.isSelected } />

                { this.props.buttons && this.props.buttons.length > 0 &&
                    this.renderButtons()
                }
            </div>
        );
        /*jshint ignore:end */
    },

    renderButtons: function () {
        var selected = this.state.selectedRecords;

        /*jshint ignore:start */
        var buttons = this.props.buttons.map((config, i) =>
            <Button name={ config.name }
                key={ i }
                type={ config.type }
                onClick={ config.onClick && config.onClick.bind(null, selected) }
                disabled={ config.disabledUntilRecordSelect && selected.length < 1 } />
        );

        return (
            <div className="panel-footer">
                <div className="btn-group btn-group-sm">
                    { buttons }
                </div>
            </div>
        );
        /*jshint ignore:end */
    },

    componentWillReceiveProps: function (newProps) {
        var props = Object.assign({
            sortBy: newProps.columns[0].property
        }, newProps);

        this.setState(Object.assign(props, this.getSortedRecords(props)));
    },

    handleSort: function (sortBy) {
        var order = this.state.sortOrder === 'asc' ? 'desc' : 'asc';

        var state = {
            sortBy: sortBy,
            sortOrder: order,
            records: this.state.records
        };

        this.setState(Object.assign(state, this.getSortedRecords(state)));
    },

    handleSelect: function (record) {
        var records = [];

        if (this.props.multiSelect) {
            records = this.isSelected(record) ?
            _.without(this.state.selectedRecords, record)
            : this.state.selectedRecords.concat(record);
        } else {
            if (!this.isSelected(record)) {
                records = [record];
            }
        }

        if (typeof(this.props.onRecordSelect) === 'function') {
            this.props.onRecordSelect(records);
        }

        this.setState({ selectedRecords: records });
    },

    getSelectedRecords: function () {
        return this.state.selectedRecords;
    },

    getSortedRecords: function (currentState) {
        var records = _.sortBy(currentState.records, currentState.sortBy);

        if (currentState.sortOrder === 'desc') {
            records.reverse();
        }

        return { records: records };
    },

    isSelected: function (record) {
        return _.contains(this.state.selectedRecords, record);
    }
});

module.exports = DataGrid;
