[//]: # (<- Header ->)
<p align="center" style="align: center; text-align: center">
<img align="center" alt="SkMail" width="40%" src="https://github.com/cooffeeRequired/SkriptMail/assets/106232282/04fd017f-2ab2-4d37-a8e2-042d22587408">
</p>
<h1 align="center">SkMail</h1>

<h3 align="center">Simple Mailer for Skript using SMTP/POP3/IMAP</h3>
<h6 align="center">The addon use SimpleMail Java API</h6>
<hr>

### Documentation
> Not exists yet, your that [Intro](#introduction) 

### Introduction
 SkMail is a very simple Script Mailer now you can send email directly from your minecraft server, but you don't have to just send plain text to someone on mail, **SkMail** can also use templates plus render variables in them

#### Lets create some account in our 'config.yml'
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
```
now we have predefined account set, lets works with account now, ex1. will show you have to get all your accounts from 'config.yml'
```applescript
    # ex1.
    set {_accounts::*} to configured emails accounts
    # lets say we wanna account 1 and we will print what account have
    set {_account} to {_accounts::1}

    broadcast id of {_account} # -> get id of our account for e.g. "my-secret-account"
    broadcast address of {_account}
    broadcast host of {_account}
    broadcast port of {_account}
    broadcast auth of {_account}
    broadcast starttls of {_account}
    broadcast service type of {_account}

    # we disallow broadcast authentication due security reason
    # get single account
    set {_account} to configured email account "my-secret-account"
```
#### Lets play with templates a bit
in the addon folder so `SkriptMail` you will found folder `templates` and also the predefined template `main.html`
all templates need to be a `.html`\
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
*What exatly mean the `{it::*}`*. The `{it::name}` and etc. its defigned pattern for a variable, lets say we wanna render any template dynamically and send it to the email. for e.g. some registration confirmation.\
As you can see at ***template example `main.html`*** we have two variables `{it::name}, {it::team}` this indicates that it is a variable that will be changed to a real value during template parsing \
So result of the parsing template will be
```diff
-    <p>Dear {it::name}</p>
+    <p>Dear Jorge</p>

-    <p>The Registration {it::team} Team</p>
+    <p>The Registration Skript Team</p>
```
How can we achieve this? First, let's talk a little bit about the `Email form`

### Email Form
The email form represents an object that has `recipients, subject, body or template` assigned to the email \
There are two ways to create an email form, using a predefined account or using a configuration string
```applescript
    # first way using an account
    set {_email} to new email using account "my-secret-account"
    # second way using a configuration string
    # this is example for smtp.gmail.com service
    set {_email} to new email with credentials "smtp:google.com:567@auth=true&starttls=true" using "test@gmail.com"
    # So now we have created empty email form lets assign some recipients, body/template and subject

    set recipients of {_email} to "some@gmail.com" and "some2@gmail.com"
    set subject of {_email} to "Registration success"
    # using a body, this method can't handle a template or HTML its only plain TEXT but you can use an Skript variables
    set body of {_email} to "There are new email for %player%" 

    # using template without data, this method will render the HTML in the mail but will not parse any variable it in means `{it::*}`
    set template of {_email} to email template "main"

    # using template with data, this method will render the HTML in the mail with data replacment
    set {_any::name} to "Jorge"
    set {_any::team} to "Skript"
    set template of {_email} to email template "main" with data {_any::*}

    # well, the last thing we need is to send the email and we can do that quite easily using `send email {_email}`... The email is always sent asynchronously!

    send email {_email}
    post email {_email}
    transmit email {_email}

    # this effect are the same..

```


### FAQ
> ❓**How can create new account?**\
> ㅤㅤ* You need to write an account to the `config.yml`
> 


> ❓**How can create new email from?**\
> ㅤㅤ* You can use an expression for that `set {_email} to new email using account "..."` or `set {_email} to new email with credentials "..." using "..."`
> 

> ❓**How can specify recipient of that email?**\
> ㅤㅤ* You can use an expression for that `set {_email}'s recipient to "crazy@gmail.com"`
> 

> ❓**How can send the email**\
> ㅤㅤ* You can use an effect for that `post email {_email}`, `send email {_email}` or `transmit email {_email}`
> 

> ❓**How can use gmail smtp**\
> ㅤㅤ* First of wall you need to have an account on `gmail.com`\
> ㅤㅤ* Second you need to go to **`Account->Security->App passcodes`**\
> ㅤㅤ* Create new passcode, the passcode may be something like **`**** **** **** ****`**\
> ㅤㅤ* Then you need to create an account in `config.yml`
> ```yml
>  example:
>    address: Skript-Mailer2;test@test.com
>    type: SMTP
>    host: smtp.gmail.com
>    port: 587
>    auth: true
>    starttls: true
>    auth-credentials:
>      username: <your gmail account>
>      password: <your passcode>
> ```
> ㅤㅤ* Then create an email form using than account `set {_email} to new email using account "example"`\
> ㅤㅤ* Then set `recipients\body\subject`\
> ㅤㅤ* And send the mail `post email {_emai}`\
> ㅤㅤ* Done! You Welcome.