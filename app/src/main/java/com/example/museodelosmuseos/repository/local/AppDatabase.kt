package com.example.museodelosmuseos.repository.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.museodelosmuseos.model.FavoritoEntity
import com.example.museodelosmuseos.model.LecturaQrEntity
import com.example.museodelosmuseos.model.ProductoCacheEntity
import com.example.museodelosmuseos.model.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        FavoritoEntity::class,
        LecturaQrEntity::class,
        ProductoCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun favoritoDao(): FavoritoDao
    abstract fun lecturaQrDao(): LecturaQrDao
    abstract fun productoCacheDao(): ProductoCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Singleton: una sola conexión a la base de datos para toda la app
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "museo_db"
                )
                    // Mientras desarrollamos: si cambia una tabla, recrea la base
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}