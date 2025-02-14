package no.nav.rekrutteringsbistand.api.stilling.ekstern

import no.nav.rekrutteringsbistand.api.autorisasjon.AuthorizedPartyUtils
import no.nav.rekrutteringsbistand.api.stilling.StillingService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
class StillingControllerEkstern(
    val stillingService: StillingService,
    val authorizedPartyUtils: AuthorizedPartyUtils
) {

    @GetMapping("/rekrutteringsbistand/ekstern/api/v1/stilling/{uuid}")
    fun hentStillingTilPersonbruker(@PathVariable uuid: String): ResponseEntity<StillingForPersonbruker> {

        if (!authorizedPartyUtils.kallKommerFraVisStilling()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val stilling = stillingService.hentRekrutteringsbistandStilling(
            stillingsId = uuid,
            somSystembruker = true
        ).stilling

        fun copyProps(vararg keys: String): Map<String, String> =
                hashMapOf(*(keys.filter { stilling.properties[it] != null }.map {
                    it to (stilling.properties[it] ?: "")
                }.toTypedArray()))

        return ResponseEntity.ok().body(
                StillingForPersonbruker(
                        title = stilling.title,
                        properties = copyProps(
                                "adtext", "applicationdue", "applicationemail", "engagementtype", "jobarrangement", "extent", "workday",
                                "workhours", "positioncount", "sector", "starttime", "employerhomepage", "employerdescription",
                                "applicationurl", "jobtitle", "twitteraddress", "facebookpage", "linkedinpage"
                        ),
                        contactList = stilling.contactList.map {
                            Contact(
                                    name = it.name,
                                    email = it.email,
                                    phone = it.phone,
                                    title = it.title
                            )
                        },
                        location = stilling.location,
                        employer = stilling.employer?.let {
                            Arbeidsgiver(
                                    name = it.name,
                                    location = it.location,
                                    publicName = it.publicName
                            )
                        },
                        updated = stilling.updated,
                        medium = stilling.medium,
                        businessName = stilling.businessName,
                        status = stilling.status,
                        id = stilling.id,
                        uuid = stilling.uuid,
                        source = stilling.source
                )
        )
    }
}
