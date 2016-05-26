package jnekoimagesdb.core.net;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

public class NetHTTPGet {
    private final org.slf4j.Logger 
            logger = LoggerFactory.getLogger(NetHTTPGet.class);
    
    private final NetHTTPResponse 
            resp;
    
    private final String 
            userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0";
   
    private final CloseableHttpClient 
            httpClient;
    
    private final RequestConfig 
            defaultRequestConfig;
    
    private final HttpHost 
            proxy;
    
    private final CookieStore 
            cookieStore = new BasicCookieStore();
    
    private final CredentialsProvider 
            credentialsProvider = new BasicCredentialsProvider();
    
    private String 
            refURL = "https://www.google.ru/?gfe_rd=cr&ei=&gws_rd=ssl#newwindow=1&safe=off&q=image";

    public NetHTTPGet(NetHTTPResponse r) {
        resp = r;
        proxy = null;
        defaultRequestConfig = RequestConfig
                .custom()
                .setConnectTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .setContentCompressionEnabled(false)
                .setMaxRedirects(5)
                .setSocketTimeout(60000)
                .setRedirectsEnabled(true)
                .setRelativeRedirectsAllowed(true)
                .setCookieSpec(CookieSpecs.STANDARD)
                .setExpectContinueEnabled(true)
                .build();
        
        httpClient = HttpClients
                .custom()
                .setUserAgent(userAgent)
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(defaultRequestConfig)
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();
    }
    
    public NetHTTPGet(NetHTTPResponse r, String proxyURL, int proxyPort) {
        resp = r;
        if ((proxyPort > 0) && (proxyPort < 65535)) {
            throw new RuntimeException("Port incorrect");
        }
        
        proxy = new HttpHost(proxyURL, proxyPort);
        
        defaultRequestConfig = RequestConfig
                .custom()
                .setProxy(proxy)
                .setConnectTimeout(60000)
                .setConnectionRequestTimeout(60000)
                .setContentCompressionEnabled(false)
                .setMaxRedirects(5)
                .setSocketTimeout(90000)
                .setRedirectsEnabled(true)
                .setRelativeRedirectsAllowed(true)
                .setCookieSpec(CookieSpecs.STANDARD)
                .setExpectContinueEnabled(true)
                .build();
        
        httpClient = HttpClients
                .custom()
                .setUserAgent(userAgent)
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(defaultRequestConfig)
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();
    }

    public void exec(String url) {
        try {
            final HttpGet httpget = new HttpGet(url);
            httpget.addHeader("Referer", refURL);
            final ResponseHandler<String> responseHandler = (HttpResponse hr) -> {
                final int status = hr.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    final HttpEntity entity = hr.getEntity();
                    if (entity != null) {
                        resp.OnOK(entity, hr);
                        return EntityUtils.toString(entity);
                    } else {
                        resp.OnError(hr, -1);
                    }
                    return null;
                } else {
                    resp.OnError(hr, status); 
                    throw new HttpResponseException(status, null);
                }
            };
            httpClient.execute(httpget, responseHandler);
            refURL = url;
        } catch (IOException ex) {
            resp.OnError(null, 0);
            logger.error(ex.getMessage());
        }
    }
    
    public void dispose() {
        try {
            httpClient.close();
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            //Logger.getLogger(NetHTTPGet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
