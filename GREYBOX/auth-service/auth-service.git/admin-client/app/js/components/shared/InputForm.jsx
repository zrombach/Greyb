'use strict';

var React = require('react');
var Reflux = require('reflux');
var ErrorView = require('./Error.jsx');
var { Navigation } = require('react-router');

/**
 * Displays a tcomb-form and provides a submission button and error display
 * field that can show details from a HTTP response.
 */
var InputForm = React.createClass({
    mixins: [Reflux.ListenerMixin, Navigation],

    propTypes: {
        // Object created by tcomb-form.form.create(...)
        tcombForm: React.PropTypes.func.isRequired,

        // Object with values to pre-populate form fields
        value: React.PropTypes.object,

        // Text to display in submit button. If not provided will take on a
        // default value of 'Add' if the 'value' property is falsy or 'Save'
        // otherwise.
        buttonText: React.PropTypes.string,

        // Action to trigger when submit button is pressed and input passes
        // form validation. Will be passed an object containing all values
        // entered in the form.
        onSubmit: React.PropTypes.func,

        // Action to trigger when the form detects an input validation
        // error.
        onValidationError: React.PropTypes.func,

        // Provides a way for another component to notify this one that
        // there was an error with the form values that were provided to the
        // 'onSubmit' action. Accepts a HTTP response object as a parameter.
        errorAction: React.PropTypes.func,

        // Message that will be displayed when 'errorAction' is triggered.
        // If provided this will be shown in the error display area before
        // the details section.
        errorMessage: React.PropTypes.string,

        // Override cancel button action. By default said button will
        // navigate back to the previous route.
        onCancel: React.PropTypes.func,
    },

    getInitialState: function() {
        // Value rendered in form will be based on state instead of prop so
        // it will not be reset if/when error display is rendered.
        return { value: this.props.value || { } };
    },

    componentDidMount: function() {
        if (this.props.errorAction) {
            // Register to listen for error reported by another component
            this.listenTo(this.props.errorAction, function(httpResponse) {
                this.setState({
                    httpResponse: httpResponse,
                    showError: true
                });
            });
        }
    },

    render: function() {
        var FormInstance = this.props.tcombForm;
        var buttonText = this.props.buttonText ||
                         (this.props.value ? 'Save' : 'Add');

        /* jshint ignore:start */
        return (
            <form>
                <FormInstance ref="form" value={this.state.value} />
                { this.state.showError &&
                    <ErrorView
                        message={this.props.errorMessage}
                        httpResponse={this.state.httpResponse} />
                }
                <div className="btn-group">
                    <button className="btn btn-primary"
                        onClick={this.onSubmit}>{buttonText}</button>
                    <button className="btn"
                        onClick={this.onCancel}>Cancel</button>
                </div>
            </form>
        );
        /* jshint ignore:end */
    },

    onSubmit: function(evt) {
        evt.preventDefault();

        // NOTE: getValue() returns null on validation errors
        var formData = this.refs.form.getValue();

        if (formData) {
            // Backup state before calling button action to ensure form will
            // not be cleared if/when error message is rendered. Also clear
            // previous error.
            this.setState({
                value: formData, showError: null, httpResponse: null
            });

            if (this.props.onSubmit) {
                // Send form data to optional handler
                this.props.onSubmit(formData);
            }
        } else if (this.props.onValidationError) {
            // Notify component that data failed to validate
            this.props.onValidationError();
        }
    },

    onCancel: function(evt) {
        evt.preventDefault();

        // Allow default cancel action to be overridden
        if (this.props.onCancel) {
            this.props.onCancel();
        } else {
            this.goBack(); // Navigate to previous route
        }
    }
});

module.exports = InputForm;
