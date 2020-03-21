package com.example.mprog_project

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Serializer() {
    companion object {
        inline fun <reified T> fromJson(json: String): T {
            return Gson().fromJson(json, object: TypeToken<T>(){}.type)
        }
    }
}