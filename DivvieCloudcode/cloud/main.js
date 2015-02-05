
Parse.Cloud.define("sendChargeEmail", function(request, response) {
    var toEmail = request.params.toEmail;
    var toName = request.params.toName;
    var fromName = request.params.fromName;
    var groupName = request.params.groupName;
    var chargeDescription = request.params.chargeDescription;
    var amount = request.params.amount;

    var Mandrill = require('mandrill');
    Mandrill.initialize('M5f77U0mhIWJxzMWFDhBkw');
    Mandrill.sendEmail({
        message: {
        subject: "You have been charged " + amount + ".",
        text: fromName + " has charged your group " + groupName + " for " + chargeDescription + ".",
        from_email: "divvie-no-reply@parseapps.com",
        from_name: "Divvie App",
        to: [{
                email: toEmail,
                name: toName
            }]
        },
        async: true
    }, {
        success: function(httpResponse) { response.success("Email sent!"); },
        error: function(httpResponse) { response.error("Uh oh, something went wrong"); }
    });
});

Parse.Cloud.define("sendNewUserEmail", function(request, response) {
    var toEmail = request.params.toEmail;
    var fromName = request.params.fromName;
    var groupName = request.params.groupName;

    var Mandrill = require('mandrill');
    Mandrill.initialize('M5f77U0mhIWJxzMWFDhBkw');
    Mandrill.sendEmail({
        message: {
        subject: fromName + " has added you to their group " + groupName ,
        text: "You have been added to " + fromName + "'s group on Divvie. Download the Android " +
        "Divvie app from the Google Play store to easily view, pay, create, and manage group " +
        "charges.",
        from_email: "divvie-no-reply@parseapps.com",
        from_name: "Divvie App",
        to: [{
                email: toEmail,
                name: toEmail
            }]
        },
        async: true
    }, {
        success: function(httpResponse) { response.success("Email sent!"); },
        error: function(httpResponse) { response.error("Uh oh, something went wrong"); }
    });
});

Parse.Cloud.define("getTransactionsDescending", function(request, response) {
    var query = new Parse.Query("Transaction");
    query.equalTo("users", request.params.currentUser);
    query.descending("createdAt");
    query.find({
        success: function(results) { 
            console.log("getTransactionsDescending found " + results.length + "results.");
            response.success(results);
        }
    });
});    