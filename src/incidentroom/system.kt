package incidentroom

import java.net.InetAddress
import java.util.*
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class IncidentRoomSystem(val dbPool: DbPool, val url: String) {

    fun createIncident(topic: String, channel: String): Incident {
        val created = System.currentTimeMillis()
        val id = dbPool.take { it.insertIncident(IncidentState(0, topic, channel, created)) }
        return Incident(this, IncidentState(id, topic, channel))
    }

    fun findIncidentForChannel(channel: String) : Incident? {
        val state = dbPool.take { it.findIncidentByChannel(channel) }
        return if (state != null) Incident(this, state) else null;
    }
}

class Incident(val system: IncidentRoomSystem, val state : IncidentState) {
    val id : Long get() = state.id
    val topic : String get() = state.topic
    val qid : String get() = "ID-$id"

    val channel: String
        get() = state.channel

    val url: String
        get() = "${system.url}/ir/$id"

    fun updateStatus(message: String, creator: String) {
        system.dbPool.take { it.insertStatus(id, message, creator, System.currentTimeMillis()) }
    }

    fun sendInvite(from: String, to: String, message: String) {
        val session = Session.getDefaultInstance(Properties())
        val email = MimeMessage(session)
        email.setFrom(InternetAddress("incidentroom@" + InetAddress.getLocalHost().hostName, "Incident Room"))
        email.addRecipient(MimeMessage.RecipientType.TO, InternetAddress(to))
        email.setSubject("Assistance request: [$qid]: $topic")
        email.setText("""
        $from has requested your assistance with an ongoing incident response effort.

        > $message

        Please join IRC channel $channel. To catch up on the situation see: $url
        """.trimIndent())
        Transport.send(email)
    }
}