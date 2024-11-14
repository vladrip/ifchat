package com.vladrip.ifchat.data.network

import com.haroldadmin.cnradapter.NetworkResponse
import com.vladrip.ifchat.data.entity.ChatMemberShort
import com.vladrip.ifchat.data.entity.Message
import com.vladrip.ifchat.data.entity.Person
import com.vladrip.ifchat.data.network.model.BooleanWrapper
import com.vladrip.ifchat.data.network.model.ChatCreateDto
import com.vladrip.ifchat.data.network.model.ChatDto
import com.vladrip.ifchat.data.network.model.ErrorResponse
import com.vladrip.ifchat.data.network.model.PagedResponse
import com.vladrip.ifchat.data.network.model.StringWrapper
import com.vladrip.ifchat.data.network.model.UserChatMemberDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

const val CHAT_LIST_NETWORK_PAGE_SIZE = 20
const val MESSAGE_NETWORK_PAGE_SIZE = 25
const val CHAT_MEMBERS_PAGE_SIZE = 30

interface IFChatService {

    @GET("chats")
    suspend fun getChatList(
        @Query("page") page: Int,
        @Query("size") size: Int = CHAT_LIST_NETWORK_PAGE_SIZE,
    ): NetworkResponse<PagedResponse<_root_ide_package_.com.vladrip.ifchat.data.entity.ChatListItem>, ErrorResponse>

    @GET("chats/{id}")
    suspend fun getChat(
        @Path("id") id: Long,
    ): NetworkResponse<ChatDto, ErrorResponse>

    @GET("chats/private")
    suspend fun getPrivateChat(
        @Query("otherPersonUid") otherPersonUid: String,
    ): NetworkResponse<ChatDto, ErrorResponse>

    @GET("chats/{id}/messages")
    suspend fun getMessages(
        @Path("id") chatId: Long,
        @Query("afterId") afterId: Long = 0,
        @Query("beforeId") beforeId: Long = 0,
        @Query("limit") limit: Int = MESSAGE_NETWORK_PAGE_SIZE,
    ): List<Message>

    @GET("chats/{id}/members")
    suspend fun getMembers(
        @Path("id") chatId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int = CHAT_MEMBERS_PAGE_SIZE,
    ): NetworkResponse<PagedResponse<ChatMemberShort>, ErrorResponse>

    @POST("chats")
    suspend fun createChat(
        @Body chatData: ChatCreateDto,
    ): NetworkResponse<ChatDto, ErrorResponse>

    @POST("messages")
    suspend fun saveMessage(
        @Body message: Message,
    ): NetworkResponse<Unit, ErrorResponse>

    @DELETE("messages/{id}")
    suspend fun deleteMessage(
        @Path("id") id: Long,
    ): NetworkResponse<Unit, ErrorResponse>

    @POST("devices")
    suspend fun saveDeviceToken(
        @Body deviceToken: StringWrapper,
    ): NetworkResponse<Unit, ErrorResponse>

    @DELETE("devices/{deviceToken}")
    suspend fun deleteDeviceToken(
        @Path("deviceToken") deviceToken: String,
    ): NetworkResponse<Unit, ErrorResponse>

    @PUT("persons/{uid}/chats/{chatId}/chat-muted")
    suspend fun setIsChatMuted(
        @Path("uid") personUid: String,
        @Path("chatId") chatId: Long,
        @Body isChatMuted: BooleanWrapper,
    ): NetworkResponse<UserChatMemberDto, ErrorResponse>

    @GET("persons/{uid}")
    suspend fun getPerson(
        @Path("uid") uid: String,
    ): NetworkResponse<Person, ErrorResponse>

    @POST("persons/registered")
    suspend fun getRegisteredPersons(
        @Body phoneNumbers: List<String>,
    ): NetworkResponse<List<Person>, ErrorResponse>
}