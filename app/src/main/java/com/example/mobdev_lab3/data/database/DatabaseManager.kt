package com.example.mobdev_lab3.data.database

import android.content.Context
import com.example.mobdev_lab3.data.database.dao.DaoMaster
import com.example.mobdev_lab3.data.database.dao.DaoSession
import org.greenrobot.greendao.database.Database

object DatabaseManager {
    private var daoSession: DaoSession? = null

    fun init(context: Context) {
        if (daoSession == null) {
            val helper = DaoMaster.DevOpenHelper(context, "file-manager-db")
            val db: Database = helper.writableDb
            daoSession = DaoMaster(db).newSession()
        }
    }

    fun getDaoSession(): DaoSession {
        return daoSession ?: throw IllegalStateException("DatabaseManager must be initialized!")
    }
}
