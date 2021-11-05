package net.bortolan.tradinglog

import android.app.Application
import net.bortolan.tradinglog.db.TradingDatabase

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        TradingDatabase.init(this)
    }
}