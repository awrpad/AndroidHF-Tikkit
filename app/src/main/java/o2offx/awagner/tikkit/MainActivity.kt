package o2offx.awagner.tikkit

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val tag = "TIKKIT->Main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Intent az Inspector típusú felhasználók felületének indításához
        val inspectorIntent = Intent(this, InspectorActivity::class.java)
        inspectorIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val boughtIntent = Intent(this, TicketBoughtActivity::class.java);

        webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                // HA a felhasználó Inspector  típusú
                // AKKOR nem a WebView-en folytatjuk,
                //          hanem az Inspector Activity-én
                if(url!!.endsWith("appinspect")){
                    startActivity(inspectorIntent)
                }

                // Kezeljük azt is, amikor egy felhasználó megvett egy jegyet
                if (url.contains("/bought/")){
                    view!!.stopLoading()
                    boughtIntent.putExtra("url", url)
                    startActivity(boughtIntent)
                }
            }
        }

        btnSeeTickets.setOnClickListener {
            val i = Intent(this, MyTicketsActivity::class.java)
            startActivity(i)
        }

        // Az app indulásakor betöltjük a bejelentkező oldalt
        webview.loadUrl("http://" + getString(R.string.server) + "/applogin")
    }

    override fun onBackPressed() {
        if(webview.canGoBack())
            webview.goBack()
        else
            super.onBackPressed()
    }

}
