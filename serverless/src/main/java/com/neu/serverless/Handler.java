package com.neu.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class Handler implements RequestHandler<SNSEvent, Object> {

    @Override
    public Object handleRequest(SNSEvent req, Context context) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());

        context.getLogger().log("Invocation Started: " + timeStamp);

        context.getLogger().log("Request is NULL: "+ (req == null));

        context.getLogger().log("Number of Records: " + (req.getRecords().size()));

        String record = req.getRecords().get(0).getSNS().getMessage();

        context.getLogger().log("Request Message: " + record);


        Map<String, SNSEvent.MessageAttribute> map = req.getRecords().get(0).getSNS().getMessageAttributes();

        context.getLogger().log("User firstname " + map.get("firstName").getValue());

        String firstName = map.get("firstName").getValue();
        String domainName = map.get("domainName").getValue();
        String to = map.get("emailId").getValue();
        String token = map.get("token").getValue();

        String from = "noreply@" + domainName;

        String link = "https://" + domainName + "/v1/verifyUserEmail?email=" + to + "&token=" + token;

        String message = "Hi " + firstName + ",  \n\n" +
                "Below is the verification link \n\n" +
                link + "\n\n\n\n Regards, \n" + domainName + " \n";

        String subject = "Email Verification";


        try {
            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            .withRegion(Regions.US_EAST_1).build();
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(to))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(message)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(subject)))
                    .withSource(from);
            client.sendEmail(request);
            System.out.println("Email sent!");
        } catch (Exception ex) {
            System.out.println("The email was not sent. Error message: "
                    + ex.getMessage());
        }

        context.getLogger().log("Invocation completed: " + timeStamp);
        return null;
    }
} 