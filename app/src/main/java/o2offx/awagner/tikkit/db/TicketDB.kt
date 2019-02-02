package o2offx.awagner.tikkit.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = [Ticket::class], version = 1)
abstract class TicketDB : RoomDatabase() {

    abstract fun ticketDAO(): TicketDAO

    companion object {
        private var instance: TicketDB? = null

        fun getInstance(cntx: Context): TicketDB{
            if (instance == null){
                instance = Room.databaseBuilder(
                    cntx.applicationContext,
                    TicketDB::class.java,
                    "tickets.db"
                ).build()
            }

            return instance!!
        }

        fun destroyInstance(){
            instance = null
        }
    }

}