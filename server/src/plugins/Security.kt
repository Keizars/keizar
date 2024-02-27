package org.keizar.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.*
import org.keizar.server.ServerContext

fun Application.configureSecurity(context: ServerContext) {
    authentication {
        bearer("auth-bearer") {
            authenticate { tokenCredential ->
                context.authTokenManager.matchToken(tokenCredential.token)
                    ?.let { UserIdPrincipal(it) }
            }
        }
    }
}
