module.exports = require("./make-webpack-config")({
    separateStylesheet: true,
    devServer: true,
    devtool: "eval",
    debug: true
});