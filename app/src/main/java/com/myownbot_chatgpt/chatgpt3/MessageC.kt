package com.myownbot_chatgpt.chatgpt3

import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.IUser
import com.stfalcon.chatkit.commons.models.MessageContentType
import java.util.*

class MessageC(val mid:String, val mtext : String, val muser : IUser, val mdate : Date, var mUrl : String) : IMessage,
    MessageContentType.Image {
    override fun getId(): String {
        return mid
    }

    override fun getText(): String {
       return mtext
    }

    override fun getUser(): IUser {
        return muser
    }

    override fun getCreatedAt(): Date {
        return mdate
    }

    override fun getImageUrl(): String? {
        if (mUrl.equals("")){
            return null
        }
       return mUrl
    }
}