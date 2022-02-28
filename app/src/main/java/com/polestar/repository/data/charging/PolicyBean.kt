package com.robolectric




data class PolicyBean(val data: PolicyData)

data class PolicyData(val appChinaPrivacyPolicy: Policy)

class Policy(val content: String, val catelog: String)