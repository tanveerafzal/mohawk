package ca.mohawkcollege.ocastranscript

final class OcasApi {
    public static final String ENDPOINT_AUTHENTICATE = 'auth/login'
    public static final String ENDPOINT_NEW_REQUESTS = 'transcriptrequests/no_response'
    public static final String ENDPOINT_GET_REQUEST = 'transcriptrequests/$requestId'
    public static final String ENDPOINT_NEW_TRANSCRIPTS = 'transcripts/no_response'
    public static final String ENDPOINT_GET_TRANSCRIPT = 'transcripts/$requestId'
    public static final String ENDPOINT_POST_ACKNOWLEDGMENT = 'transcripts/$requestId/acknowledgment'
    public static final String ENDPOINT_POST_RESPONSE = 'transcriptrequests/$requestId/response'
    public static final String ENDPOINT_POST_TRANSCRIPT = 'transcriptrequests/$requestId/transcript'

    public static final String NOTEMESSAGE_NO_ACADEMIC_HISTORY = 'Verify'
}
