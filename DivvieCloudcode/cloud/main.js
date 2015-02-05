
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

/*Parse.Cloud.define("getMemberNames", function(request, response) {
    var members = request.params.members;
    var membersWithNames = new Array(members.length);

    for (var i = 0; i < members.length; i++) {
        Parse.Cloud.useMasterKey();
        var query = new Parse.Query(Parse.User);
        console.log("member to find: " + members[i]);
        query.equalTo("email", members[i]);
        query.find({
            success: function(results) {
                if (results.length == 0) {
                    console.log("Empty results");
                    membersWithNames[i] = members[i];
                } else {
                    membersWithNames[i] = results[0].get("fullName");
                    console.log("Empty results");
                }
            },
            error: function() {
              response.error("Query failed. Error = " + error.message);
            }
          });
    }

    response.success(membersWithNames);
});*/