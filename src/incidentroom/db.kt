package incidentroom

import com.googlecode.flyway.core.Flyway
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.skife.jdbi.v2.BeanMapper
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.sqlobject.*
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean
import org.skife.jdbi.v2.tweak.ResultSetMapper
import java.io.Closeable
import java.sql.ResultSet
import javax.sql.DataSource

class DbPool(jdbcUrl: String) {
    val dataSource = initHikariCp(jdbcUrl)
    val dbi = DBI(dataSource)

    fun take(): Db {
        return dbi.open(Db::class.java)
    }

    fun <R> take(block: (Db) -> R) : R {
        return take().use(block)
    }

    fun migrate() {
        val flyway = Flyway()
        flyway.dataSource = dataSource
        flyway.setLocations("incidentroom/migrations")
        flyway.migrate()
    }

    private fun initHikariCp(jdbcUrl: String): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = jdbcUrl
        config.poolName = "IncidentRoomDbPool"
        return HikariDataSource(config)
    }
}

const val INCIDENT_COLUMNS = "id, topic, channel"

data class IncidentState(val id : Long = 0, val topic : String, val channel : String, val created : Long = System.currentTimeMillis())

class IncidentMapper : ResultSetMapper<IncidentState> {
    override fun map(index: Int, r: ResultSet, ctx: StatementContext?): IncidentState {
        return IncidentState(r.getLong("id"), r.getString("topic"), r.getString("channel"), r.getLong("created"))
    }
}

@RegisterMapper(IncidentMapper::class)
interface Db : Closeable {

    @SqlUpdate("INSERT INTO incident (topic, channel, created) VALUES (:topic, :channel, :created)")
    @GetGeneratedKeys
    fun insertIncident(@BindBean state : IncidentState): Long

    @SqlQuery("SELECT $INCIDENT_COLUMNS FROM incident WHERE channel = :channel")
    fun findIncidentByChannel(@Bind("channel") channel: String): IncidentState?

    @SqlQuery("SELECT $INCIDENT_COLUMNS FROM incident WHERE id = :id")
    fun findIncidentById(@Bind("id") id: Long): IncidentState?

    @SqlUpdate("INSERT INTO status (incident_id, message, creator, created) VALUES (:incidentId, :message, :creator, :created)")
    fun insertStatus(@Bind("incidentId") incidentId: Long, @Bind("message") message: String, @Bind("creator") creator: String, @Bind("created") created: Long): Long

}