package jnekoimagesdb.core.net;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public interface NetHTTPResponse {
    void OnError(HttpResponse hr, int errcode);
    void OnOK(HttpEntity he, HttpResponse hr);
}
