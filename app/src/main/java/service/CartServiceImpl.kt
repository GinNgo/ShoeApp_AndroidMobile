package service

import model.Cart
import repository.CartRepositoryImpl
import repository.ICartRepository

class CartServiceImpl(
    private val cartRepository: ICartRepository = CartRepositoryImpl()
) : ICartService {

    // 🟢 Tạo giỏ hàng mới cho user nếu chưa có
    override suspend fun createCartForUser(userId: String) {
        val existingCart = cartRepository.getCartByUserId(userId)
        if (existingCart == null) {
            cartRepository.createCartForUser(userId)
        }
    }

    // 🟢 Lấy giỏ hàng theo userId
    override suspend fun getCartByUserId(userId: String): Cart? {
        return cartRepository.getCartByUserId(userId)
    }

    // 🟢 Thêm sản phẩm vào giỏ hàng của user
    override suspend fun addProductToCart(userId: String, productId: String) {
        var cart = cartRepository.getCartByUserId(userId)
        if (cart == null) {
            cartRepository.createCartForUser(userId)
            cart = cartRepository.getCartByUserId(userId)
        }
        cart?.let {
            cartRepository.addProductInCart(it.id, productId)
        }
    }

    override suspend fun addProductToCart(
        userId: String,
        productId: String,
        quantity: Int
    ) {
        var cart = cartRepository.getCartByUserId(userId)
        if (cart == null) {
            cartRepository.createCartForUser(userId)
            cart = cartRepository.getCartByUserId(userId)
        }
        cart?.let {
            cartRepository.addProductInCart(it.id, productId, quantity)
        }
    }

    // 🟢 Xóa sản phẩm khỏi giỏ hàng của user
    override suspend fun removeProductFromCart(userId: String, productId: String) {
        val cart = cartRepository.getCartByUserId(userId)
        cart?.let {
            cartRepository.deleteProductOutOfCart(it.id, productId)
        }
    }
}