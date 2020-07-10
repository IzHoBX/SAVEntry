package com.izho.saveentry.data

import androidx.room.*
import kotlin.math.abs
import kotlin.random.Random

@Entity(tableName = "location")
data class Location(
    @PrimaryKey @ColumnInfo(name = "location_id") val locationId: String,
    @ColumnInfo(name = "organization") val organization: String,
    @ColumnInfo(name = "venue_name") val venueName: String,
    val url: String,
    var favorite: Boolean = false,
    @ColumnInfo(name = "user_defined_name") var userDefinedName: String? = null
)

@Entity(tableName = "visit")
data class Visit (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "visit_id") val visitId: Long = 0,
    @ColumnInfo(name = "location_id") val locationId: String,
    @ColumnInfo(name = "check_in_at") val checkInAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "check_out_at") var checkOutAt: Long? = null,
    @ColumnInfo(name = "notification_id") val notificationId: Int = abs(Random.nextInt()),
    @ColumnInfo(name = "pass_image_path") val passImagePath: String? = null,
    val isOfflineCheckIn:Boolean = false
)

data class VisitWithLocation(
    @Embedded val visit: Visit,
    @Relation(
        parentColumn = "location_id",
        entityColumn = "location_id"
    )
    val location: Location
)
