package com.zetavision.panda.ums.utils

import com.zetavision.panda.ums.model.User
import org.litepal.crud.DataSupport
import java.util.*

/**
 * Created by wheroj on 2018/2/2 10:26.
 * @describe
 */
object UserUtils {

    private var advanceRemindDate = 3*60


    /**
     * 获取当前登录的用户
     */
    fun getCurretnLoginUser(): User? {
        val userList = DataSupport.findAll(User::class.java)
        if (userList != null) {
            for (i in userList.indices) {
                if (User.LOGIN == userList[i].isCurrentLogin)
                    return userList[i]
            }
        }
        return null
    }

    /**
     * 清空所有登录状态的用户
     */
    fun clearLogin(users: List<User>? = null) {
        var userList: List<User> = users ?: DataSupport.findAll(User::class.java)
        for (i in userList.indices) {
            if (userList[i].isCurrentLogin != User.LOGINOUT) {
                userList[i].isCurrentLogin = User.LOGINOUT
                userList[i].loginTime = -1
                userList[i].saveOrUpdate("USERNAME='" + userList[i].USERNAME + "'")
            }
        }
    }

    /**
     * 删除所有用户
     */
    fun removeAllUser() {
        DataSupport.deleteAll(User::class.java)
    }

    /**
     * 设置某一个用户为登录状态
     */
    fun setUserLogin(user: User) {
        var userList = DataSupport.findAll(User::class.java)
        clearLogin(userList)
        user.isCurrentLogin = User.LOGIN
        user.loginTime = System.currentTimeMillis()
        user.saveOrUpdate("USERNAME='" + user.USERNAME + "'")
    }

    /**
     * 提前三分钟判断token是否过期
     */
    fun isTokenGoingOutOfDate(): Boolean {
        val userEntity = getCurretnLoginUser()
        return if (userEntity != null) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = userEntity.loginTime
            println("curentSaveTime=" + calendar.timeInMillis)
            calendar.add(Calendar.SECOND, userEntity.expireIn - advanceRemindDate)
            println("outDateSaveTime=" + calendar.timeInMillis)
            println("currentSystemTime=" + System.currentTimeMillis())
            calendar.timeInMillis <= System.currentTimeMillis()
        } else true
    }

    /**
     * token已经过期
     */
    fun isTokenOutOfDate(): Boolean {
        val userEntity = getCurretnLoginUser()
        return if (userEntity != null) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = userEntity.loginTime
            println("curentSaveTime=" + calendar.timeInMillis)
            calendar.add(Calendar.SECOND, userEntity.expireIn)
            println("outDateSaveTime=" + calendar.timeInMillis)
            println("currentSystemTime=" + System.currentTimeMillis())
            calendar.timeInMillis <= System.currentTimeMillis()
        } else true
    }

    /**
     * 根据UserName获取用户
     */
    fun getUserByName(userName: String): User? {
        val sql = "USERNAME = '$userName'"
        return DataSupport.where(sql).findFirst(User::class.java)
    }
}