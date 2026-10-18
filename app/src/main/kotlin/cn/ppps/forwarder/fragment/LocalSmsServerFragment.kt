package cn.ppps.forwarder.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import cn.ppps.forwarder.R
import cn.ppps.forwarder.core.BaseFragment
import cn.ppps.forwarder.databinding.FragmentEmptyBinding
import cn.ppps.forwarder.utils.LocalSmsServerHelper
import cn.ppps.forwarder.utils.SharedPreference
import cn.ppps.forwarder.utils.XToastUtils
import com.xuexiang.xpage.annotation.Page
import com.xuexiang.xui.widget.actionbar.TitleBar
import com.xuexiang.xui.widget.button.roundbutton.RoundButton

@Page(name = "自建接码容器设置")
class LocalSmsServerFragment : BaseFragment<FragmentEmptyBinding?>() {

    private lateinit var etServerUrl: EditText
    private lateinit var etSim1Phone: EditText
    private lateinit var cbSim2Enable: CheckBox
    private lateinit var etSim2Phone: EditText

    override fun viewBindingInflate(inflater: LayoutInflater, container: ViewGroup): FragmentEmptyBinding {
        return FragmentEmptyBinding.inflate(inflater, container, false)
    }

    override fun initTitle(): TitleBar? {
        val titleBar = super.initTitle()!!.setImmersive(false)
        titleBar.setTitle("自建接码容器一键配置")
        return titleBar
    }

    override fun initViews() {
        val context = requireContext()
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        root.addView(TextView(context).apply {
            text = "1. WebServer 地址 (IP+端口):"
            textSize = 15f
        })
        etServerUrl = EditText(context).apply {
            hint = "例如: http://192.168.8.162:20086"
            setText(SharedPreference.getString("sms_local_server_url", "http://192.168.8.162:20086"))
        }
        root.addView(etServerUrl)

        root.addView(TextView(context).apply {
            text = "\n2. 卡1 (SIM1) 手机号:"
            textSize = 15f
        })
        etSim1Phone = EditText(context).apply {
            hint = "输入卡1真实11位手机号"
            setText(SharedPreference.getString("sms_local_sim1_phone", ""))
        }
        root.addView(etSim1Phone)

        cbSim2Enable = CheckBox(context).apply {
            text = "启用 卡2 (SIM2) 转发"
            isChecked = SharedPreference.getBoolean("sms_local_sim2_enable", true)
        }
        root.addView(cbSim2Enable)
        etSim2Phone = EditText(context).apply {
            hint = "输入卡2真实手机号 (无卡2留空即可)"
            setText(SharedPreference.getString("sms_local_sim2_phone", ""))
        }
        root.addView(etSim2Phone)

        val btnSave = RoundButton(context).apply {
            text = "保存并生效"
            textSize = 16f
            setOnClickListener {
                val server = etServerUrl.text.toString().trim()
                val sim1 = etSim1Phone.text.toString().trim()
                val sim2Enable = cbSim2Enable.isChecked
                val sim2 = etSim2Phone.text.toString().trim()

                if (server.isEmpty() || sim1.isEmpty()) {
                    XToastUtils.error("请至少填入 WebServer 地址和卡1手机号！")
                    return@setOnClickListener
                }

                SharedPreference.putString("sms_local_server_url", server)
                SharedPreference.putString("sms_local_sim1_phone", sim1)
                SharedPreference.putBoolean("sms_local_sim2_enable", sim2Enable)
                SharedPreference.putString("sms_local_sim2_phone", sim2)

                LocalSmsServerHelper.applyServerConfig(server, sim1, sim2Enable, sim2)
                XToastUtils.success("接码配置已自动保存并生效！")
                popToBack()
            }
        }
        val btnParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 60 }
        root.addView(btnSave, btnParams)

        (binding?.root as? ViewGroup)?.addView(root)
    }
}
