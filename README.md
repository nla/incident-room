Incident Room
=============

**Status:** Incomplete (unusable)

Incident Room is a combination status website and IRC bot which makes
communication easier during incident response. It provides a placeto
display status updates to end users and a sitrep page including chat
history to get newly arriving responders up to speed quickly.

IRC Commands
------------

### !ir topic

Starts a new incident response.

    <sam> !ir Site is down
    * irbot changed the topic of #ir: [IR-27] Site is down | IC: sam | Sitrep: http://status.example.org/ir/27

If the channel is in use, irbot will suggest a second one.

    <elvis> !ir Stove is busted
    <irbot> #ir is currently reserved for "[IR-27] Site is down". Please join #ir2 or resolve the current incident.

### !topic new-topic

Renames the current incident.

    <sam> !topic Site is slow
    * irbot changed the topic of #ir: [IR-27] Site is slow | IC: sam | Sitrep: http://status.example.org/ir/27

### !invite user1 msg

Invite's a responder to the incident room.

    <sam> !invite penny Looks like a db problem. Can you give me hand?
    <irbot> Email sent.

Penny will be sent an email asking her to join the IRC room and read the incident history.

    To: Penny Morris
    Subject: Assitance request: [IR-27] Site is down

    Sam has requested your assistance with an ongoing incident response effort.

    > Looks like a db problem. Can you give me a hand?

    Please join IRC channel #ir. To catch up on the sitrep and chat history see the [IR page].

### !status msg

Provides a status update to end users.

    <sam> !status A workaround is in place and the site's online but slow. Work
                  on adressing the root cause is continuing.

### !nextupdate time

Set an estimated time for the next status update. The bot will stop prompting
update reminders until then.

    <sam> !nextupdate 2 hours
    <irbot> Next update estimate set for 2 hours. I will remind you 5 minutes before then.

### !resolve

Mark the incident as resolved. The IC will be reminded to complete an incident report.

    <sam> !resolve
    * irbot changed the topic of #ir: No active incident | Previous: https://status.example.org/ir/27
    <irbot> sam: Please complete the incident report or transfer IC at https://status.example.org/ir/27/report. I will remind you by email in one day.
