package co.elastic.apm.impl.context;

import co.elastic.apm.objectpool.Recyclable;
import co.elastic.apm.util.PotentiallyMultiValuedMap;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response implements Recyclable {

    /**
     * A mapping of HTTP headers of the response object
     */
    @JsonProperty("headers")
    private final PotentiallyMultiValuedMap<String, String> headers = new PotentiallyMultiValuedMap<>();
    /**
     * A boolean indicating whether the response was finished or not
     */
    @JsonProperty("finished")
    private boolean finished;
    @JsonProperty("headers_sent")
    private boolean headersSent;
    /**
     * The HTTP status code of the response.
     */
    @JsonProperty("status_code")
    private long statusCode;

    /**
     * A boolean indicating whether the response was finished or not
     */
    @JsonProperty("finished")
    public boolean isFinished() {
        return finished;
    }

    /**
     * A boolean indicating whether the response was finished or not
     */
    public Response withFinished(boolean finished) {
        this.finished = finished;
        return this;
    }

    /**
     * Adds a response header.
     *
     * @param headerName  The name of the header.
     * @param headerValue The value of the header.
     * @return <code>this</code>, for fluent method chaining
     */
    public Response addHeader(String headerName, String headerValue) {
        headers.add(headerName, headerValue);
        return this;
    }

    /**
     * A mapping of HTTP headers of the response object
     */
    @JsonProperty("headers")
    public Map<String, Object> getHeaders() {
        return headers;
    }


    @JsonProperty("headers_sent")
    public boolean isHeadersSent() {
        return headersSent;
    }

    public Response withHeadersSent(boolean headersSent) {
        this.headersSent = headersSent;
        return this;
    }

    /**
     * The HTTP status code of the response.
     */
    @JsonProperty("status_code")
    public long getStatusCode() {
        return statusCode;
    }

    /**
     * The HTTP status code of the response.
     */
    public Response withStatusCode(long statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("finished", finished).append("headers", headers).append("headersSent", headersSent).append("statusCode", statusCode).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(headers).append(headersSent).append(finished).append(statusCode).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Response) == false) {
            return false;
        }
        Response rhs = ((Response) other);
        return new EqualsBuilder().append(headers, rhs.headers).append(headersSent, rhs.headersSent).append(finished, rhs.finished).append(statusCode, rhs.statusCode).isEquals();
    }

    @Override
    public void resetState() {
        finished = false;
        headers.clear();
        headersSent = false;
        statusCode = 0;
    }

    public void copyFrom(Response other) {
        this.finished = other.finished;
        this.headers.putAll(other.headers);
        this.headersSent = other.headersSent;
        this.statusCode = other.statusCode;
    }
}
