package com.example.rt.domain.repository

import com.example.rt.domain.model.User

/**
 * 用户数据仓库接口（定义于 shared，ViewModel/UseCase 只依赖接口）
 *
 * 实现位于 :feature:user（面向接口编程 + DI）。
 */
interface UserRepository {
    /** 从远端获取用户（实现内负责 ResponseBody 解包与 code 判断） */
    suspend fun fetchUser(id: String): User

    /** 保存到本地（当前用 CacheManager 持久化 JSON；后续可替换为 SQLDelight） */
    suspend fun saveUserLocally(user: User)
}
