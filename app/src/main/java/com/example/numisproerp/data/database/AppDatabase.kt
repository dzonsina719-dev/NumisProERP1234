package com.numisproerp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.numisproerp.data.dao.CatalogDao
import com.numisproerp.data.dao.ClientDao
import com.numisproerp.data.dao.OtherExpenseDao
import com.numisproerp.data.dao.ProductDao
import com.numisproerp.data.dao.PurchaseDao
import com.numisproerp.data.dao.SaleDao
import com.numisproerp.data.dao.SupplierDao
import com.numisproerp.data.entities.CatalogItem
import com.numisproerp.data.entities.Client
import com.numisproerp.data.entities.OtherExpense
import com.numisproerp.data.entities.Product
import com.numisproerp.data.entities.Purchase
import com.numisproerp.data.entities.Sale
import com.numisproerp.data.entities.Supplier

@Database(
    entities = [
        Product::class,
        Client::class,
        Supplier::class,
        Purchase::class,
        Sale::class,
        OtherExpense::class,
        CatalogItem::class
    ],
    version = 11,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun clientDao(): ClientDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun saleDao(): SaleDao
    abstract fun otherExpenseDao(): OtherExpenseDao
    abstract fun catalogDao(): CatalogDao

    companion object {
        /**
         * Реєстр міграцій між версіями схеми БД.
         *
         * Поточна базова версія — 11 (з цього моменту схема експортується у
         * `app/schemas/com.numisproerp.data.database.AppDatabase/11.json`).
         *
         * При будь-якій майбутній зміні entities:
         *   1) збільшити `version` у `@Database`,
         *   2) додати у цей масив новий `Migration(from, to) { db -> ... }`.
         *
         * Не використовуй більше `fallbackToDestructiveMigration()` — він знищує
         * дані користувача. Якщо абсолютно немає сенсу мігрувати — додавай
         * `fallbackToDestructiveMigrationFrom(versionN)` тільки на конкретні
         * старі версії.
         */
        val MIGRATIONS: Array<Migration> = emptyArray()
    }
}
