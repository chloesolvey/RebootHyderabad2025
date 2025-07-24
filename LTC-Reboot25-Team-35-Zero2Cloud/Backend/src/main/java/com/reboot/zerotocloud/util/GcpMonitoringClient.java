//package com.reboot.zerotocloud.util;
//
//import java.io.FileInputStream;
//import java.net.URLEncoder;
//import java.util.Collections;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
//import com.google.api.client.http.GenericUrl;
//import com.google.api.client.http.HttpRequest;
//import com.google.api.client.http.HttpRequestFactory;
//import com.google.api.client.http.HttpResponse;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.auth.http.HttpCredentialsAdapter;
//import com.google.auth.oauth2.GoogleCredentials;
//
//@Component
//public class GcpMonitoringClient {
//
//    private final HttpRequestFactory requestFactory;
//    private final String projectId;
//	private final HttpTransport httpTransport = new NetHttpTransport();
//
//    public GcpMonitoringClient(@Value("${gcp.key.path}") String keyPath) throws Exception {
//    	 GoogleCredentials credentials = GoogleCredentials
//                 .fromStream(new FileInputStream(keyPath))
//                 .createScoped(Collections.singleton("https://www.googleapis.com/auth/cloud-platform"));
//    	 this.requestFactory = httpTransport.createRequestFactory(new HttpCredentialsAdapter(credentials));
//
//
//
//        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(keyPath))
//                .createScoped("https://www.googleapis.com/auth/monitoring.read");
//
//        this.requestFactory = new NetHttpTransport().createRequestFactory(credential);
//        this.projectId = "red-welder-466202-u6";
//    }
//
//    public String fetchMetric(String metricType, String startTime, String endTime, String resourceFilter) throws Exception {
//        String filter = URLEncoder.encode("metric.type=\"" + metricType + "\" AND " + resourceFilter, "UTF-8");
//        String url = String.format(
//            "https://monitoring.googleapis.com/v3/projects/%s/timeSeries?filter=%s&interval.startTime=%s&interval.endTime=%s",
//            projectId, filter, startTime, endTime
//        );
//
//        HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(url));
//        HttpResponse response = request.execute();
//        return response.parseAsString();
//    }
//}
