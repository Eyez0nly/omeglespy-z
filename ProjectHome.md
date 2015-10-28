# Omegle and spying. #

**Get the new beta [here](http://omeglespy-z.googlecode.com/files/omeglespy-z-desktop-2.0.0-BETA-003-distribution.zip).**

**IMPORTANT NOTE TO WINDOWS USERS:** If you have Java installed and configured correctly, you should be able to run the application by simply double-clicking on the .JAR file included in the .ZIP file. If this does not work, please drop me a line: darkimport at gmail dot com.

This project consists of two main components. The first component is an application agnostic Java library ([omeglespy-z-core](https://omeglespy-z.googlecode.com/svn/sites/omeglespy-z-core/index.html)) for interfacing with omegle. It has the capability of generating multiple simultaneous connections to omegle and joining them together. It also has a facility for observing these joined conversations and allowing an implementing application to interject its own chat.

The second component of this project is a [desktop GUI application](https://omeglespy-z.googlecode.com/svn/sites/omeglespy-z-desktop/index.html) that utilizes the omeglespy-z-core library to pair 2 random strangers, essentially allowing you to spy on their conversation. Features include:

  * an easy to use interface
  * a cool icon
  * impersonate either stranger (the stranger you're impersonating has no idea!!!)
  * selectively disconnect strangers
  * swap out a stranger with a brand new stranger
  * block the strangers from chatting with each other; or not! You decide.
  * automatically disconnect chat bots. or people who ask "asl?" or people who talk about their cats.
  * keyboard shortcuts for common tasks

## Update 2012-07-12 ##

After a two month hiatus, I am back with a small update. This update addresses two main issues: Windows users can now use the app by just double clicking on the JAR file. Also, it seems that the omegle site changed slightly while I was away, breaking the application.  Well, that's fixed :)

## Update 2012-05-02 ##

Developers! I have good news: **Version 2.0.0 of omeglespy-z-core has been released.**

You can use this tiny library to interface with omegle in your own Java applications, either mediating conversations between two or more users or just creating your own standalone omegle client.

Additionally, I've been doing some experimentation with using omeglespy-z-core outside the JVM. .NET/Mono devs can use the omeglespy-z-core APIs thanks to [IKVM](http://www.ikvm.net/). I threw together a little standalone app as a proof of concept. Get the Windows version [here](http://omeglespy-z.googlecode.com/files/omeglespy-z-nospy-windows.zip), and get the Linux/Mac version [here](http://omeglespy-z.googlecode.com/files/omeglespy-z-nospy-linux.zip). Source is included.

## Update 2012-04-27 ##

I just posted new 2.0.0 betas. The core is pretty much done, and I just have a few niggling little issues in the UI to take care of before the final release.

This beta release fixes a couple of major issues:

  * The problem where you'd see a bunch of "Stranger has left" messages when a stranger was disconnected.
  * Auto scrolling wasn't enabled by default, and in some cases, it wasn't even working :/

Also, for any of you Windows folks who were having trouble starting the app, I included an alternate batch file (bin/omeglespy-z-alt.bat). Give it a shot, and let me know if it works.

Oh, yeah. And also I wanted to give a shout out to **Breckdareck**. He has just joined the team as our first official beta tester, and that means that he will be the first to have his name mentioned in the new about page.

Just a reminder, if you're interested in helping with the beta testing and getting your name and a picture in the app, let me know. You can mention me on twitter (**@darkimport**) or just shoot me an email.

## Update 2012-04-11 ##

The 2.0.0 beta is available for download. If you have an issue, please make a note of it on http://code.google.com/p/omeglespy-z/issues/list .

One of the main features of this beta is the retooling the connection script to reduce the occurrence of recaptcha challenges. HOWEVER, if you are currently experiencing a recaptcha with every connection, this will not help you (unless you change your external IP address). ADDITIONALLY, the application is NOT recaptcha proof. You MIGHT eventually be forced to recaptcha with every connection just like prior versions of the app. The modifications I made simply make it harder for omegle to detect your use of the app -- not impossible.

Other updates include:

  * automatically disconnecting a stranger if they type in crap about web cams.
  * allowing you to replace a disconnected stranger without ending the conversation.
  * keyboard shortcuts for common tasks.
  * miscellaneous other fun stuff.

Additionally, behind the scenes, I have:

  * fully re-architected the application. The UI code is now completely separate from the "business" code. Those of you with imaginations can guess why I took the time to do that.
  * I have externalized configuration elements so that we don't have to recompile the code in order to do things like add and remove omegle servers. However, I don't have a configuration front-end, yet, so if you need to make changes, you're going to have to do some exploring of the configuration files.

Also, check out the command line version of omeglespy-z. Yeah. I was bored :P

## Update 2012-03-21 ##

### Beta testers needed!!! ###

The core features of 2.0.0 are almost complete, but I'm sure that I've left behind some bugs. Help me improve omeglespy-z by signing up to become a private beta tester. Perks include a mention on the app's About page, and if you find more than three issues, I'll put a small picture of your choice next to your name (nothing pornographic, fecal, or snuffy, though -- that applies to names, too :P).

In order to sign up, send me an email or mention me on twitter (@darkimport).

## Update 2012-03-15 ##

Version 2.0.0 is currently in development. Planned end-user features include:

  * disconnecting a stranger if they type in crap about web cams.
  * allowing you to replace a disconnected stranger without ending the conversation.
  * keyboard shortcuts for common tasks.
  * retooling the connection script to reduce the occurrence of recaptcha challenges
  * miscellaneous other fun stuff.

Additionally, behind the scenes, I'll be:

  * making further departures from the wonkiness of the original architecture
  * externalizing configuration elements so that we don't have to recompile the code in order to do things like add and remove omegle servers.


---

Special rememberance to

  * sfoley1988 for conceiving the original omegle-spy (http://code.google.com/p/omegle-spy/) before he disappeared under mysterious circumstances.
  * BrentBXR for keeping omegle-spy alive as omeglespyx (before his own untimely disappearance)  (http://code.google.com/p/omeglespyx/) after sfoley1988 disappeared

The project icon is based on [Viper & Sun](http://openclipart.org/detail/3697/viper-&-sun-by-stranger) by [StRanger](http://openclipart.org/user-detail/StRanger). If anyone has access to a better public domain or creative commons icon that would suit this project, please let me know.

Also, I **need** some help **translating** the app to other languages. If you are a native speaker of a non-English language (I know you're out there Chinese and Indians) and you have some time to do some **unpaid** translation work, drop me a line. If you do well, I'll credit you on this page and in the app. Forever IMMORTALIZED in omeglespy-z!

-- darkimport