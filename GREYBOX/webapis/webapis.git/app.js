var express      = require('express');
var https        = require('https');
var fs           = require('fs');
var path         = require('path');
var favicon      = require('serve-favicon');
var logger       = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser   = require('body-parser');
var config       = require('config');
var routes       = require('./routes/index');
var searchlight  = require('./routes/searchlight');
var utm          = require('./routes/utm');
var tpm          = require('./routes/tpm');

var app = express();

/**
  Setup application to listen
**/
var key     = fs.readFileSync(config.get('CertSettings.key'));
var cert    = fs.readFileSync(config.get('CertSettings.cert'));

var PORT    = config.get('Port');
var options = {
  key: key,
  cert: cert,
  secureProtocol: 'TLSv1_method',
  requestCert: true,
  rejectUnauthorized: false
};

https.createServer(options, app).listen(PORT);

console.log('Running on port ' + PORT);

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

// uncomment after placing your favicon in /public
//app.use(favicon(__dirname + '/public/favicon.ico'));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

/**
  Endpoints of interest
**/
app.use('/', routes);
app.use('/searchlight', searchlight);
app.use('/utm/jax-rs/:version/utm', utm);
app.use('/log', tpm);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
  app.use(function(err, req, res, next) {
    var status = err.status || 500;
    res.status(status);
    if (status == 422) {
      res.type('text/plain');
      res.send(err.message);
    } else {
      res.render('error', {
        message: err.message,
        error: err
      });
    }
  });
} else {
  // production error handler
  // no stacktraces leaked to user
  app.use(function(err, req, res, next) {
    var status = err.status || 500;
    res.status(status);

    if (status = 422) {
      res.type('text/plain');
      res.send(err.message);
    } else {
      res.render('error', {
        message: err.message,
        error: {}
      });
    }
  });
}

module.exports = app;
