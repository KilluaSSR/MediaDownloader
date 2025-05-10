package killua.dev.mediadownloader.utils

import androidx.navigation.NavController

class Auth {

    private fun getBiometricHelper(): BiometricHelper? {
        return BiometricManagerSingleton.getBiometricHelper()
    }

    suspend fun authenticate(onAuthFailed: (String) -> Unit): Boolean {
        val biometricHelper = getBiometricHelper()
        if (biometricHelper == null) {
            onAuthFailed("生物识别服务未初始化")
            return false
        }

        if (biometricHelper.canAuthenticate()) {
            if (biometricHelper.authenticate()) {
                return true
            } else {
                onAuthFailed("认证失败")
                return false
            }
        } else {
            onAuthFailed("设备不支持生物识别")
            return false
        }
    }

    suspend fun authenticateAndNavigate(
        navController: NavController,
        route: String,
        onAuthFailed: (String) -> Unit
    ) {
        if (authenticate(onAuthFailed)) {
            navController.navigate(route)
        }
    }
}