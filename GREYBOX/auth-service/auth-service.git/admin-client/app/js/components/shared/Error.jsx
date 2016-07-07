'use strict';

var React = require('react');
var _ = require('lodash');

/**
 * Display an error message and/or HTTP response error.
 */
var ErrorView = React.createClass({
    render: function() {
        var detail;
        var status;

        if (this.props.httpResponse) {
            var resp = this.props.httpResponse;

            status = resp.statusText;

            if (!status && resp.status) {
                status = 'Error code: ' + resp.status;
            }

            if (resp.responseText) {
                /* jshint ignore:start */
                detail = <pre>{resp.responseText}</pre>;
                /* jshint ignore:end */
            }

            // Prefer JSON (structured) instead of text if available
            if (resp.responseJSON) {
                var json = resp.responseJSON;

                if (json.message) {
                    /* jshint ignore:start */
                    detail = <pre>{json.message}</pre>;
                    /* jshint ignore:end */
                } else if (json.errors) {
                    var errListKey = 0;

                    detail = _.map(json.errors, function(item) {
                        /* jshint ignore:start */
                        return <pre key={errListKey++}>{item}</pre>;
                        /* jshint ignore:end */
                    });
                } else {
                    /* jshint ignore:start */
                    detail = <pre>{JSON.stringify(json)}</pre>;
                    /* jshint ignore:end */
                }
            }
        }

        // Invisible when it has no content
        if (!this.props.message && !detail && !status) {
            return null;
        } else {
            /* jshint ignore:start */
            return (
                <div className="alert alert-danger">
                    <b>{this.props.message}</b>
                    { status ? <div><i>{status}</i></div> : '' }
                    {detail}
                </div>
            );
            /* jshint ignore:end */
        }
    }
});

module.exports = ErrorView;
