package o2offx.awagner.tikkit.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface TicketDAO {

    @Insert
    fun insert(vararg newTicket: Ticket)

    @Query("SELECT * FROM Ticket")
    fun getAll(): List<Ticket>

    @Query("SELECT * FROM Ticket WHERE id = :ID")
    fun getOne(ID: String): Ticket

}