package o2offx.awagner.tikkit.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class Ticket (
    @PrimaryKey(autoGenerate = false)
    var id: String = "",
    @ColumnInfo
    var name: String = "",
    @ColumnInfo
    var securityInfo: String = "",
    @ColumnInfo
    var other: String = "",
    @ColumnInfo
    var desc: String = ""
)