jest.dontMock('../ColumnHeader.jsx');

var React = require('react/addons');
var ColumnHeader = require('../ColumnHeader.jsx');
var TestUtils = React.addons.TestUtils;

function renderHeader (props) {
    return TestUtils.renderIntoDocument(
        <ColumnHeader { ...props } />
    );
}

function testSort(sortOrder) {
    return function () {
        var props = { title: 'this is a test!', sortOrder: sortOrder };

        var header = renderHeader(props);
        var icon = TestUtils.findRenderedDOMComponentWithTag(header, 'i');
        var th = TestUtils.findRenderedDOMComponentWithTag(header, 'th');

        expect(th.getDOMNode().textContent).toBe(props.title);
        expect(icon.getDOMNode().className).toBe('fa fa-sort-' + sortOrder);
    }
}

describe('ColumnHeader', function () {
    it('correctly renders the column title', function () {
        var props = { title: 'this is a test!' };

        var th = TestUtils.findRenderedDOMComponentWithTag(
            renderHeader(props),
            'th'
        );

        expect(th.getDOMNode().textContent).toEqual(props.title);
    });

    it('does not render sort icon when not given sortOrder', function () {
        var props = { title: 'this is a test!' };

        var icons = TestUtils.scryRenderedDOMComponentsWithTag(
            renderHeader(props),
            'i'
        );

        expect(icons.length).toBe(0);
    });

    it('correctly renders with asc sort icon', testSort('asc'));

    it('correctly renders with desc sort icon', testSort('desc'));

    it('calls handlesort when clicked', function () {
        var props = {
            title: 'this is a test!',
            sortOrder: 'asc',
            handleSort: jest.genMockFunction()
        };

        var th = TestUtils.findRenderedDOMComponentWithTag(
            renderHeader(props),
            'th'
        );

        TestUtils.Simulate.click(th);

        expect(props.handleSort).toBeCalled();
    });
});
