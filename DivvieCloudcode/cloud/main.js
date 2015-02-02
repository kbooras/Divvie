
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});

Parse.Cloud.define("hi", function(request, response) {
  var Mandrill = require('mandrill');
  Mandrill.initialize('M5f77U0mhIWJxzMWFDhBkw');
  Mandrill.sendEmail({
    message: {
      text: "Hello World!",
      subject: "Using Cloud Code and Mandrill is great!",
      from_email: "divvie-no-reply@parseapps.com",
      from_name: "Divvie App",
      to: [
        {
          email: "kirstiebooras@gmail.com",
          name: "Kirstie Booras"
        }
      ]
    },
    async: true
  }, {
    success: function(httpResponse) { response.success("Email sent!"); },
    error: function(httpResponse) { response.error("Uh oh, something went wrong"); }
  });
});