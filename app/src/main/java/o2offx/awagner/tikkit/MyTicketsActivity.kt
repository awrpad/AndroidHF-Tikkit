package o2offx.awagner.tikkit

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_my_tickets.*
import o2offx.awagner.tikkit.adapter.TicketAdapter
import o2offx.awagner.tikkit.db.TicketDB

class MyTicketsActivity : AppCompatActivity() {
    var n = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_tickets)
        Thread {
            val tickets = TicketDB.getInstance(this@MyTicketsActivity).ticketDAO().getAll()
            val adapter = TicketAdapter(this, tickets)

            runOnUiThread {
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = adapter
            }
        }.start()

    }

}
