package com.example.shoesapp

import kotlinx.coroutines.runBlocking
import model.Order.Order
import model.Order.OrderStatus
import model.Product
import org.junit.Test

import org.junit.Assert.*
import service.OrderServiceImpl

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
     fun createOrder() = runBlocking {
        // Giả lập service
        val orderService = OrderServiceImpl()

        // Tạo product mẫu
        val product1 = Product(
            id = "prod1",
            name = "Nike Air Max",
            price = 120.0
        )

        val product2 = Product(
            id = "prod2",
            name = "Adidas Ultra Boost",
            price = 150.0,
        )

        // Tạo danh sách order mẫu
        val orders = listOf(
            Order(
                userId = "user123",
                product = product1,
                quantity = 2,
                status = OrderStatus.IN_DELIVERY,
                totalPrice = product1.price * 2
            ),
            Order(
                userId = "user123",
                product = product2,
                quantity = 1,
                status = OrderStatus.COMPLETE,
                totalPrice = product2.price
            ),
            Order(
                userId = "user123",
                product = product1,
                quantity = 3,
                status = OrderStatus.CANCEL,
                totalPrice = product1.price * 3
            )
        )

        // Lưu vào service / repository
        orders.forEach { order ->
            try {
                orderService.createOrder(order)
                println("Order created: ${order.id} - ${order.product.name} x${order.quantity}")
            } catch (e: Exception) {
                println("Failed to create order: ${e.message}")
            }
        }
    }
}