package com.myownbot_chatgpt.chatgpt3

import com.stfalcon.chatkit.commons.models.IUser

class UserC(val mId: String, val mName:String, val mAvatar:String) : IUser {
    override fun getId(): String {
        return mId
    }

    override fun getName(): String {
        return mName
    }

    override fun getAvatar(): String {
        return mAvatar
    }
}