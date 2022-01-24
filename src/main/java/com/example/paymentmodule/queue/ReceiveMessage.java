package com.example.paymentmodule.queue;


import com.example.paymentmodule.dto.OrderDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.example.paymentmodule.queue.Config.QUEUE_PAY;

@Component
public class ReceiveMessage {

    @Autowired
    ConsumerService consumerService;

    @RabbitListener(queues = {QUEUE_PAY})
    public void getInfoOrder(OrderDto orderDto) {
        System.out.println("Module Payment nhận thông tin order: " + orderDto);
        consumerService.handlerPayment(orderDto);
    }

}
