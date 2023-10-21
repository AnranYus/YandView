package com.lsp.view.repository.exception

import okhttp3.ResponseBody

class NetworkErrorException(override val message: String?) : LoggerException(message) {
}