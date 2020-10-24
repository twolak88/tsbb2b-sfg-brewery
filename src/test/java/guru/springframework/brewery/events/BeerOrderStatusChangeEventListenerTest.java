package guru.springframework.brewery.events;

import com.github.jenspiegsa.wiremockextension.Managed;
import com.github.jenspiegsa.wiremockextension.ManagedWireMockServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import guru.springframework.brewery.domain.BeerOrder;
import guru.springframework.brewery.domain.OrderStatusEnum;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.web.client.RestTemplateBuilder;

/**
 *
 * @author twolak
 */
@ExtendWith(WireMockExtension.class)
public class BeerOrderStatusChangeEventListenerTest {
    
    @Managed
    private WireMockServer wireMockServer = ManagedWireMockServer.with(WireMockConfiguration.wireMockConfig().dynamicPort());
    
    private BeerOrderStatusChangeEventListener beerOrderStatusChangeEventListener;
    
    @BeforeEach
    public void setUp() {
        this.beerOrderStatusChangeEventListener = new BeerOrderStatusChangeEventListener(new RestTemplateBuilder());
    }

    /**
     * Test of listen method, of class BeerOrderStatusChangeEventListener.
     */
    @Test
    public void testListen() {
        
        this.wireMockServer.stubFor(WireMock.post("/update").willReturn(WireMock.ok()));
        
        BeerOrder beerOrder = BeerOrder.builder()
                .orderStatus(OrderStatusEnum.READY)
                .orderStatusCallbackUrl("http://localhost:" + this.wireMockServer.port() + "/update")
                .createdDate(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        BeerOrderStatusChangeEvent beerOrderStatusChangeEvent = new BeerOrderStatusChangeEvent(beerOrder, OrderStatusEnum.NEW);
        
        this.beerOrderStatusChangeEventListener.listen(beerOrderStatusChangeEvent);
        
        WireMock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo("/update")));
    }
    
}
