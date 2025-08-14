package ca.mohawkcollege.ocastranscript

import grails.plugins.rest.client.RestResponse
import grails.transaction.Transactional
import grails.util.Holders
import groovy.text.SimpleTemplateEngine
import org.codehaus.groovy.grails.web.json.JSONElement
import org.codehaus.groovy.grails.web.mime.MimeType
import org.springframework.http.HttpStatus
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException

@Transactional
class OcasApiService {

    // RestBuilder bean
    def rest
    def ocasAuthenticationService

    protected ConfigObject apiConfig = Holders.grailsApplication.config.mohawkcollege.ocastranscript.api
    private String apiUrlBase = apiConfig.baseURL

    /**
     * Given the relative URL of the desired API request (with optional string replacement parameters), perform an
     * authenticated GET request to the API. If successful, the returned value is the response's body content, in JSON
     * format.
     *
     * If there is an error in transmission detected, a RestClientException is thrown. For this purpose, receiving an
     * empty JSON object is treated as an error, as is receiving a valid JSON object with an abnormal HTTP status code.
     * The calling method must decide how to catch and handle those.
     *
     * @param optional named arguments for substitution into relativeUrl
     * @param relative URL (with no leading slash) of the API endpoint
     * @return
     * @throws RestClientException
     */
    JSONElement jsonGet(Map urlParams = [:], String relativeUrl) throws RestClientException {
        String url = buildUrl(relativeUrl, urlParams)
        log.debug("API Endpoint: $url")
        try {
            processResponse rest.get(url) {
                contentType MimeType.JSON.name
                header "Authorization", ocasAuthenticationService.accessToken
            }
        }
        catch (Exception ex) {
            throw new RestClientException("API GET failed with exception", ex)
        }
    }

    /**
     * Given the relative URL of the desired API request (with optional string replacement parameters) and a map
     * containing the outgoing request body content, perform an authenticated JSON POST request to the API. If
     * successful, the returned value is the response's body content, in JSON format.
     *
     * If there is an error in transmission detected, a RestClientException is thrown. For this purpose, receiving an
     * empty JSON object is treated as an error, as is receiving a valid JSON object with an abnormal HTTP status code.
     * The calling method must decide how to catch and handle those.
     *
     * @param optional named arguments for substitution into relativeUrl
     * @param relative URL (with no leading slash) of the API endpoint
     * @param map of body content
     * @return
     * @throws RestClientException
     */
    JSONElement jsonPost(Map urlParams = [:], String relativeUrl, Map postBody) throws RestClientException {
        String url = buildUrl(relativeUrl, urlParams)
        log.debug("Endpoint: $url")
        try {
            processResponse rest.post(url) {
                contentType MimeType.JSON.name
                header "Authorization", ocasAuthenticationService.accessToken
                json postBody
            }
        }
        catch (Exception ex) {
            throw new RestClientException("API POST failed with exception", ex)
        }
    }

    /**
     * Given the relative URL of the desired API request (with optional string replacement parameters) and a map
     * containing the outgoing request body content, perform a form-encoded POST request to the API. If
     * successful, the returned value is the response's body content, in JSON format.
     *
     * If there is an error in transmission detected, a RestClientException is thrown. For this purpose, receiving an
     * empty JSON object is treated as an error, as is receiving a valid JSON object with an abnormal HTTP status code.
     * The calling method must decide how to catch and handle those.
     *
     * @param optional named arguments for substitution into relativeUrl
     * @param relative URL (with no leading slash) of the API endpoint
     * @param map of body content
     * @return
     * @throws RestClientException
     */
    JSONElement formPost(Map urlParams = [:], String relativeUrl, MultiValueMap postBody) throws RestClientException {
        String url = buildUrl(relativeUrl, urlParams)
        log.debug("Endpoint: $url")
        try {
            processResponse rest.post(url) {
                contentType MimeType.FORM.name
                body postBody
            }
        }
        catch (Exception ex) {
            throw new RestClientException("API form POST failed with exception", ex)
        }
    }

    /**
     * Given a RestResponse, check it for errors and, if none found, return its JSON payload
     *
     * @param response
     * @return JSONElement
     */
    protected static JSONElement processResponse(RestResponse response) {
        if (response?.statusCode == HttpStatus.OK) {
            return response.json
        } else if (!response) {
            throw new RuntimeException("API POST returned null response")
        } else if (response.json?.Message) {
            throw new HttpClientErrorException(response.statusCode, response.json.Message.toString())
        } else {
            throw new HttpClientErrorException(response.statusCode)
        }
    }

    protected String buildUrl(String relativeUrl, Map<String, String> params) {
        String urlTemplate = "$apiUrlBase/$relativeUrl"
        new SimpleTemplateEngine().createTemplate(urlTemplate).make(params).toString()
    }
}
