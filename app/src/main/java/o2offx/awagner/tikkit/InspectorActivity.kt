package o2offx.awagner.tikkit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_inspector.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import o2offx.awagner.tikkit.model.Ticket
import o2offx.awagner.tikkit.model.ValidationResult
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class InspectorActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private val CAMERA_GRANTED = 256 // Csak hogy kerek szám legyen
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspector)

        scroll.fullScroll(View.FOCUS_DOWN)

        // Megnézzük, hogy van-e jogosultságunk a kamerához
        val permissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permissionResult == PackageManager.PERMISSION_GRANTED) {
            // Ha van, beállítjuk, hogy ez az activity lesz az, aki kezeli
            //       a ZXing-től visszajövő eredményt
            // És elindítjuk a kamerát.
            zxingview.setResultHandler(this)
            zxingview.startCamera()
        } else {
            // Ha nincs engedélyünk a kamera használatára, kérünk
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_GRANTED)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CAMERA_GRANTED) {
            // Csakazértis beállítjuk az eredménykezelőt és elindítjuk a kemerát.
            zxingview.setResultHandler(this)
            zxingview.startCamera()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun handleResult(p0: Result?) {
        tvInspTest.append("\n> VALIDAING...")

        // Külön szálon elküldjük a szervernek az adatokat és várunk a válaszra
        Thread {
            // Ide írjuk majd, mi volt a validáció eredménye
            // Kezdésnek nem tekintjük jegynek a beolvasott kódot
            var result = ValidationResult.NOT_A_TICKET
            val t: Ticket

            try {
                // A beolvasott kód jó esetben egy JSON objektum
                val obj = JSONObject(p0.toString())
                Log.w("tst", "Heeellllooooo")

                // A szerver 4 érték alapján ellenőrzi a jegyet:
                //   - _id
                //   - name
                //   - securityData
                //   - other
                // Ha ebből az ID vagy a név null vagy üres string, a jegy formátuma érvénytelen.
                t = Ticket(
                    obj.getString("_id"),
                    obj.getString("name"),
                    obj.getString("securityData") ?: "",
                    obj.getString("other") ?: ""
                )
                Log.w("tst", "t done.")

                // Ellenőrizzük, hogy a szükségea paraméterek megvannak-e
                // HA NINCSENEK, hibát dobunk
                if (!t.isFormatValid())
                    throw Exception("Invalid ticket form.")

                Log.w("tst", "format validated.")

                // HA a jegy valamely nem kötelező része üres
                // AKKOR azt kitöltjük, hogy a szerver tudja kezelni a kérést
                if (t.securityData == "")
                    t.securityData = "_"
                if (t.other == "")
                    t.other = "_"

                Log.w("tst", "t very done")

                // Mostmár tudjuk, hogy egy jegy volt, amit beolvastunk
                // DE még ellenőrizni kell, hogy érvényes-e
                // ÚGYHOGY egyelőre invalidnak tekintjük.
                result = ValidationResult.INVALID

            } catch (e: Exception) {
                Log.w("bad", e.message)

                // Ha ide eljutunk, az azért lehet, mert a jegy formátuma nem megfelelő.
                result = ValidationResult.NOT_A_TICKET

                // Ha eljutunk ide, külön be kell állítani a visszajelzést az Inspector felületen
                // (Mert a lejjebb lévő runOnUiThread nem fog lefutni)
                runOnUiThread {
                    tvInspTest.append(
                        "\n" +
                                """| VALIDATING DONE. RESULT:
                                / NaT."""
                    )
                }
                return@Thread
            }

            // Az előzőkben ellenőriztük a beolvasott jegy formátumát
            // Amennyiben az megfelelő, a beolvasott adatot elküldjük a szervernek.
            if (result != ValidationResult.NOT_A_TICKET) {
                Log.w("tst", "networking...")

                val reqURL = "/appinspect/${encode(t.id)}" +
                        "/${encode(t.name)}" +
                        "/${encode(t.securityData)}" +
                        "/${encode(t.other)}"

                Log.w("tst", "encoded: $reqURL.")
                val serverRes = httpGet(reqURL)
                Log.w("tst", "HTTP-got")

                Log.w("tst", "testing server response: $serverRes")
                when (serverRes.trim()) {
                    "valid" -> result = ValidationResult.VALID
                    "invalid" -> result = ValidationResult.INVALID
                }

                Log.w("tst", "ALL DONE.")
            }

            runOnUiThread {
                tvInspTest.append("\n| VALIDATING DONE. RESULT:\n")
                when (result) {
                    ValidationResult.VALID -> tvInspTest.append("+ VALID.")
                    ValidationResult.INVALID -> tvInspTest.append("- INvalid.")
                    ValidationResult.NOT_A_TICKET -> tvInspTest.append("/ NaT.")
                }
            }
        }.start()

        zxingview.resumeCameraPreview(this)
    }

    // 7-es labor alapján
    private fun httpGet(url: String): String {
        val request = Request.Builder()
            .url("http://${getString(R.string.server)}$url")
            .build()

        Log.w("call", "http://${getString(R.string.server)}$url")

        val res = client.newCall(request).execute()

        Log.w("call", "Answer from server: $res")

        return res.body()?.string() ?: "ERROR: RES is EMPTY."
    }

    private fun encode(url: String) = URLEncoder.encode(url, "UTF-8")
}
