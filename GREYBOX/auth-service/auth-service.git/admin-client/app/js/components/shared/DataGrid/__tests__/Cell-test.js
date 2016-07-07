jest.dontMock('../Cell.jsx');

describe('Cell', function () {
    it('correctly renders a string value', function () {
        var React = require('react/addons');
        var Cell = require('../Cell.jsx');
        var TestUtils = React.addons.TestUtils;

        var data = 'this is a test!'
        var cell = TestUtils.renderIntoDocument(
            <Cell dataValue={ data } />
        );

        var td = TestUtils.findRenderedDOMComponentWithTag(cell, 'td');

        expect(td.getDOMNode().textContent).toEqual(data);
    });
});
