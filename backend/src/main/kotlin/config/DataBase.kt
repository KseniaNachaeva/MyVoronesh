package config

import org.jetbrains.exposed.sql.Database

object DataBase {

    fun init() {
        Database.connect(
            url = "jdbc:mysql://localhost:3306/MyVoronesh?useSSL=false&serverTimezone=UTC",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = ""
        )
    }
}
