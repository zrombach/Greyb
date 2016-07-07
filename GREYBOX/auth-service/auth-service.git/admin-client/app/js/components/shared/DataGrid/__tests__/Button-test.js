jest.dontMock('../Button.jsx');

var React = require('react/addons');
var Button = require('../Button.jsx');
var TestUtils = React.addons.TestUtils;

describe('button', function () {
    it('renders the correct name and default class', function () {
        var btn = TestUtils.findRenderedDOMComponentWithTag(
            TestUtils.renderIntoDocument(
                <Button name="bob" />
            ),
            'button'
        );

        expect(btn.getDOMNode().textContent).toBe('bob');
        expect(btn.getDOMNode().className).toBe('btn btn-default');
    });

    it('has the correct class when specified', function () {
        var btn = TestUtils.findRenderedDOMComponentWithTag(
            TestUtils.renderIntoDocument(
                <Button name="bob" type="primary" />
            ),
            'button'
        );

        expect(btn.getDOMNode().className).toBe('btn btn-primary');
    });

    it('calls onClick', function () {
        var fn = jest.genMockFunction()

        var btn = TestUtils.findRenderedDOMComponentWithTag(
            TestUtils.renderIntoDocument(
                <Button name="bob" type="primary" onClick={ fn } />
            ),
            'button'
        );

        TestUtils.Simulate.click(btn);

        expect(fn).toBeCalled();
    });
})
