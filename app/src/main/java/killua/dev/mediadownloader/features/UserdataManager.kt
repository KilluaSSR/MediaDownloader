package killua.dev.mediadownloader.features

import android.content.Context
import killua.dev.mediadownloader.datastore.ApplicationUserDataLofter
import killua.dev.mediadownloader.datastore.ApplicationUserDataTwitter
import killua.dev.mediadownloader.datastore.readApplicationUserAuth
import killua.dev.mediadownloader.datastore.readApplicationUserCt0
import killua.dev.mediadownloader.datastore.readApplicationUserID
import killua.dev.mediadownloader.datastore.readDelay
import killua.dev.mediadownloader.datastore.readKuaikanPassToken
import killua.dev.mediadownloader.datastore.readLofterEndTime
import killua.dev.mediadownloader.datastore.readLofterLoginAuth
import killua.dev.mediadownloader.datastore.readLofterLoginKey
import killua.dev.mediadownloader.datastore.readLofterStartTime
import killua.dev.mediadownloader.datastore.readMissEvanToken
import killua.dev.mediadownloader.datastore.readPixivPHPSSID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataManager @Inject constructor(
    private val context: Context,
    scope: CoroutineScope
) {
    private val _userData = MutableStateFlow(ApplicationUserDataTwitter("", "", ""))
    val userTwitterData: StateFlow<ApplicationUserDataTwitter> = _userData.asStateFlow()

    private val _userLofterData = MutableStateFlow(ApplicationUserDataLofter("", "", 0, 0))
    val userLofterData: StateFlow<ApplicationUserDataLofter> = _userLofterData.asStateFlow()


    private val _userPixivPHPSSID = MutableStateFlow("")
    val userPixivPHPSSID: StateFlow<String> = _userPixivPHPSSID.asStateFlow()

    private val _userKuaikanData = MutableStateFlow("")
    val userKuaikanData: StateFlow<String> = _userKuaikanData.asStateFlow()

    private val _userMissEvanData = MutableStateFlow("")
    val userMissEvanData: StateFlow<String> = _userMissEvanData.asStateFlow()

    private val _delay = MutableStateFlow(2)
    val delay : StateFlow<Int> = _delay.asStateFlow()

    init {
        scope.launch {
            // Twitter 数据流
            combine(
                context.readApplicationUserCt0(),
                context.readApplicationUserAuth(),
                context.readApplicationUserID()
            ) { ct0, auth, twid ->
                ApplicationUserDataTwitter(ct0, auth, twid)
            }.collect {
                _userData.value = it
            }
        }

        scope.launch {
            // Lofter 数据流
            combine(
                context.readLofterLoginKey(),
                context.readLofterLoginAuth(),
                context.readLofterStartTime(),
                context.readLofterEndTime()
            ) { key, auth, startTime, endTime ->
                ApplicationUserDataLofter(
                    login_key = key,
                    login_auth = auth,
                    start_time = startTime,
                    end_time = endTime
                )
            }.collect {
                _userLofterData.value = it
            }
        }

        scope.launch{
            context.readKuaikanPassToken().collect{
                _userKuaikanData.value = it
            }
        }

        scope.launch{
            context.readDelay().collect{
                _delay.value = it
            }
        }

        scope.launch{
            context.readPixivPHPSSID().collect{
                _userPixivPHPSSID.value = it
            }
        }

        scope.launch{
            context.readMissEvanToken().collect{
                _userMissEvanData.value = it
            }
        }
    }
}