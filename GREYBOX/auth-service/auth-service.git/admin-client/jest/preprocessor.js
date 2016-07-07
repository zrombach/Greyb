var ReactTools = require('react-tools');

module.exports = {
    process: function(src, file) {
        return /node_modules/.test(file) ? src : ReactTools.transform(src, { harmony: true });
    }
};
