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

        if (sim1Phone.isNotBlank()) {
            syncSingleSim(
                simSlot = "SIM1",
                senderName = "Webhook-卡1",
                ruleName = "接码容器-卡1",
                phone = sim1Phone.trim(),
                webhookUrl = finalUrl,
                enabled = true
            )
        }

        if (sim2Phone.isNotBlank()) {
            syncSingleSim(
                simSlot = "SIM2",
                senderName = "Webhook-卡2",
                ruleName = "接码容器-卡2",
                phone = sim2Phone.trim(),
                webhookUrl = finalUrl,
                enabled = sim2Enable
            )
        }
        return true
    }

    private fun syncSingleSim(
        simSlot: String,
        senderName: String,
        ruleName: String,
        phone: String,
        webhookUrl: String,
        enabled: Boolean
    ) {
        val gson = Gson()
        val settingMap = mapOf(
            "method" to "POST",
            "webServer" to webhookUrl,
            "secret" to "",
            "response" to "",
            "webParams" to gson.toJson(mapOf("phone" to phone, "content" to "[content]")),
            "headers" to mapOf("Content-Type" to "application/json;charset=utf-8")
        )
        val jsonSetting = gson.toJson(settingMap)

        val senders = Core.sender.getAll()
        var targetSender = senders.find { it.name == senderName }

        if (targetSender == null) {
            targetSender = Sender(
                id = 0,
                type = TYPE_WEBHOOK,
                name = senderName,
                jsonSetting = jsonSetting,
                status = if (enabled) 1 else 0,
                time = Date()
            )
            val newId = Core.sender.insert(targetSender)
            targetSender.id = newId
        } else {
            targetSender.jsonSetting = jsonSetting
            targetSender.status = if (enabled) 1 else 0
            Core.sender.update(targetSender)
        }

        val rules = Core.rule.getAll()
        var targetRule = rules.find { it.title == ruleName || (it.simSlot == simSlot && it.senderId == targetSender.id) }

        if (targetRule == null) {
            targetRule = Rule(
                id = 0,
                type = "sms",
                filed = "transpond_all",
                check = "is",
                value = "",
                senderId = targetSender.id,
                simSlot = simSlot,
                status = if (enabled) 1 else 0,
                time = Date(),
                senderList = listOf(targetSender),
                senderLogic = "ALL",
                title = ruleName
            )
            Core.rule.insert(targetRule)
        } else {
            targetRule.status = if (enabled) 1 else 0
            targetRule.senderId = targetSender.id
            targetRule.senderList = listOf(targetSender)
            Core.rule.update(targetRule)
        }
    }
}
