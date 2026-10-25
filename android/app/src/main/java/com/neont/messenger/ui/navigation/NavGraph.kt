// Neont Messenger - NavGraph.kt
// Navigasi Terpusat untuk Seluruh Layar (Splash, Login, Main, ChatDetail, Calls, Stories, Group/Channel, Settings)
package com.neont.messenger.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.neont.messenger.data.model.Chat
import com.neont.messenger.data.model.User
import com.neont.messenger.navigation.NeontAppViewModel
import com.neont.messenger.ui.screens.*

@Composable
fun NeontNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: NeontAppViewModel = hiltViewModel()
) {
    val chats by viewModel.chats.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val calls by viewModel.calls.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
    ) {
        // 1. Splash Screen (1.5 detik -> cek Auth)
        composable("splash") {
            SplashScreen(
                isUserLoggedIn = viewModel.authRepo.isUserLoggedIn,
                onNavigateNext = { dest ->
                    navController.navigate(dest) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 2. Auth Screen (100% Email & Password)
        composable("auth") {
            AuthScreen(
                onLogin = { email, pass ->
                    viewModel.signInWithEmail(email, pass) { user ->
                        if (user.firstName.isNotBlank()) {
                            navController.navigate("main") {
                                popUpTo("auth") { inclusive = true }
                            }
                        } else {
                            navController.navigate("profile_setup") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    }
                },
                onRegister = { email, pass ->
                    viewModel.createUserWithEmail(email, pass) { user ->
                        if (user.firstName.isNotBlank()) {
                            navController.navigate("main") {
                                popUpTo("auth") { inclusive = true }
                            }
                        } else {
                            navController.navigate("profile_setup") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    }
                },
                errorMessage = viewModel.authError,
                isLoading = viewModel.isAuthLoading
            )
        }

        // 4. Profile Setup Screen (for first time user onboarding)
        composable("profile_setup") {
            ProfileSetupScreen(
                initialEmail = viewModel.authRepo.currentUser?.email ?: "",
                onCompleteSetup = { first, last, username, bio ->
                    viewModel.updateProfile(first, last, bio, "") {
                        navController.navigate("main") { popUpTo("profile_setup") { inclusive = true } }
                    }
                }
            )
        }

        // 5. Main Screen (Telegram 4 Tabs BottomNav: Contacts, Calls, Chats, Settings)
        composable("main") {
            MainScreen(
                currentUser = currentUser,
                chats = chats,
                contacts = contacts,
                calls = calls,
                stories = stories,
                settings = settings,
                onChatClick = { chatId ->
                    navController.navigate("chat_detail/$chatId")
                },
                onContactClick = { user ->
                    navController.navigate("other_profile/${user.uid}")
                },
                onCallClick = { call ->
                    if (call.isVideo) {
                        navController.navigate("video_call/${call.contactName}")
                    } else {
                        navController.navigate("voice_call/${call.contactName}")
                    }
                },
                onStoryClick = { story ->
                    navController.navigate("story_viewer/${story.storyId}")
                },
                onAddStoryClick = {
                    navController.navigate("story_camera")
                },
                onSearchClick = {
                    navController.navigate("search")
                },
                onNewGroupClick = {
                    navController.navigate("create_group")
                },
                onEditProfileClick = {
                    navController.navigate("edit_profile")
                },
                onPrivacyClick = {
                    navController.navigate("privacy_settings")
                },
                onNotificationClick = {
                    navController.navigate("notification_settings")
                },
                onTogglePin = { chatId -> viewModel.togglePinChat(chatId) },
                onToggleMute = { chatId -> viewModel.toggleMuteChat(chatId) },
                onDeleteChat = { chatId -> viewModel.deleteChat(chatId) },
                onLogoutClick = {
                    viewModel.logout {
                        navController.navigate("auth") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                }
            )
        }

        // 6. Chat Detail Screen
        composable(
            route = "chat_detail/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val chat = chats.find { it.chatId == chatId } ?: Chat(chatId = chatId, title = "Chat")
            val messages by remember(chatId) { viewModel.chatRepo.listenToMessages(chatId) }.collectAsState(initial = emptyList())
            val currentUid = viewModel.authRepo.currentUser?.uid ?: "me"
            val otherUid = chat.memberUids.firstOrNull { it != currentUid } ?: "u_pavel"

            val presenceInfo by remember(otherUid) { viewModel.chatRepo.observePresence(otherUid) }.collectAsState(initial = Pair(true, 0L))
            val typers by remember(chatId) { viewModel.chatRepo.observeTyping(chatId, currentUid) }.collectAsState(initial = emptyList())

            ChatDetailScreen(
                chat = chat,
                messages = messages,
                currentUserId = currentUid,
                isContactOnline = presenceInfo.first,
                isContactTyping = typers.isNotEmpty(),
                onBackClick = { navController.popBackStack() },
                onSendMessage = { text ->
                    viewModel.sendMessage(chatId, text)
                },
                onTyping = { isTyping ->
                    viewModel.setTyping(chatId, isTyping)
                },
                onDeleteMessage = { msg ->
                    viewModel.deleteMessage(chatId, msg.msgId)
                },
                onCallClick = {
                    navController.navigate("voice_call/${chat.title}")
                }
            )
        }

        // 7. Voice Call Screen
        composable(
            route = "voice_call/{contactName}",
            arguments = listOf(navArgument("contactName") { type = NavType.StringType })
        ) { backStackEntry ->
            val contactName = backStackEntry.arguments?.getString("contactName") ?: "Contact"
            VoiceCallScreen(
                contactName = contactName,
                onEndCall = { durationSec ->
                    viewModel.logCall(contactName, durationSec, isVideo = false)
                    navController.popBackStack()
                }
            )
        }

        // 8. Video Call Screen
        composable(
            route = "video_call/{contactName}",
            arguments = listOf(navArgument("contactName") { type = NavType.StringType })
        ) { backStackEntry ->
            val contactName = backStackEntry.arguments?.getString("contactName") ?: "Contact"
            VideoCallScreen(
                contactName = contactName,
                onEndCall = { durationSec ->
                    viewModel.logCall(contactName, durationSec, isVideo = true)
                    navController.popBackStack()
                }
            )
        }

        // 9. Incoming Call Screen
        composable(
            route = "incoming_call/{callerName}",
            arguments = listOf(navArgument("callerName") { type = NavType.StringType })
        ) { backStackEntry ->
            val callerName = backStackEntry.arguments?.getString("callerName") ?: "Unknown"
            IncomingCallScreen(
                callerName = callerName,
                onAcceptCall = {
                    navController.popBackStack()
                    navController.navigate("voice_call/$callerName")
                },
                onRejectCall = { navController.popBackStack() }
            )
        }

        // 10. Profile Screen (Own Profile)
        composable("profile") {
            currentUser?.let { user ->
                ProfileScreen(
                    user = user,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { navController.navigate("edit_profile") }
                )
            }
        }

        // 11. Edit Profile Screen
        composable("edit_profile") {
            EditProfileScreen(
                currentUser = currentUser,
                onSaveProfile = { first, last, bio, avatar ->
                    viewModel.updateProfile(first, last, bio, avatar) {
                        navController.popBackStack()
                    }
                },
                onBackClick = { navController.popBackStack() },
                isLoading = viewModel.isProfileUpdating
            )
        }

        // 12. Other Profile Screen (Contact's Profile)
        composable(
            route = "other_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val user = contacts.find { it.uid == userId } ?: User(uid = userId, firstName = "Contact")
            OtherProfileScreen(
                user = user,
                onBackClick = { navController.popBackStack() },
                onSendMessageClick = { navController.navigate("chat_detail/c_$userId") },
                onVoiceCallClick = { navController.navigate("voice_call/${user.firstName}") },
                onVideoCallClick = { navController.navigate("video_call/${user.firstName}") }
            )
        }

        // 13. Settings Screen
        composable("settings") {
            SettingsScreen(
                currentUser = currentUser,
                settings = settings,
                onEditProfileClick = { navController.navigate("edit_profile") },
                onPrivacyClick = { navController.navigate("privacy_settings") },
                onNotificationClick = { navController.navigate("notification_settings") },
                onLogoutClick = {
                    viewModel.logout {
                        navController.navigate("auth") { popUpTo("main") { inclusive = true } }
                    }
                }
            )
        }

        // 14. Privacy Settings Screen
        composable("privacy_settings") {
            PrivacySettingsScreen(onBackClick = { navController.popBackStack() })
        }

        // 15. Notification Settings Screen
        composable("notification_settings") {
            NotificationSettingsScreen(onBackClick = { navController.popBackStack() })
        }

        // 16. Create Group Screen (Select contacts + Group Name)
        composable("create_group") {
            CreateGroupScreen(
                contacts = contacts,
                onGroupCreated = { name, uids ->
                    viewModel.createGroup(name, uids)
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 17. Group Info Screen
        composable(
            route = "group_info/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val group = chats.find { it.chatId == groupId } ?: Chat(chatId = groupId, title = "Group")
            GroupInfoScreen(
                group = group,
                members = contacts,
                onBackClick = { navController.popBackStack() },
                onAddMemberClick = {}
            )
        }

        // 18. Channel Screen
        composable(
            route = "channel/{channelId}",
            arguments = listOf(navArgument("channelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
            val channel = chats.find { it.chatId == channelId } ?: Chat(chatId = channelId, title = "Channel")
            val messages by remember(channelId) { viewModel.chatRepo.listenToMessages(channelId) }.collectAsState(initial = emptyList())
            ChannelScreen(
                channel = channel,
                messages = messages,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 19. Search Screen
        composable("search") {
            SearchScreen(
                chats = chats,
                contacts = contacts,
                onChatClick = { chatId -> navController.navigate("chat_detail/$chatId") },
                onContactClick = { user -> navController.navigate("other_profile/${user.uid}") },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 20. Story Viewer Screen (Segmented Progress Bar + 5s auto progress)
        composable(
            route = "story_viewer/{storyId}",
            arguments = listOf(navArgument("storyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getString("storyId") ?: ""
            val story = stories.find { it.storyId == storyId } ?: stories.firstOrNull()
            if (story != null) {
                StoryViewerScreen(
                    story = story,
                    onStoryFinished = { navController.popBackStack() },
                    onCloseClick = { navController.popBackStack() }
                )
            }
        }

        // 21. Story Camera Screen (CameraX Live Preview + Capture)
        composable("story_camera") {
            StoryCameraScreen(
                onPhotoCaptured = { photoPath ->
                    viewModel.addStory(photoPath)
                    navController.popBackStack()
                },
                onCloseClick = { navController.popBackStack() }
            )
        }
    }
}
