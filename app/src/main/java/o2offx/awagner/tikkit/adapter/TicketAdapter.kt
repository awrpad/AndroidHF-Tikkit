package o2offx.awagner.tikkit.adapter

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.row_ticket.view.*
import o2offx.awagner.tikkit.R
import o2offx.awagner.tikkit.TicketBoughtActivity
import o2offx.awagner.tikkit.db.Ticket
import org.json.JSONObject

class TicketAdapter(
        private val context: Context,
        private val tickets: List<Ticket>
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    override fun onBindViewHolder(holder: TicketViewHolder, pos: Int) {
        val actualTicket = tickets[pos]

        holder.run {
            tvName.text = actualTicket.name
            btnSee.setOnClickListener {
                val i = Intent(context, TicketBoughtActivity::class.java)
                i.putExtra(
                    "json",
                    JSONObject()
                        .put("_id", actualTicket.id)
                        .put("name", actualTicket.name)
                        .put("securityData", actualTicket.securityInfo)
                        .put("other", actualTicket.other)
                        .put("description", actualTicket.desc)
                        .toString()
                )
                startActivity(context, i, null)
            }
        }
    }

    override fun getItemCount() = tickets.size

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): TicketViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(
            R.layout.row_ticket, p0, false
        )

        return TicketViewHolder(view)
    }


    inner class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvName = itemView.tvName
        val btnSee = itemView.btnSee
    }
}