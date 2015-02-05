
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
        text: "You have been charged " + amount + ".",
        subject: fromName + " has charged your group " + groupName + " for " + chargeDescription + ".",
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