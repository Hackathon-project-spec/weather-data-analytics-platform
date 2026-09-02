package in.gov.moes.sih26069.gateway;

import in.gov.moes.sih26069.gateway.controller.GatewayRoutingController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class GatewayRoutingControllerTest {

    @InjectMocks
    private GatewayRoutingController gatewayController;

    @Test
    public void testGatewayRoutingFallbackOnUnreachableService() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/ingestion/events");
        request.setRequestURI("/api/v1/ingestion/events");

        byte[] payload = "{\"eventId\":\"test\"}".getBytes();
        ResponseEntity<byte[]> response = gatewayController.routeIngestionService(payload, HttpMethod.POST, request);

        assertNotNull(response);
        // Should return BAD_GATEWAY (502) with descriptive error if downstream is not yet running
        assertEquals(502, response.getStatusCode().value());
        String body = new String(response.getBody());
        assertTrue(body.contains("temporarily unavailable"));
    }
}
