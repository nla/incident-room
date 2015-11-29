package irbot

import org.pircbotx.Channel
import org.pircbotx.Configuration
import org.pircbotx.PircBotX
import org.pircbotx.UtilSSLSocketFactory
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.MessageEvent
import org.pircbotx.hooks.types.GenericMessageEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.net.ssl.SSLSocketFactory

class Incident(val id: Long, var topic: String) {
    val created = Date()
    var channel : Channel? = null
    val qid = "IR-$id"

    fun start(channel: Channel) {
        this.channel = channel
        say("Starting [$qid] $topic")
        channel.send().setTopic("[$qid] $topic")
    }

    fun setStatus(status: String) {
        throw UnsupportedOperationException("not implemented")
    }

    fun invite(s: String) {
    }

    private fun say(text: String) {
        channel?.send()?.message(text)
    }
}

class Irbot : ListenerAdapter<PircBotX>() {
    var lastId = 0L
    val incidentsByChannel = ConcurrentHashMap<String, Incident>()

    override fun onMessage(event: MessageEvent<PircBotX>) {
        val words = event.message.split(' ')
        val rest = words.subList(1, words.size).joinToString(" ")
        val incident = incidentsByChannel[event.channel.name]

        if (incident != null) {
            when (words[0]) {
                "!ir" -> {
                    event.respond("This channel is currently reserved for ${incident.qid}: ${incident.topic}")
                }
                "!status" -> incident.setStatus(rest)
                "!invite" -> incident.invite(words[1])
            }
        } else {
            when (words[0]) {
                "!ir" -> {
                    val incident = Incident(++lastId, rest)
                    incidentsByChannel.put(event.channel.name, incident)
                    incident.start(event.channel)
                }
                else -> event.respond("No incident in progress. Start one with: !ir <topic>")
            }
        }
    }

}

fun main(args: Array<String>) {
    val config = Configuration.Builder<PircBotX>()
    config.setAutoReconnect(true)
    config.setAutoNickChange(true)
    config.setRealName("Incident Room Bot")
    config.setServerHostname("localhost")
    config.setName("irbot")
    config.setLogin("irbot")
    config.addListener()

    var i = 0
    while (i < args.size) {
        when (args[i++]) {
            "-n", "--nick" -> config.setName(args[i++])
            "-s", "--server" -> config.setServerHostname(args[i++])
            "-p", "--port" -> config.setServerPort(args[i++].toInt())
            "-P", "--password" -> config.setServerPassword(args[i++])
            "-c", "--channel" -> config.addAutoJoinChannel(args[i++])
            "--ssl" -> config.setSocketFactory(SSLSocketFactory.getDefault())
            "--insecure" -> config.setSocketFactory(UtilSSLSocketFactory().trustAllCertificates())
            else -> {
                println("Unknown option: ${args[i - 1]}")
                println("""
                    irbot [options]
                    IRC incident room bot

                    -n, --nick NICK          bot's nickname
                    -s, --server HOST        hostname of IRC server to connect to
                    -p, --port PORT          port of IRC server to connect to (default: 6667)
                    -P, --password PASSWORD  IRC server password
                    -c, --channel CHANNEL    channel to join (can specified multiple times)
                    --ssl                    use SSL when connecting
                    --insecure               accept all certificates
                    """.trimIndent())
                System.exit(1)
            }
        }
    }

    val bot = PircBotX(config.buildConfiguration())
    bot.startBot()
}