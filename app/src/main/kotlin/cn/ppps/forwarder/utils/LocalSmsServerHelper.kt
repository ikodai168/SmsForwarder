package cn.ppps.forwarder.utils

import cn.ppps.forwarder.Core
import cn.ppps.forwarder.database.entity.Rule
import cn.ppps.forwarder.database.entity.Sender
import com.google.gson.Gson
import java.util.Date

object LocalSmsServerHelper {

    fun formatWebhookUrl(input: String): String {
        var url = input.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length - 1)
        }
        if (!url.endsWith("/webhook")) {
            url = "$url/webhook"
        }
        return url
    }

    fun applyServerConfig(
        serverUrl: String,
        sim1Phone: String,
        sim2Enable: Boolean,
        sim2Phone: String
    ): Boolean {
        val finalUrl = formatWebhookUrl(serverUrl)
        val gson = Gson()

        if (sim1Phone.isNotBlank()) {
            val s1Setting = gson.toJson(mapOf(
                "method" to "POST",
                "webServer" to finalUrl,
                "secret" to "",
                "response" to "",
                "webParams" to gson.toJson(mapOf("phone" to sim1Phone.trim(), "content" to "[content]")),
                "headers" to mapOf("Content-Type" to "application/json;charset=utf-8")
            ))
            val sender1 = Sender(
                id = 1L,
                type = TYPE_WEBHOOK,
                name = "Webhook-卡1",
                jsonSetting = s1Setting,
                status = 1,
                time = Date()
            )
            Core.sender.insert(sender1)

            val rule1 = Rule(
                id = 1L,
                type = "sms",
                filed = "transpond_all",
                check = "is",
                value = "",
                senderId = 1L,
                smsTemplate = "",
                regexReplace = "",
                simSlot = "SIM1",
                status = 1,
                time = Date(),
                senderList = listOf(sender1),
                senderLogic = "ALL",
                silentPeriodStart = 0,
                silentPeriodEnd = 0,
                silentDayOfWeek = "",
                title = "接码容器-卡1"
            )
            Core.rule.insert(rule1)
        }

        if (sim2Phone.isNotBlank()) {
            val s2Setting = gson.toJson(mapOf(
                "method" to "POST",
                "webServer" to finalUrl,
                "secret" to "",
                "response" to "",
                "webParams" to gson.toJson(mapOf("phone" to sim2Phone.trim(), "content" to "[content]")),
                "headers" to mapOf("Content-Type" to "application/json;charset=utf-8")
            ))
            val sender2 = Sender(
                id = 2L,
                type = TYPE_WEBHOOK,
                name = "Webhook-卡2",
                jsonSetting = s2Setting,
                status = if (sim2Enable) 1 else 0,
                time = Date()
            )
            Core.sender.insert(sender2)

            val rule2 = Rule(
                id = 2L,
                type = "sms",
                filed = "transpond_all",
                check = "is",
                value = "",
                senderId = 2L,
                smsTemplate = "",
                regexReplace = "",
                simSlot = "SIM2",
                status = if (sim2Enable) 1 else 0,
                time = Date(),
                senderList = listOf(sender2),
                senderLogic = "ALL",
                silentPeriodStart = 0,
                silentPeriodEnd = 0,
                silentDayOfWeek = "",
                title = "接码容器-卡2"
            )
            Core.rule.insert(rule2)
        }

        return true
    }
}
