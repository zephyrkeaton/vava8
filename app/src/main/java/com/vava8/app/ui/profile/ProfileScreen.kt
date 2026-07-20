package com.vava8.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vava8.app.Vava8App
import com.vava8.app.ui.theme.BrandBlue
import com.vava8.app.ui.theme.LocalReaderPrefs
import com.vava8.app.ui.theme.ReadingFontSize
import com.vava8.app.ui.theme.ThemeMode
import kotlinx.coroutines.launch

/** 站点个人中心相关页面（与 www.vava8.com 登录后菜单一致） */
object PersonalCenterUrls {
    const val MY_POSTS =
        "https://www.vava8.com/index.php?app=home&act=activity&type=posts"
    const val MY_REPLIES =
        "https://www.vava8.com/index.php?app=home&act=activity&type=comments"
    const val REPLIES_TO_ME =
        "https://www.vava8.com/index.php?app=home&act=activity&type=replies"
    const val MY_NOTIFICATIONS =
        "https://www.vava8.com/index.php?app=home&act=activity&type=notifications"
    const val MY_FAVORITES =
        "https://www.vava8.com/index.php?app=home&act=favorites"
    const val MY_MESSAGES =
        "https://www.vava8.com/index.php?app=home&act=im_list"
}

@Composable
fun ProfileScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onOpenWebInfo: (String, String) -> Unit,
    onOpenBrowseHistory: () -> Unit,
    onFontSizeChange: (ReadingFontSize) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onNoImageModeChange: (Boolean) -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val repo = Vava8App.instance.repository
    val appPrefs = Vava8App.instance.preferences
    val user by repo.user.collectAsStateWithLifecycle()
    val rememberLogin by appPrefs.rememberLogin.collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()
    val prefs = LocalReaderPrefs.current

    fun openPersonal(title: String, url: String) {
        if (!user.isLoggedIn) {
            onLogin()
        } else {
            onOpenWebInfo(title, url)
        }
    }

    Scaffold(bottomBar = bottomBar) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(BrandBlue.copy(0.12f))
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (user.isLoggedIn) user.username.ifBlank { "已登录用户" } else "未登录",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (user.isLoggedIn) "欢迎回到 Vava8 发吧" else "登录后可评论、收藏、发帖",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (!user.isLoggedIn) {
                    Row {
                        Button(onClick = onLogin, modifier = Modifier.weight(1f)) { Text("登录") }
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedButton(onClick = onRegister, modifier = Modifier.weight(1f)) {
                            Text("注册")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { scope.launch { repo.logout() } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("退出登录")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "个人中心",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandBlue,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
                HorizontalDivider()
                SettingRow(Icons.AutoMirrored.Outlined.Article, "我的主贴") {
                    openPersonal("我的主贴", PersonalCenterUrls.MY_POSTS)
                }
                HorizontalDivider()
                SettingRow(Icons.AutoMirrored.Outlined.Comment, "我的回复") {
                    openPersonal("我的回复", PersonalCenterUrls.MY_REPLIES)
                }
                HorizontalDivider()
                SettingRow(Icons.AutoMirrored.Outlined.Chat, "回复我的") {
                    openPersonal("回复我的", PersonalCenterUrls.REPLIES_TO_ME)
                }
                HorizontalDivider()
                SettingRow(Icons.Outlined.NotificationsNone, "我的提醒") {
                    openPersonal("我的提醒", PersonalCenterUrls.MY_NOTIFICATIONS)
                }
                HorizontalDivider()
                SettingRow(Icons.Outlined.BookmarkBorder, "我的收藏") {
                    openPersonal("我的收藏", PersonalCenterUrls.MY_FAVORITES)
                }
                HorizontalDivider()
                SettingRow(Icons.AutoMirrored.Outlined.Message, "我的私信") {
                    openPersonal("我的私信", PersonalCenterUrls.MY_MESSAGES)
                }
                HorizontalDivider()
                SettingRow(Icons.Outlined.History, "浏览历史") {
                    onOpenBrowseHistory()
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.DarkMode, null, tint = BrandBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("主题外观", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = prefs.themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            label = { Text(mode.label()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandBlue.copy(alpha = 0.14f),
                                selectedLabelColor = BrandBlue
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(1.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                SettingRow(Icons.Outlined.TextFields, "字体大小：${prefs.fontSize.label()}") {
                    val next = when (prefs.fontSize) {
                        ReadingFontSize.Small -> ReadingFontSize.Medium
                        ReadingFontSize.Medium -> ReadingFontSize.Large
                        ReadingFontSize.Large -> ReadingFontSize.ExtraLarge
                        ReadingFontSize.ExtraLarge -> ReadingFontSize.Small
                    }
                    onFontSizeChange(next)
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.ImageNotSupported, null, tint = BrandBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("无图模式", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "列表与正文不加载图片，节省流量",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.55f)
                        )
                    }
                    Switch(
                        checked = prefs.noImageMode,
                        onCheckedChange = onNoImageModeChange,
                        colors = SwitchDefaults.colors(checkedTrackColor = BrandBlue)
                    )
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Person, null, tint = BrandBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("保存用户名和密码", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "登录页可自动填入上次保存的账号",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.55f)
                        )
                    }
                    Switch(
                        checked = rememberLogin,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                if (enabled) {
                                    appPrefs.setRememberLogin(true)
                                } else {
                                    appPrefs.clearLoginCredentials()
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = BrandBlue)
                    )
                }
                HorizontalDivider()
                SettingRow(Icons.Outlined.MailOutline, "联系我们") {
                    onOpenWebInfo("联系我们", "https://www.vava8.com/index.php?app=feedback&act=index")
                }
                HorizontalDivider()
                SettingRow(Icons.Outlined.Info, "关于我们") {
                    onOpenWebInfo("关于我们", "https://www.vava8.com/index.php?app=index&act=page&id=3")
                }
                HorizontalDivider()
                SettingRow(Icons.Outlined.Policy, "隐私政策") {
                    onOpenWebInfo("隐私政策", "https://www.vava8.com/index.php?app=index&act=page&id=1")
                }
                HorizontalDivider()
                SettingRow(Icons.Outlined.Policy, "发吧规则") {
                    onOpenWebInfo("发吧规则", "https://www.vava8.com/index.php?app=index&act=page&id=2")
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "数据来源 www.vava8.com",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = BrandBlue)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun ReadingFontSize.label(): String = when (this) {
    ReadingFontSize.Small -> "小"
    ReadingFontSize.Medium -> "中"
    ReadingFontSize.Large -> "大"
    ReadingFontSize.ExtraLarge -> "特大"
}
