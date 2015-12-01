package incidentroom

import droute.Handler
import droute.Request
import droute.Response
import droute.Route

class Webapp(val system: IncidentRoomSystem) : Handler {
    val routes = Route.routes(Route.GET("/", Handler { Response.response("Hello world.")} ))

    override fun handle(request: Request): Response {
        return routes.handle(request)
    }

}