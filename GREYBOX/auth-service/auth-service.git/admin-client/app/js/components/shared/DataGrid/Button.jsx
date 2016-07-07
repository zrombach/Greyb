'use strict';

var React = require('react');
var { string, oneOf, bool, func } = React.PropTypes;

var Button = React.createClass({
    propTypes: {
        name: string.isRequired,
        type: oneOf(['default', 'primary', 'warning', 'danger', 'info']),
        disabled: bool,
        onClick: func
    },

    getDefaultProps: function () {
        return {
            type: 'default',
            disabled: false
        };
    },

    render: function () {
        var className='btn btn-' + this.props.type;

        /*jshint ignore:start */
        return (
            <button disabled={ this.props.disabled }
                className={ className }
                onClick={ this.props.onClick } >

                { this.props.name }
            </button>
        );
        /*jshint ignore:end */
    }
});

module.exports = Button;
