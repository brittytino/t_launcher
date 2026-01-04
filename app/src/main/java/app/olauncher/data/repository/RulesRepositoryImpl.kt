package app.olauncher.data.repository

import app.olauncher.domain.model.UsageRule
import app.olauncher.domain.repository.RulesRepository

import app.olauncher.domain.repository.CategoryRepository

class RulesRepositoryImpl(
    private val ruleDao: app.olauncher.data.local.RuleDao,
    private val categoryRepository: CategoryRepository
) : RulesRepository {

    override suspend fun getRulesForPackage(packageName: String): List<UsageRule> {
        val appRules = ruleDao.getRules(packageName).mapNotNull { entity ->
            app.olauncher.data.local.RuleSerializer.deserialize(entity.ruleType, entity.ruleData)
        }
        
        val category = categoryRepository.getCategory(packageName)
        val categoryRules = if (category != null) {
            ruleDao.getRules("CATEGORY_${category.type.name}").mapNotNull { entity ->
                app.olauncher.data.local.RuleSerializer.deserialize(entity.ruleType, entity.ruleData)
            }
        } else {
            emptyList()
        }
        
        return appRules + categoryRules
    }

    override suspend fun addRule(packageName: String, rule: UsageRule) {
        val (type, data) = app.olauncher.data.local.RuleSerializer.serialize(rule)
        ruleDao.insertRule(
            app.olauncher.data.local.RuleEntity(
                packageName = packageName,
                ruleType = type,
                ruleData = data
            )
        )
    }

    override suspend fun removeRule(packageName: String, rule: UsageRule) {
        val (type, _) = app.olauncher.data.local.RuleSerializer.serialize(rule)
        ruleDao.deleteRule(packageName, type)
    }

    override fun getAllRules(): kotlinx.coroutines.flow.Flow<List<app.olauncher.data.local.RuleEntity>> {
        return ruleDao.getAllRules()
    }

    override suspend fun clearRules(packageName: String) {
        ruleDao.clearRules(packageName)
    }
}
