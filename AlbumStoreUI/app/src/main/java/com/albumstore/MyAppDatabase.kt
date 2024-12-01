package com.albumstore

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.albumstore.todo.data.band.Band
import com.albumstore.todo.data.local.BandDao
import com.albumstore.todo.data.product.Product
import com.albumstore.todo.data.local.ProductDao
import com.albumstore.utils.Converters

@Database(entities = [Product::class , Band::class], version = 4, exportSchema = false)

@TypeConverters(Converters::class) // Add the Converters class here
abstract class MyAppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao // DAO = Data Access Object
    abstract fun bandDao(): BandDao // DAO = Data Access Object
    companion object {
        @Volatile
        private var INSTANCE: MyAppDatabase? = null

        // Define a migration from version 1 to 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create a new table with the correct schema
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS new_products (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                price REAL NOT NULL,
                bandName TEXT NOT NULL,
                artistsNames TEXT NOT NULL,
                image TEXT NOT NULL,
                baseImage TEXT,
                isFavorited INTEGER NOT NULL DEFAULT 0,
                genre TEXT
            )
        """)

                // Copy the data from the old table
                database.execSQL("""
            INSERT INTO new_products (id, name, price, bandName, artistsNames, image, baseImage, isFavorited)
            SELECT id, name, price, bandName, artistsNames, image, baseImage, isFavorited FROM products
        """)

                // Drop the old table
                database.execSQL("DROP TABLE products")

                // Rename the new table
                database.execSQL("ALTER TABLE new_products RENAME TO products")
            }
        }



        fun getDatabase(context: Context): MyAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyAppDatabase::class.java,
                    "app_database"
                )
//                    .addMigrations(MIGRATION_1_2) // Add migration here
                    .fallbackToDestructiveMigration() // Optional: Deletes database if migration fails
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
