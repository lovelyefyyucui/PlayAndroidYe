package com.yfy.network.service

import com.yfy.model.model.ArticleList
import com.yfy.model.model.BaseModel
import com.yfy.model.room.entity.ProjectClassify
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 工程
 */
interface ProjectService {

    @GET("project/tree/json")
    suspend fun getProjectTree(): BaseModel<List<ProjectClassify>>

    @GET("project/list/{page}/json")
    suspend fun getProject(@Path("page") page: Int, @Query("cid") cid: Int): BaseModel<ArticleList>

}