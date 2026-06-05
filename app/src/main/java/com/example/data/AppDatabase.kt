package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TransactionEntity::class, 
        CategoryEntity::class, 
        OrderEntity::class, 
        PieceCalculationEntity::class, 
        UserEntity::class, 
        BrandConfigEntity::class, 
        InvestmentEntity::class,
        ClientEntity::class,
        EmployeeEntity::class,
        EmployeePaymentEntity::class,
        ProductModelEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val transactionDao: TransactionDao
    abstract val categoryDao: CategoryDao
    abstract val orderDao: OrderDao
    abstract val pieceCalculationDao: PieceCalculationDao
    abstract val userDao: UserDao
    abstract val brandConfigDao: BrandConfigDao
    abstract val investmentDao: InvestmentDao
    abstract val clientDao: ClientDao
    abstract val employeeDao: EmployeeDao
    abstract val employeePaymentDao: EmployeePaymentDao
    abstract val productModelDao: ProductModelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `businessArea` TEXT NOT NULL DEFAULT 'Geral'")
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `status` TEXT NOT NULL DEFAULT 'Pendente'")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `investments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `description` TEXT NOT NULL, 
                        `totalAmount` REAL NOT NULL, 
                        `abatedAmount` REAL NOT NULL, 
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `clients` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `phone` TEXT NOT NULL DEFAULT ''
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `employees` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `role` TEXT NOT NULL DEFAULT 'Costureira'
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `employee_payments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `employeeId` INTEGER NOT NULL, 
                        `employeeName` TEXT NOT NULL, 
                        `amount` REAL NOT NULL, 
                        `week` TEXT NOT NULL, 
                        `paymentDate` TEXT NOT NULL, 
                        `status` TEXT NOT NULL DEFAULT 'Pendente', 
                        `timestamp` INTEGER NOT NULL,
                        `transactionId` INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `product_models` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ms_modaintima_database"
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
