
Parse.Cloud.define("registerUser", function(request, response) {
    var email = request.params.email;
    var password = request.params.password;
    var fullName = request.params.fullName;

    // Register the user
    var newUser = new Parse.User();
    newUser.set("username", email);
    newUser.set("password", password);
    newUser.set("email", email);
    newUser.set("fullName", fullName);
    newUser.signUp(null, {
      success: function(user) {
        // Replace email with their fullName in the displayNames array for any group they are in
        var groupsQuery = new Parse.Query("Group");
        groupsQuery.equalTo("displayNames", email);
        groupsQuery.find({
            success: function(results) {
                console.log("Replace displayName in " + results.length + " groups.");
                for (var i = 0; i < results.length; i++) {
                    var result = results[i];
                    // Find them in the displayNames array and replace with their full name
                    var groupDisplayNames = result.get("displayNames");
                    for (var j = 0; j < groupDisplayNames.length; j++) {
                        if (groupDisplayNames[j] === email) {
                            groupDisplayNames[j] = fullName;
                            result.save(null, {
                                success: function(result) {
                                    result.set("displayNames", groupDisplayNames);
                                    result.save();
                                    console.log("Saved new group displayName");
                                }
                            });
                        }
                    }
                }

                // Replace email with their fullName in the displayNames array for any transaction they are in
                var transactionsQuery = new Parse.Query("Transaction");
                transactionsQuery.equalTo("displayNames", email);
                transactionsQuery.find({
                    success: function(results) {
                        console.log("Replaced displayName in " + results.length + " transactions.");
                        for (var i = 0; i < results.length; i++) {
                            var result = results[i];
                            // Find them in the displayNames array and replace with their full name
                            var transactionDisplayNames = result.get("displayNames");
                            for (var j = 0; j < transactionDisplayNames.length; j++) {
                                if (transactionDisplayNames[j] === email) {
                                    transactionDisplayNames[j] = fullName;
                                    result.save(null, {
                                        success: function(result) {
                                            result.set("displayNames", groupDisplayNames);
                                            result.save();
                                            console.log("Saved new transaction displayName");
                                        }
                                    });
                                }
                            }
                        }
                        response.success();
                    },
                    error: function(results, error) {
                        console.log("Transasctions Error: " + error.code + " " + error.message);
                    }
                });
            },
            error: function(results, error) {
                console.log("Groups Error: " + error.code + " " + error.message);
            }
        });
      },
      error: function(user, error) {
        // Show the error message somewhere and let the user try again.
        console.log("Register Error: " + error.code + " " + error.message);
        response.error(error.code);
      }
    });
});

Parse.Cloud.define("sendInviteEmails", function(request, response) {
    var key = request.params.key;

    var toEmail = request.params.toEmail;
    var fromName = request.params.fromName;
    var groupName = request.params.groupName;

    var Mandrill = require('mandrill');
    Mandrill.initialize(key);
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

Parse.Cloud.define("sendChargeEmail", function(request, response) {
    var key = request.params.key;

    var toEmail = request.params.toEmail;
    var toName = request.params.toName;
    var fromName = request.params.fromName;
    var groupName = request.params.groupName;
    var chargeDescription = request.params.chargeDescription;
    var amount = request.params.amount;

    var Mandrill = require('mandrill');
    Mandrill.initialize(key);
    Mandrill.sendEmail({
        message: {
        subject: "You have been charged " + amount,
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

Parse.Cloud.define("sendReminderEmail", function(request, response) {
    var key = request.params.key;

    var toEmail = request.params.toEmail;
    var toName = request.params.toName;
    var fromName = request.params.fromName;
    var chargeDescription = request.params.chargeDescription;

    var Mandrill = require('mandrill');
    Mandrill.initialize(key);
    Mandrill.sendEmail({
        message: {
        subject: "Payment Reminder",
        text: "This is a reminder that you owe " + fromName + " for " + chargeDescription + ".",
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