package com.example.shoesapp

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import model.Order.Order
import model.Order.OrderStatus
import model.Product

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import service.OrderServiceImpl

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.shoesapp", appContext.packageName)
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