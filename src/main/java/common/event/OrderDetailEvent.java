package common.event;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderDetailEvent {

    private Long productId;
    private Long orderId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;

}
