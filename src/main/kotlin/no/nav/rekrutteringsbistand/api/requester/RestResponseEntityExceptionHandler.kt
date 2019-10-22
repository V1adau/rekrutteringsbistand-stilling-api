package no.nav.rekrutteringsbistand.api.requester

import no.nav.rekrutteringsbistand.api.LOG
import no.nav.security.spring.oidc.validation.interceptor.OIDCUnauthorizedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.io.IOException
import java.nio.file.AccessDeniedException
import java.util.*
import javax.servlet.http.HttpServletResponse

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [OIDCUnauthorizedException::class, AccessDeniedException::class])
    @ResponseBody
    protected fun handleUnauthorizedException(e: RuntimeException, webRequest: WebRequest): ResponseEntity<Any> {
        LOG.info("${HttpStatus.UNAUTHORIZED} contextpath:${webRequest.contextPath} Authorization:${webRequest.getHeader(HttpHeaders.AUTHORIZATION)} Cookie:${webRequest.getHeader(HttpHeaders.COOKIE)} HEADERS:${webRequest.headerNames}",e)
        return getResponseEntity(e, "You are not authorized to access this ressource", HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(value = [Exception::class])
    @ResponseBody
    protected fun handleGenerellException(e: RuntimeException, webRequest: WebRequest): ResponseEntity<Any> {
        LOG.error("Uhåndtert feil", e)
        return getResponseEntity(e, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(IOException::class, ResourceAccessException::class)
    @Throws(IOException::class)
    protected fun handleIOException(e: Exception, response: HttpServletResponse) {
        LOG.error("IO feil", e)
        response.sendError(HttpStatus.BAD_GATEWAY.value(), e.message)
    }

    private fun getResponseEntity(e: RuntimeException, melding: String, status: HttpStatus): ResponseEntity<Any> {
        val body = HashMap<String, String>(1)
        body["message"] = melding
        LOG.info(
                String.format(
                        "Returnerer følgende HttpStatus '%s' med melding '%s' pga exception '%s'",
                        status.toString(),
                        melding,
                        e.message
                )
        )

        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body)
    }

}
