package com.nonoka.nhentai.feature.bypass_security

enum class ByPassingResult(val label: String) {
    Loading("Loading"),
    Processing("Processing"),
    Success("Success"),
    Failure("Failure"),
    NeedByPassing("NeedByPassing"),
}