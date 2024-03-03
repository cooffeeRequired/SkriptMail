[//]: # (<- Header ->)
<p style="align: center; text-align: center">
<img alt="SkMail" width="40%" src="https://github.com/cooffeeRequired/SkriptMail/assets/106232282/04fd017f-2ab2-4d37-a8e2-042d22587408">
</p>
<h1 style="align: center;">SkMail</h1>

<h3 align="center">Simple Mailer for Skript using SMTP/POP3/IMAP</h3>
<h6 align="center">The addon use SimpleMail Java API</h6>
<hr>

## Docs stuff
There was some stuff I saw in the like, expression descriptions and stuff like that which would be updated if you want? Not a massive deal or anything. I'd be happy to go through the annotations if you'd like?


## Documentation
> [SkripHub](https://skripthub.net/docs/?addon=SkMail )

## Introduction
> [!NOTE]
> **SkMail** is a very simple Skript Mailer - now you can send email directly from your Minecraft server! And it doesn't have to just send plain text, SkMail can also use `templates` plus render variables in them.

### Creating an account in config.yml
Creating an account is a fairly simple. The following is an example of an account in the `config.yml`.
```yml
project-debug: true
email-debug: true
accounts:
    my-secret-account:
        address: Skript-Mailer;skript-mailer@non-reply.com
        type: SMTP
        host: smtp.gmail.com
        port: 587
        auth: true
        starttls: true
        auth-credentials:
            username: Test@gmail.com
            password: "password"
mailbox:
    enabled: true # default false
    folders:
      - something
    filter: # regex
    refresh-rate: 1000
    rate-unit: SECONDS # MILISECONDS, SECONDS, MINUTES, HOURS
    max-fetch-per-request: 20 # default 20
```
Now that we have a predefined account set, let's see how it works. First, we have to get the account, which we can do two different ways:
```applescript
set {_accounts::*} to configured emails accounts # gets all the accounts in config.yml
set {_account} to {_accounts::1} # gets the first one

# OR

set {_account} to configured email account "my-secret-account"
```
And now that we have the account, let's take a look at the expressions that can be used on said account:
```applescript
id of {_account}
address of {_account}
host of {_account}
port of {_account}
auth of {_account}
starttls of {_account}
service type of {_account}
# note that the auth-credentails are disallowed due to security reasons
```

### Creating templates
In the addon folder (`SkriptMail`) you'll see a folder called `templates`, as well as the predefined template `main.html`. All templates need to be of this `.html` type. Here's a fairly basic example of one:
```html
<style>
    /* Reset CSS */
    body,
    html {
        margin: 0;
        padding: 0;
        font-family: Arial, sans-serif;
        font-size: 16px;
        line-height: 1.6;
    }

    /* Container for email */
    .container {
        max-width: 600px;
        margin: 0 auto;
        padding: 20px;
    }

    /* Styles for headings */
    h1,
    h2,
    h3 {
        margin-bottom: 20px;
    }

    /* Styles for paragraphs */
    p {
        margin-bottom: 20px;
    }

    /* Styles for buttons */
    .button {
        display: inline-block;
        padding: 10px 20px;
        background-color: #007bff;
        color: #fff;
        text-decoration: none;
        border-radius: 5px;
    }

    /* Responsive images */
    img {
        max-width: 100%;
        height: auto;
    }
</style>

<div class="container">
    <h1>Registration Confirmation</h1>
    <p>Dear {it::name}</p>
    <p>Thank you for registering with us. Your account has been successfully created.</p>
    <p>Please feel free to explore our website and let us know if you have any questions.</p>
    <p>Best regards,</p>
    <p>The Registration {it::team} Team</p>
</div>
```
> [!IMPORTANT]  
> **What exactly does the `{it::*}` mean?**
>
> The `{it::name}` is a placeholder for a variable, which let's us render any template dynamically.
> In the above template, we have two variable placeholders: `{it::name}` and `{it::team}`. These indicate that it will be changed to a real value when parsed.
> For example, after parsing the template, the result could be
```diff
-    <p>Dear {it::name}</p>
+    <p>Dear Jorge</p>

-    <p>The Registration {it::team} Team</p>
+    <p>The Registration Skript Team</p>
```
For more info on placeholder variables, click [here](#placeholder-variables). Now that we've (briefly) covered the templates, let's talk a little bit about the `Email Form`.

### Email Form
The email form represents an object that has `recipients`, a `subject`, and a `body` or `template`.
There are two ways to create an email form. The first is by using the predefined accounts we talked about earlier:
```applescript
set {_email} to new email form using account "my-secret-account"
set {_email} to new email form using account {_account}
```
And the second is by using a `configuration string`:
```applescript
# this is example for smtp.gmail.com service
set {_email} to new email form with credentials "smtp:google.com:567@auth=true&starttls=true" using "test@gmail.com"
```
So we now have an empty email form. Let's assign some of those fields we mentioned earlier:
```applescript
set recipients of {_email} to "some@gmail.com" and "some2@gmail.com"
set subject of {_email} to "Registration Success"
set body of {_email} to "There are new email for %player%"
```
Well, this email is ready to send, so the last thing we need to do is just that... send the email! We can do that quite easily using `send email {_email}`. And don't worry, the email is always sent asynchronously!

### Using templates
#### Placeholder Variables
There are a few things to note about how templates work. An important feature when using templates are the placeholder variables - they're incredibly useful. First things first, when you're defining a placeholder variable in the template itself, it ***has*** to be an index nested under the `it` index. Let's have a look at what would be a valid and invalid definition of a placeholder variable in the template:
```diff
+ {it::name}
+ {it::otherIndex::prefix}
- {name}
- {otherIndex::it::prefix}
```

#### Setting the template of an email form
Let's take a look at how we can set the template of an email form without any placeholder variables:
```applescript
set template of {_email} to email template "main"
```
Pretty simple stuff, right? And it doesn't get much trickier when providing data to be dynamically rendered in. Let's take a look at another example, assuming that `{it::name}` and `{it::team}` are both included in the template:
```applescript
set {_any::name} to "CoffeeRequired"
set {_any::team} to "SkriptLang"
set template of {_email} to email template "main" with data {_any::*}
```
As you can see, pretty simple stuff. Just make sure that the indexes of the list that you provide as data match the indexes of the placeholder variables defined in the template. Also, if a placeholder variable defined in the template is not provided in the data to said template, it just remains unchanged. That pretty much covers setting the template, so let's move on.

> [!IMPORTANT]  
> **Body/Template Priority**
>
> You might've noticed in the email form example that we didn't set the template, we only set the body. That's because only one of those can be set at a time, either the body *or* the template. Additionally, a template will always take priority over a body. Let's take a look at the following example:
> ```applescript
> set template of {_email} to {_template}
> set body of {_email} to "example body"
> ```
> In the above example, even though the body of the email is set *after* the template, the template will still be used, because it takes priority. In order to use a body instead, you have to first `reset` the template of the email: `reset body of {_email}`.
> 
### Handeling Mailbox
first we need to register a mailbox service we will do it thanks
```applescript
register new service with id "test" and using account configured email account "example"
```
> [!IMPORTANT]  
> always remember to terminate the service
> ```applescript
> unregister email service for id "test"
> ```
#### Receiving messages
If we have registered the service we can listen to it using `on email receive`
```applescript
    send event-id to console
    send event-subject to console
    send event-message to console
    send event-recipient to console
    send event-received date to console
    send event-sender to console
```
And lastly, working with inbox
```applescript
set {_mail} to first message of service "test"
set {_mail} to last message of service "test"
set {_mais::*} to first 10 messages of service "test"
set {_mais::*} to last10 messages of service "test"
```

## FAQ
> [!TIP]
> ❓How can I create a new account?
> You need to create an account in the `config.yml`. See [Creating an account in config.yml](#creating-an-account-in-configyml) for more information.

> [!TIP]
> ❓How can I use Gmail SMTP?
> 1. Have an account on `gmail.com`
> 2. Go to `Account->Security->App passcodes`
> 3. Create new passcode, the passcode may be something like `**** **** **** ****`
> 4. Create an account in `config.yml`. It should look something like this:
> ```yml
> example:
>    address: Skript-Mailer2;test@test.com
>    type: SMTP
>    host: smtp.gmail.com
>    port: 587
>    auth: true
>    starttls: true
>    auth-credentials:
>        username: <your gmail account>
>        password: <your passcode>
> ```
> 5. Create an email form using that account
> 6. Set the recipients, subject and body/template of said email form
> 7. Send away!
