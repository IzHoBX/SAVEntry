package com.izho.saveentry.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocation(location: Location): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: Visit): Long

    @Update
    suspend fun updateVisit(visit: Visit): Int

    @Update
    suspend fun updateLocation(location: Location): Int

    @Query("SELECT * FROM location WHERE location_id = :locationId")
    fun getLocationById(locationId: String): LiveData<Location>

    @Query("SELECT * FROM location")
    fun getAllLocations(): LiveData<List<Location>>

    @Query("SELECT * FROM location WHERE favorite = 1 ORDER BY venue_name ASC")
    fun getAllFavoriteLocations(): LiveData<List<Location>>

    @Delete
    suspend fun deleteVisit(visit: Visit)

    @Transaction
    @Query("SELECT * FROM visit WHERE visit_id = :visitId")
    suspend fun getVisitWithLocationById(visitId: Long): VisitWithLocation

    // TODO: Implement Paging for both calls below.

    @Transaction
    @Query("SELECT * FROM visit ORDER BY check_in_at DESC")
    fun getAllVisitWithLocation(): LiveData<List<VisitWithLocation>>

    @Transaction
    @Query("SELECT * FROM visit WHERE check_out_at IS NOT NULL ORDER BY check_in_at DESC")
    fun getAllCompletedVisitWithLocation(): LiveData<List<VisitWithLocation>>

    @Transaction
    @Query("SELECT * FROM visit WHERE check_out_at IS NULL ORDER BY check_in_at DESC")
    fun getAllActiveVisitWithLocation(): LiveData<List<VisitWithLocation>>

    @Query("SELECT * FROM visit WHERE check_out_at IS NULL AND location_id = :locationId")
    fun getActiveVisitWithLocationId(locationId:String) : LiveData<List<VisitWithLocation>>

    @Query("UPDATE location SET organization = :organization, venue_name = :venueName WHERE location_id = :location_id")
    fun updateLocationNames(venueName:String, organization:String, location_id:String)
}

@Database(entities = [Location::class, Visit::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract val dao: AppDao
}

// Migration (24/05/2020): Added a new column for notification id
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE visit ADD COLUMN notification_id INTEGER NOT NULL DEFAULT(0)")
    }
}

// Migration (24/05/2020): Added a new column for notification id
private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE visit ADD COLUMN pass_image_path TEXT")
    }
}

private lateinit var INSTANCE: AppDatabase
fun getAppDatabase(context: Context, resetDb: Boolean = false): AppDatabase {
    val TAG = "AppDatabase"

    synchronized(AppDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            if (resetDb) {
                builder.addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.execSQL("DELETE FROM location")
                        db.execSQL("DELETE FROM visit")
                        Log.i(TAG, "Cleared database")
                    }
                })
            }

            INSTANCE = builder.build()
        }
    }
    return INSTANCE
}
