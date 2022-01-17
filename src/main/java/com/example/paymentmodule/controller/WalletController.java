package com.example.paymentmodule.controller;

import com.example.paymentmodule.dto.OrderDto;
import com.example.paymentmodule.dto.PaymentDto;
import com.example.paymentmodule.entity.Wallet;
import com.example.paymentmodule.repo.WalletRepo;
import com.example.paymentmodule.response.RESTResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.example.paymentmodule.queue.Config.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/payments")
public class WalletController {

    @Autowired
    WalletRepo walletRepo;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RequestMapping(path = "account/{userId}", method = RequestMethod.GET)
    public Object find(@PathVariable int userId) {
        Wallet wallet = walletRepo.findBalletByUserId((long) userId);
        return new RESTResponse.Success()
                .addData(wallet)
                .build();
    }

    public void handlerPayment(OrderDto orderDto) {
        PaymentDto paymentDto = new PaymentDto(orderDto.getOrderId(), orderDto.getUserId(), orderDto.getDevice_token());
        if (orderDto.getUserId() == null) {
            paymentDto.setMessage("Tài khoản ví không đúng");
            System.out.println("Tài khoản ví không đúng");
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_PAY, paymentDto);
            return;
        }
        Wallet ballet = walletRepo.findBalletByUserId(Long.valueOf(orderDto.getUserId()));


        if (orderDto.getCheckOut() == 1) {
            paymentDto.setMessage("Order đã thanh toán");
            System.out.println("Order đã thanh toán");
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_PAY, paymentDto);
            return;
        }

        if (ballet == null) {
            paymentDto.setMessage("Tài khoản thanh toán không đúng");
            paymentDto.setCheckOut(0);
            System.out.println("Tài khoản thanh toán không đúng");
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_PAY, paymentDto);
            return;
        }

        double totalPrice = orderDto.getTotalPrice();
        double balance = ballet.getBalance();

        if (totalPrice > balance) {
            paymentDto.setMessage("Số dư ví không đủ");
            System.out.println("Số dư ví không đủ");
            paymentDto.setCheckOut(0);
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_PAY, paymentDto);
            return;
        }
        try {
            ballet.setBalance(balance - totalPrice);
            walletRepo.save(ballet);
            paymentDto.setMessage("Thanh toán thành công");
            paymentDto.setCheckOut(1);
            System.out.println("Thanh toán thành công");
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_PAY, paymentDto);
        } catch (Exception e) {
            ballet.setBalance(balance);
            walletRepo.save(ballet);
            paymentDto.setMessage("Thanh toán lỗi! Vui lòng thử lại");
            System.out.println("Thanh toán lỗi! Vui lòng thử lại");
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_PAY, paymentDto);
        }

    }
}
