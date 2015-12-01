package incidentroom

import com.googlecode.flyway.core.Flyway
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import droute.nanohttpd.NanoServer
import org.pircbotx.Channel
import org.pircbotx.Configuration
import org.pircbotx.PircBotX
import org.pircbotx.UtilSSLSocketFactory
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.MessageEvent
import org.pircbotx.hooks.types.GenericMessageEvent
import org.skife.jdbi.v2.DBI
import java.io.Closeable
import java.nio.channels.ServerSocketChannel
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.net.ssl.SSLSocketFactory
import javax.sql.DataSource

class IrcBot(val system: IncidentRoomSystem) : ListenerAdapter<PircBotX>() {
    override fun onMessage(event: MessageEvent<PircBotX>) {
        val words = event.message.split(' ')
        val rest = words.subList(1, words.size).joinToString(" ")
        val incident = system.findIncidentForChannel(event.channel.name)

        if (incident != null) {
            when (words[0]) {
                "!ir" -> event.respond("This channel is currently reserved for ${incident.qid}: ${incident.topic}")
                "!status" -> incident.updateStatus(rest, event.user.nick)
                "!invite" -> incident.sendInvite(event.user.nick, words[1], rest)
            }
        } else {
            when (words[0]) {
                "!ir" -> {
                    val incident = system.createIncident(rest, event.channel.name)
                    event.respond("Started incident ${incident.qid}")
                    event.channel.send().setTopic("[${incident.qid}] ${incident.topic}")
                }
                else -> event.respond("No incident in progress. Start one with: !ir <topic>")
            }
        }
    }
}

