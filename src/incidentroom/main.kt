package incidentroom

import droute.nanohttpd.NanoServer
import org.pircbotx.Configuration
import org.pircbotx.PircBotX
import org.pircbotx.UtilSSLSocketFactory
import java.net.InetAddress
import java.nio.channels.ServerSocketChannel
import javax.net.ssl.SSLSocketFactory

fun main(args: Array<String>) {
    val config = Configuration.Builder<PircBotX>()
    config.setAutoReconnect(true)
    config.setAutoNickChange(true)
    config.setRealName("Incident Room Bot")
    config.setServerHostname("localhost")
    config.setName("irbot")
    config.setLogin("irbot")

    var jdbcUrl = "h2:mem:ir"
    var webPort = 8080
    var webHost : String? = null
    var webInherit = false

    var i = 0
    while (i < args.size) {
        when (args[i++]) {
            "-d", "--data" -> jdbcUrl = "h2:file:" + args[i++]
            "-n", "--nick" -> config.setName(args[i++])
            "-s", "--server" -> config.setServerHostname(args[i++])
            "-p", "--port" -> config.setServerPort(args[i++].toInt())
            "-P", "--password" -> config.setServerPassword(args[i++])
            "-c", "--channel" -> config.addAutoJoinChannel(args[i++])
            "--ssl" -> config.setSocketFactory(SSLSocketFactory.getDefault())
            "--insecure" -> config.setSocketFactory(UtilSSLSocketFactory().trustAllCertificates())
            "-b", "--web-host" -> webHost = args[i++]
            "-p", "--web-port" -> webPort = args[i++].toInt()
            "-i", "--web-inherit" -> webInherit = true
            else -> {
                println("Unknown option: ${args[i - 1]}")
                println("""
                    irbot [options]
                    IRC incident room bot and status site

                    General Options
                      -d, --data PATH          path to store data under (default: in-memory)

                    IRC Options
                      -n, --nick NICK          bot's nickname
                      -s, --server HOST        hostname of IRC server to connect to
                      -p, --port PORT          port of IRC server to connect to (default: 6667)
                      -P, --password PASSWORD  IRC server password
                      -c, --channel CHANNEL    channel to join (can specified multiple times)
                      --ssl                    use SSL when connecting
                      --insecure               accept all certificates

                    Webapp Options
                      -b, --web-host HOST      bind address for webapp
                      -w, --web-port PORT      bind port for webapp (default: 8080)
                      -i, --web-inherit        use inetd-style inherited socket (STDIN)
                    """.trimIndent())
                System.exit(1)
            }
        }
    }

    val dbPool = DbPool(jdbcUrl)
    dbPool.migrate()

    val host = webHost ?: InetAddress.getLocalHost().hostName
    val url = "http://$host:$webPort"

    val irSystem = IncidentRoomSystem(dbPool, url)
    config.addListener(IrcBot(irSystem))

    val webapp = Webapp(irSystem)
    val webserver = initWebServer(webapp, webHost, webPort, webInherit)
    webserver.start()

    val bot = PircBotX(config.buildConfiguration())
    bot.startBot()
}

private fun initWebServer(webapp: Webapp, host: String?, port: Int, inherit: Boolean): NanoServer {
    if (inherit) {
        val channel = System.inheritedChannel();
        if (channel == null || !(channel is ServerSocketChannel)) {
            System.err.println("When the --web-inherit (-i) option is given, STDIN must be a socket");
            System.exit(1);
        }
        return NanoServer(webapp, (channel as ServerSocketChannel).socket())
    } else if (host != null) {
        return NanoServer(webapp, host, port)
    } else {
        return NanoServer(webapp, port)
    }
}