package in.gov.moes.sih26069.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Enumeration;

@RestController
@CrossOrigin(origins = "*")
public class GatewayRoutingController {

    private static final Logger log = LoggerFactory.getLogger(GatewayRoutingController.class);

    @Value("${services.ingestion.url:http://localhost:8081}")
    private String ingestionServiceUrl;

    @Value("${services.citizen.url:http://localhost:8082}")
    private String citizenServiceUrl;

    @Value("${services.verification.url:http://localhost:8083}")
    private String verificationServiceUrl;

    @Value("${services.analytics.url:http://localhost:8084}")
    private String analyticsServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @RequestMapping(value = {
            "/api/v1/stations/**",
            "/api/v1/simulator/**",
            "/api/v1/ingest/**",
            "/api/v1/ingestion/**",
            "/api/ingestion/**"
    }, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<byte[]> routeIngestionService(@RequestBody(required = false) byte[] body, HttpMethod method, HttpServletRequest request) {
        return forwardRequest(ingestionServiceUrl, request, method, body);
    }

    @RequestMapping(value = "/api/v1/reports/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<byte[]> routeCitizenService(@RequestBody(required = false) byte[] body, HttpMethod method, HttpServletRequest request) {
        return forwardRequest(citizenServiceUrl, request, method, body);
    }

    @RequestMapping(value = "/api/v1/verify/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<byte[]> routeVerificationService(@RequestBody(required = false) byte[] body, HttpMethod method, HttpServletRequest request) {
        return forwardRequest(verificationServiceUrl, request, method, body);
    }

    @RequestMapping(value = {
            "/api/v1/analytics/**",
            "/api/v1/alerts/**"
    }, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<byte[]> routeAnalyticsService(@RequestBody(required = false) byte[] body, HttpMethod method, HttpServletRequest request) {
        return forwardRequest(analyticsServiceUrl, request, method, body);
    }

    private ResponseEntity<byte[]> forwardRequest(String targetBaseUrl, HttpServletRequest request, HttpMethod method, byte[] body) {
        try {
            String path = request.getRequestURI();
            String query = request.getQueryString();
            String targetUrl = targetBaseUrl + path + (query != null ? "?" + query : "");

            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!headerName.equalsIgnoreCase("host") && !headerName.equalsIgnoreCase("content-length")) {
                    headers.set(headerName, request.getHeader(headerName));
                }
            }

            HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);
            return restTemplate.exchange(new URI(targetUrl), method, entity, byte[].class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).headers(e.getResponseHeaders()).body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            log.warn("Gateway forward error to {}: {}", targetBaseUrl, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(("{\"error\": \"Microservice at " + targetBaseUrl + " temporarily unavailable: " + e.getMessage() + "\"}").getBytes());
        }
    }
}
