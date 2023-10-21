package com.lsp.view.repository.exception

import java.lang.Exception

class UnableConstructObjectException(override val message: String?):LoggerException(message) {
}