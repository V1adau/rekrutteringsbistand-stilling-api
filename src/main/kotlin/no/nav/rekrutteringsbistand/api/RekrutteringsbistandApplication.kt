package no.nav.rekrutteringsbistand.api

import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableOIDCTokenValidation(ignore = [
    "org.springframework",
    "springfox.documentation.swagger.web.ApiResourceController"
])
class RekrutteringsbistandApplication

fun main(args: Array<String>) {
    runApplication<RekrutteringsbistandApplication>(*args)
}
