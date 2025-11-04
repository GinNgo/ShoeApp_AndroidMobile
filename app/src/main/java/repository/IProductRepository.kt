package repository

import model.Product

interface IProductRepository {
    suspend fun getAllProducts(): List<Product>
    suspend fun getSizeProduct(): Int
    suspend fun addProduct(product: Product): Boolean
    suspend fun getProductById(id: String): Product?
    suspend fun updateProduct(product: Product): Boolean
    suspend fun deleteProduct(id: String): Boolean
    suspend fun getProductsByCategory(categoryId: String): List<Product>
}