package com.example.demo.service;

import com.example.demo.dao.*;
import com.example.demo.dto.*;
import com.example.demo.model.*;
import com.example.demo.controller.OrderController;
import com.example.demo.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderServiceTest {
    UserDao users;
    ProductDao products;
    OrderDao orders;
    OrderServiceImpl service;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        users = mock(UserDao.class); products = mock(ProductDao.class); orders = mock(OrderDao.class);
        service = new OrderServiceImpl();
        ReflectionTestUtils.setField(service, "userDao", users);
        ReflectionTestUtils.setField(service, "productDao", products);
        ReflectionTestUtils.setField(service, "orderDao", orders);
        OrderController controller = new OrderController();
        ReflectionTestUtils.setField(controller, "orderService", service);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        when(users.getUserById(1)).thenReturn(new User());
        Product product = new Product(); product.setPrice(20);
        when(products.getProductById(4)).thenReturn(product);
        when(products.decreaseStock(eq(4), anyInt())).thenReturn(1);
        when(orders.createOrder(anyInt(), anyInt())).thenReturn(42);
    }

    CreateOrderRequest request(int... quantities) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setBuyItemList(java.util.Arrays.stream(quantities).mapToObj(q -> {
            BuyItem item = new BuyItem(); item.setProductId(4); item.setQuantity(q); return item;
        }).toList());
        return request;
    }

    @Test
    void mergesDuplicatesAndCalculatesAmount() {
        assertEquals(42, service.createOrder(1, request(2, 3)));
        verify(products).decreaseStock(4, 5);
        verify(orders).createOrder(1, 100);
        ArgumentCaptor<List<OrderItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(orders).createOrderItems(eq(42), captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(100, captor.getValue().get(0).getAmount());
        assertEquals(5, captor.getValue().get(0).getQuantity());
    }

    @Test
    void returnsCreatedAndOrderId() throws Exception {
        mvc.perform(post("/users/1/orders").contentType("application/json")
                .content("{\"buyItemList\":[{\"productId\":4,\"quantity\":2}]}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.orderId").value(42));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"buyItemList\":[]}", "{\"buyItemList\":[null]}",
        "{\"buyItemList\":[{}]}", "{\"buyItemList\":[{\"productId\":4,\"quantity\":0}]}",
        "{\"buyItemList\":[{\"productId\":-1,\"quantity\":1}]}"})
    void rejectsInvalidRequest(String body) throws Exception {
        mvc.perform(post("/users/1/orders").contentType("application/json").content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(users, products, orders);
    }

    @Test
    void rejectsMissingUser() {
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.createOrder(2, request(1))).getStatusCode().value());
        verifyNoInteractions(products, orders);
    }

    @Test
    void rejectsMissingProduct() {
        when(products.getProductById(4)).thenReturn(null);
        assertBadRequest(request(1));
    }

    @Test
    void rejectsInsufficientStock() {
        when(products.decreaseStock(4, 2)).thenReturn(0);
        assertBadRequest(request(2));
    }

    @Test
    void rejectsQuantityOverflow() { assertBadRequest(request(Integer.MAX_VALUE, 1)); }

    @Test
    void rejectsAmountOverflow() { assertBadRequest(request(Integer.MAX_VALUE)); }

    void assertBadRequest(CreateOrderRequest request) {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.createOrder(1, request)).getStatusCode().value());
        verifyNoInteractions(orders);
    }

    @Test
    void returnsOrderListWithDetails() throws Exception {
        Order order = new Order();
        order.setOrderId(42); order.setUserId(1); order.setTotalAmount(40);
        OrderItem item = new OrderItem();
        item.setOrderItemId(8); item.setOrderId(42); item.setProductId(4);
        item.setQuantity(2); item.setAmount(40);
        when(orders.getOrders(1)).thenReturn(List.of(order));
        when(orders.getOrderItemsByOrderId(42)).thenReturn(List.of(item));
        mvc.perform(get("/users/1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(42))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].totalAmount").value(40))
                .andExpect(jsonPath("$[0].orderItemList[0].productId").value(4))
                .andExpect(jsonPath("$[0].orderItemList[0].quantity").value(2))
                .andExpect(jsonPath("$[0].orderItemList[0].amount").value(40));
    }

    @Test
    void returnsEmptyListForUserWithoutOrders() throws Exception {
        when(orders.getOrders(1)).thenReturn(List.of());
        mvc.perform(get("/users/1/orders"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(orders, never()).getOrderItemsByOrderId(anyInt());
    }

    @Test
    void queryRejectsMissingUser() throws Exception {
        mvc.perform(get("/users/2/orders")).andExpect(status().isNotFound());
        verifyNoInteractions(orders);
    }

    @Test
    void queryRejectsNonIntegerUserId() throws Exception {
        mvc.perform(get("/users/abc/orders")).andExpect(status().isBadRequest());
        verifyNoInteractions(users, orders);
    }
}
