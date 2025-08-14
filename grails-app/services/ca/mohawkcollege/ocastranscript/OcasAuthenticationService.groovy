package ca.mohawkcollege.ocastranscript

import ca.mohawkcollege.baselib.util.DateUtils
import grails.transaction.Transactional
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

@Transactional
class OcasAuthenticationService extends OcasApiService {

    private Integer tokenLifetimeSeconds = apiConfig.tokenLifetimeSeconds
    private TimeDuration tokenLifetime = new TimeDuration(0, 0, tokenLifetimeSeconds, 0)
    private Date tokenFetchTime
    private String accessToken

    String getAccessToken() {
        if (tokenValid) {
            log.debug("Reusing unexpired access token")
        } else {
            log.debug("Authenticating to API")

            MultiValueMap<String, String> payload = new LinkedMultiValueMap<String, String>()
            payload.add("Username", apiConfig.username.toString())
            payload.add("Password", apiConfig.password.toString())
            payload.add("grant_type", "password")

            try {
                def authJson = formPost(OcasApi.ENDPOINT_AUTHENTICATE, payload)
                if (authJson?.access_token && authJson?.token_type) {
                    tokenFetchTime = new Date()
                    accessToken = "${authJson?.token_type} ${authJson?.access_token}"
                }
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to authenticate to API", e)
            }

            if (!tokenValid) {
                throw new RuntimeException("Failed to authenticate to API")
            }
        }

        return accessToken
    }

    private boolean isTokenValid() {
        accessToken && tokenFetchTime && (tokenAge < tokenLifetime)
    }

    private TimeDuration getTokenAge() {
        TimeCategory.minus(DateUtils.now, tokenFetchTime)
    }
}
