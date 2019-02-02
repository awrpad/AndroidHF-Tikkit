package o2offx.awagner.tikkit

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.synthetic.main.activity_ticket_bought.*
import o2offx.awagner.tikkit.db.Ticket
import o2offx.awagner.tikkit.db.TicketDB
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// Ez az Activity hivatott megjeleníteni a már megvásárolt jegyeket.
// Kétféleképpen indítható el:
//   1.: A megvásárolt jegy URL-jét átadva (abban az esetben, ha épp most vették meg a jegyet).
//       Ekkor az indító Intent-ben szerepelnie kell egy "url" nevű extrának.
//       Amennyiben ez létezik, az Activity automatikusan el is menti a kapott jegyet.
//   2.: Egy már korábban megvásárolt jegyet úgy jeleníthetjük meg, ha a jegy JSON-ját átadjuk az Activity-nek.
//       Ebben az esetben az indító Intent-ben szerepelnie kell egy "json" nevű extrának.
//       Ha az Activity így lett elindítva, nem menti el a jegyet (hiszen feltételezi, hogy az egy már mentett jegy).
class TicketBoughtActivity : AppCompatActivity() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_bought)

        Thread {
            var rawTicketData: String
            var shouldPersist = false

            when {
                intent.extras["url"] != null -> {
                    rawTicketData = httpGet(intent.extras["url"].toString())

                    rawTicketData = rawTicketData.replace("&#39;", "'")
                    shouldPersist = true
                }
                intent.extras["json"] != null -> rawTicketData = intent.extras["json"].toString()
                else -> throw Exception("No data for the ticket.")
            }

            try{
                val writer = QRCodeWriter()
                val bitMx = writer.encode(rawTicketData, BarcodeFormat.QR_CODE, 350, 350)
                val bitMap = Bitmap.createBitmap(bitMx.width, bitMx.height, Bitmap.Config.RGB_565)
                for (x in 0 until bitMx.width){
                    for (y in 0 until bitMx.height){
                        bitMap.setPixel(x, y, if(bitMx[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
                val obj = JSONObject(rawTicketData)
                val t = Ticket(
                    obj.getString("_id"),
                    obj.getString("name"),
                    obj.getString("securityData"),
                    obj.getString("other"),
                    obj.getString("description")
                )

                runOnUiThread {
                    ivQR.setImageDrawable(BitmapDrawable(resources, bitMap))
                    tvTicketName.text = t.name
                    tvTicketDesc.text = t.desc
                }

                if (shouldPersist) {
                    TicketDB.getInstance(this@TicketBoughtActivity).ticketDAO().insert(t)
                }

            } catch (e: Exception){
                Log.w("bad", e.message)
            }

        }.start()
    }

    // Ez sajna majdnem teljesen copy-paselve lett az Inspector Activity-ből :(
    // 7-es labor alapján
    private fun httpGet(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()

        val res = client.newCall(request).execute()

        return res.body()?.string() ?: "ERROR: RES is EMPTY."
    }
}
