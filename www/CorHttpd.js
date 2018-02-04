cordova.define("com.rjfun.cordova.httpd.CorHttpd", function(require, exports, module) {

var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec');

var corhttpd_exports = {};

corhttpd_exports.startServer = function(options, success, error) {
	//alert("bla2");
	  var defaults = {
			    'www_root': '',
			    'port': 8888,
			    'localhost_only': false
			  };
	  
	  // Merge optional settings into defaults.
	  for (var key in defaults) {
	    if (typeof options[key] !== 'undefined') {
	      defaults[key] = options[key];
	    }
	  }
			  
  exec(success, error, "CorHttpd", "startServer", [ defaults ]);
};

corhttpd_exports.stopServer = function(success, error) {
	  exec(success, error, "CorHttpd", "stopServer", []);
};

corhttpd_exports.getURL = function(success, error) {
	  exec(success, error, "CorHttpd", "getURL", []);
};

corhttpd_exports.getLocalPath = function(success, error) {
	  exec(success, error, "CorHttpd", "getLocalPath", []);
};


corhttpd_exports.onRequest = function (success, error) {
 // alert("onRequest(): platforms\\android\\platform_wwww\\plugins\\...\\CorHttpd.js");

  exec(success, error, "CorHttpd", "onRequest", []);
}

corhttpd_exports.sendResponse = function (requestId, params, success, error) {
  //alert("sendResponse(): platforms\\android\\platform_wwww\\plugins\\cordova-plugin-webserver\\webserver");
  //(0, _exec2.default)(success_callback, error_callback, WEBSERVER_CLASS, SENDRESPONSE_FUNCION, [requestId, params]);


  exec(success, error, "CorHttpd", "sendResponse", [requestId, params]);
}

module.exports = corhttpd_exports;
//exports=corhttpd_exports;

});