package ca.mohawkcollege.ocastranscript.xml.pesc

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

final class Format {
    static final Format DATE = new Format("yyyy-MM-dd")
    static final Format TIMESTAMP = new Format("yyyy-MM-dd'T'HH:mm:ssXXX")
    static final Format TIMESTAMP_MILLIS = new Format("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX")
    static final Format YEAR_MONTH = new Format("yyyy-MM")

    String pattern

    Format(String pattern) { this.pattern = pattern }

    @Delegate
    @Lazy
    DateTimeFormatter dateTimeFormatter = { DateTimeFormatter.ofPattern(pattern) }()

    @Lazy
    SimpleDateFormat simpleDateFormat = { new SimpleDateFormat(pattern) }()
}
