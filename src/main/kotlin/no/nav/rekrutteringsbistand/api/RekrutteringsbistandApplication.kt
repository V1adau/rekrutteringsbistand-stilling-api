package no.nav.rekrutteringsbistand.api

import no.nav.rekrutteringsbistand.api.innloggetbruker.InnloggetBrukerController
import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@EnableOIDCTokenValidation(ignore = ["org.springframework"])
class RekrutteringsbistandApplication
    fun main(args: Array<String>) {
        runApplication<RekrutteringsbistandApplication>(*args)
    }
