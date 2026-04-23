package com.smsbridge.util

import com.smsbridge.data.db.entity.FilterField
import com.smsbridge.data.db.entity.FilterRuleEntity
import com.smsbridge.data.db.entity.MatchType
import com.smsbridge.data.db.entity.WebhookEntity

/**
 * Evaluates whether an incoming SMS should be forwarded to a webhook.
 *
 * If a webhook has NO filter rules → always forward (catch-all).
 * filterOperator AND → SMS must satisfy every rule.
 * filterOperator OR  → SMS must satisfy at least one rule.
 */
object FilterMatcher {

    fun matches(
        webhook: WebhookEntity,
        rules: List<FilterRuleEntity>,
        sender: String,
        body: String
    ): Boolean {
        if (rules.isEmpty()) return true

        val results = rules.map { rule ->
            val target = when (rule.field) {
                FilterField.SENDER -> sender
                FilterField.BODY -> body
                else -> ""
            }
            val matched = evaluateRule(rule, target)
            if (rule.negate) !matched else matched
        }

        return when (webhook.filterOperator) {
            "OR" -> results.any { it }
            else -> results.all { it }  // AND is default
        }
    }

    private fun evaluateRule(rule: FilterRuleEntity, target: String): Boolean {
        val value = rule.value
        return when (rule.matchType) {
            MatchType.CONTAINS -> target.contains(value, ignoreCase = true)
            MatchType.EXACT -> target.equals(value, ignoreCase = true)
            MatchType.STARTS_WITH -> target.startsWith(value, ignoreCase = true)
            MatchType.ENDS_WITH -> target.endsWith(value, ignoreCase = true)
            MatchType.REGEX -> try {
                Regex(value, RegexOption.IGNORE_CASE).containsMatchIn(target)
            } catch (e: Exception) {
                false
            }
            else -> false
        }
    }
}
