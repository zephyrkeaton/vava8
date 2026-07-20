package com.vava8.app.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vava8.app.BuildConfig
import com.vava8.app.ui.theme.BrandBlue

private const val PRIVACY_URL = "https://www.vava8.com/index.php?app=index&act=page&id=1"
private const val GITHUB_URL = "https://github.com/zephyrkeaton/vava8"
private const val FEEDBACK_EMAIL = "zephyrkeaton@hotmail.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun openEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$FEEDBACK_EMAIL")
            putExtra(Intent.EXTRA_SUBJECT, "Vava8 发吧 Android 反馈")
        }
        context.startActivity(Intent.createChooser(intent, "发送邮件"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于此 App") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Vava8 发吧",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = BrandBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "版本 ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(20.dp))
            AboutSection(title = "这是什么") {
                Text(
                    "本应用是面向 www.vava8.com 的非官方 Android 客户端，由独立开发者制作，" +
                        "用于在手机上浏览、搜索、发帖、评论及使用个人中心等功能。"
                )
            }

            AboutSection(title = "开发者") {
                Text("Zephrkeaton（个人 / 独立开发者）")
            }

            AboutSection(title = "与官网的关系") {
                Text(
                    "本应用非 vava8.com 官方出品，亦未获网站运营方授权或背书。" +
                        "应用名称与界面仅为方便识别，不代表与站点的隶属或合作关系。"
                )
            }

            AboutSection(title = "内容与版权") {
                Text(
                    "应用内展示的资讯、帖子、评论、图片等均由网站及用户发布，" +
                        "版权与相应责任归原作者及原站点所有。本应用仅作为访问与交互入口，" +
                        "不对第三方内容的完整性、真实性或合法性作担保。"
                )
            }

            AboutSection(title = "使用与责任") {
                Text(
                    "请遵守当地法律法规及站点《发吧规则》。请勿发布违法、侵权或不当内容；" +
                        "您对自己在站点上的发帖、评论等行为负责。如发现侵权内容，" +
                        "可通过站点「联系我们」或下方邮箱反馈，我们将协助按合理方式处理。"
                )
            }

            AboutSection(title = "隐私与本地数据") {
                Text(
                    "为提供登录与使用体验，本机可能保存：登录会话（Cookie）、浏览历史、" +
                        "主题与阅读偏好、发帖草稿、可选保存的账号密码等。上述数据主要保存在您的设备上，" +
                        "用于维持登录状态与恢复个人设置。站点侧数据处理请参阅官网隐私政策。"
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { openUrl(PRIVACY_URL) }) {
                    Text("查看官网隐私政策", color = BrandBlue)
                }
            }

            AboutSection(title = "开源") {
                Text("应用源码公开于 GitHub，欢迎查看、反馈与参与改进。")
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { openUrl(GITHUB_URL) }) {
                    Text(GITHUB_URL, color = BrandBlue)
                }
            }

            AboutSection(title = "问题与反馈") {
                Text("使用中遇到问题或有建议，欢迎联系：")
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = ::openEmail) {
                    Text(FEEDBACK_EMAIL, color = BrandBlue)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "本说明不构成法律意见；如有合规或责任方面的疑虑，请咨询专业律师。",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AboutSection(title: String, content: @Composable () -> Unit) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = BrandBlue
    )
    Spacer(modifier = Modifier.height(6.dp))
    Column {
        androidx.compose.material3.ProvideTextStyle(
            value = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
        ) {
            content()
        }
    }
}
