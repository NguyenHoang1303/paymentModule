package common.event;

import com.example.paymentmodule.dto.OrderDetailDto;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderEvent {

    private Long orderId;
    private Long userId;
    private Set<OrderDetailEvent> orderDetailEvents = new HashSet<>();
    private BigDecimal totalPrice;
    private String paymentStatus;
    private String inventoryStatus;
    private String orderStatus;
    private String device_token;
    private String message;
    private String queueName;

    public boolean validationPayment(){
        return this.totalPrice.compareTo(BigDecimal.valueOf(0)) > 0
                && this.orderId != null && this.userId != null && this.paymentStatus != null;
    }
}
