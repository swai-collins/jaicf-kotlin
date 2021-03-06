package com.justai.jaicf.channel.telegram

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.BotChannel
import com.justai.jaicf.channel.jaicp.JaicpExternalPollingChannelFactory
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.*
import okhttp3.logging.HttpLoggingInterceptor

class TelegramChannel(
    override val botApi: BotApi,
    private val telegramBotToken: String,
    private val telegramApiUrl: String = "https://api.telegram.org/bot",
    private val telegramLogLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BASIC
) : BotChannel {

    fun run() {
        bot {
            apiUrl = telegramApiUrl
            token = telegramBotToken
            logLevel = telegramLogLevel

            dispatch {

                fun process(request: TelegramBotRequest) {
                    botApi.process(request, TelegramReactions(bot, request))
                }

                text { _, update ->
                    update.message?.let {
                        process(TelegramTextRequest(it))
                    }
                }

                callbackQuery { _, update ->
                    update.callbackQuery?.let {
                        process(TelegramQueryRequest(it.message!!, it.data))
                    }
                }

                location { _, update, location ->
                    update.message?.let {
                        process(TelegramLocationRequest(it, location))
                    }
                }

                contact { _, update, contact ->
                    update.message?.let {
                        process(TelegramContactRequest(it, contact))
                    }
                }
            }
        }.startPolling()
    }

    companion object : JaicpExternalPollingChannelFactory {
        override val channelType = "telegram"
        override fun createAndRun(botApi: BotApi, apiUrl: String) = TelegramChannel(
            botApi = botApi,
            telegramBotToken = "",
            telegramApiUrl = apiUrl,
            telegramLogLevel = HttpLoggingInterceptor.Level.BASIC
        ).apply { run() }
    }
}