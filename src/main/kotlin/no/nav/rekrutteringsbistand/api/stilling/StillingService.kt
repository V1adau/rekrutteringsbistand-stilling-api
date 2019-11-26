package no.nav.rekrutteringsbistand.api.stilling

import arrow.core.getOrElse
import no.nav.rekrutteringsbistand.api.autorisasjon.TokenUtils
import no.nav.rekrutteringsbistand.api.stillingsinfo.Stillingsid
import no.nav.rekrutteringsbistand.api.stillingsinfo.Stillingsinfo
import no.nav.rekrutteringsbistand.api.stillingsinfo.StillingsinfoService
import no.nav.rekrutteringsbistand.api.support.LOG
import no.nav.rekrutteringsbistand.api.support.config.ExternalConfiguration
import no.nav.rekrutteringsbistand.api.support.rest.RestResponseEntityExceptionHandler
import no.nav.rekrutteringsbistand.api.support.toMultiValueMap
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class StillingService(
        val restTemplate: RestTemplate,
        val externalConfiguration: ExternalConfiguration,
        val rekrutteringsbistandService: StillingsinfoService,
        val tokenUtils: TokenUtils
) {

    fun hentStilling(uuid: String): StillingMedStillingsinfo {
        val url = "${externalConfiguration.stillingApi.url}/b2b/api/v1/ads/$uuid"
        LOG.debug("henter stilling fra url $url")
        val opprinneligStilling = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity(null, headersUtenToken()),
                StillingMedStillingsinfo::class.java)
                .body

        return berikMedRekruttering(
                opprinneligStilling ?: throw RestResponseEntityExceptionHandler.NoContentException("Fant ikke stilling")
        )
    }

    fun hentStillinger(url: String, queryString: String?): Page<StillingMedStillingsinfo> {

        val withQueryParams: String = UriComponentsBuilder.fromHttpUrl(url).query(queryString).build().toString()

        LOG.debug("henter stilling fra url $withQueryParams")
        val opprinneligeStillinger = restTemplate.exchange(
                withQueryParams,
                HttpMethod.GET,
                HttpEntity(null, headers()),
                object : ParameterizedTypeReference<Page<StillingMedStillingsinfo>>() {})
                .body

        val validertContent = (opprinneligeStillinger
                ?: throw RestResponseEntityExceptionHandler.NoContentException("Fant ikke stillinger")).content

        return opprinneligeStillinger.copy(
                content = validertContent
                        .map {
                            berikMedRekruttering(it)
                        })
    }

    fun berikMedRekruttering(stillingMedStillingsinfo: StillingMedStillingsinfo): StillingMedStillingsinfo =
            rekrutteringsbistandService.hentForStilling(Stillingsid(stillingMedStillingsinfo.uuid!!))
                    .map(Stillingsinfo::asDto)
                    .map { stillingMedStillingsinfo.copy(rekruttering = it) }
                    .getOrElse { stillingMedStillingsinfo }


    fun headers() =
            mapOf(
                    HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON.toString(),
                    HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON.toString(),
                    HttpHeaders.AUTHORIZATION to "Bearer ${tokenUtils.hentOidcToken()}}"
            ).toMultiValueMap()

    fun headersUtenToken() =
            mapOf(
                    HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON.toString(),
                    HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON.toString()
            ).toMultiValueMap()
}
