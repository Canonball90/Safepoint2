package safepoint.two.core.initializers
/*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import safepoint.two.Safepoint
import safepoint.two.SafepointMod
import safepoint.two.core.module.Module
import safepoint.two.core.settings.Setting
import safepoint.two.core.settings.impl.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*


class ConfigManager {
    private val path = File(SafepointMod.MOD_NAME)

    init {
        initModules()
        initFriend()
        initGuis()
    }

    private fun initModules() {
        if (!path.exists()) {
            path.mkdirs()
        }
        for (module in Safepoint.moduleInitializer!!.moduleList) {
            val modulePath = getModulePath(module)
            if (!modulePath.exists()) {
                saveModuleConfig(module)
            } else {
                loadModule(module)
            }
        }
    }

    private fun initFriend() {
        if (!path.exists()) {
            path.mkdirs()
        }
        val friendFile = File(path, "Friends.json")
        if (!friendFile.exists()) {
            friendFile.parentFile.mkdirs()
            friendFile.createNewFile()
        } else {
            val moduleJson = Gson().fromJson(
                String(Files.readAllBytes(friendFile.toPath()), StandardCharsets.UTF_8),
                ArrayList::class.java
            ) ?: return
            moduleJson.forEach { Safepoint.friendInitializer!!.addFriend(it.toString()) }
        }
    }

    private fun initGuis() {
        if (!path.exists()) {
            path.mkdirs()
        }
        val guiFile = File(path, "Guis.json")
        if (!guiFile.exists()) {
            guiFile.parentFile.mkdirs()
            guiFile.createNewFile()
            val guiJson = JsonObject()
            guiJson.add("ClickGui", clickGuiJson())
            guiJson.add("HudEditor", hudEditorJson())
            Files.write(
                guiFile.toPath(), GsonBuilder().setPrettyPrinting().create().toJson(guiJson).toByteArray(
                    StandardCharsets.UTF_8
                )
            )
        } else {
            loadClickGui()
            loadHudEditor()
        }
    }

//    private fun initCommand() {
//        if (!path.exists()) {
//            path.mkdirs()
//        }
//        val commandFile = File(path, "Command.json")
//        if (!commandFile.exists()) {
//            commandFile.parentFile.mkdirs()
//            commandFile.createNewFile()
//            val commandJson = JsonObject()
//            commandJson.addProperty("Prefix", Cube.commandPrefix)
//            Files.write(
//                commandFile.toPath(), GsonBuilder().setPrettyPrinting().create().toJson(commandJson).toByteArray(
//                    StandardCharsets.UTF_8
//                )
//            )
//        } else {
//            val commandJson = Gson().fromJson(
//                String(Files.readAllBytes(commandFile.toPath()), StandardCharsets.UTF_8),
//                JsonObject::class.java
//            ) ?: return
//            Cube.commandPrefix = commandJson.get("Prefix").asString
//        }
//    }

    private fun saveModuleConfig(module: Module) {
        val modulePath = getModulePath(module)
        if (!modulePath.exists()) {
            modulePath.parentFile.mkdirs()
            modulePath.createNewFile()
        }
        modulePath.parentFile.mkdirs()
        modulePath.createNewFile()
        val moduleJson = JsonObject()
        moduleJson.addProperty("Name", module.name)
        moduleJson.addProperty("Toggle", module.toggle)
        if (module.isHud) {
            moduleJson.addProperty("HudXPos", module.x)
            moduleJson.addProperty("HudYPos", module.y)
            moduleJson.addProperty("HudWidth", module.width)
            moduleJson.addProperty("HudHeight", module.height)
        }
        if (module.settings.isNotEmpty()) {
            val settingObject = JsonObject()
            for (setting in module.settings) {
                saveSetting(setting, settingObject)
            }
            moduleJson.add("Settings", settingObject)
        }
//        if (module.commonSettings.isNotEmpty()) {
//            val settingObject = JsonObject()
//            for (setting in module.commonSettings) {
//                saveSetting(setting, settingObject)
//            }
//            moduleJson.add("CommonSettings", settingObject)
//        }
        Files.write(
            modulePath.toPath(), GsonBuilder().setPrettyPrinting().create().toJson(moduleJson).toByteArray(
                StandardCharsets.UTF_8
            )
        )
    }

    private fun loadModule(module: Module) {
        val modulePath = getModulePath(module)
        if (!modulePath.exists()) return
        val moduleJson = Gson().fromJson(
            String(Files.readAllBytes(modulePath.toPath()), StandardCharsets.UTF_8),
            JsonObject::class.java
        ) ?: return
        module.name = moduleJson.get("Name").asString
        val toggle = moduleJson.get("Toggle").asBoolean
        if (module.isEnabled && !toggle) {
            module.disableModule()
        }
        if (!module.isEnabled && toggle) {
            module.enableModule()
        }
        if (module.isHud) {
            module.x = moduleJson.get("HudXPos").asFloat
            module.y = moduleJson.get("HudYPos").asFloat
            module.width = moduleJson.get("HudWidth").asFloat
            module.height = moduleJson.get("HudHeight").asFloat
        }
        val element = moduleJson.get("Settings") ?: return
        val settingsJson = element.asJsonObject
        if (settingsJson != null) {
            for (setting in module.settings) {
                setSetting(setting, settingsJson)
            }
        }
        val commonSettingsJson = moduleJson.get("CommonSettings").asJsonObject
//        if (commonSettingsJson != null) {
//            for (setting in module.commonSettings) {
//                setSetting(setting, commonSettingsJson)
//            }
//        }
    }

    private fun clickGuiJson(): JsonObject {
        val jsonObject = JsonObject()
        for (panel in Cube.clickGui!!.panels) {
            val panelJson = JsonObject()
            panelJson.addProperty("IsShowModules", panel.isShowModules)
            panelJson.addProperty("X", panel.x)
            panelJson.addProperty("Y", panel.y)
            jsonObject.add(panel.category.getName(), panelJson)
        }
        return jsonObject
    }

    private fun hudEditorJson(): JsonObject {
        val jsonObject = JsonObject()
        for (panel in Cube.hudEditor!!.panels) {
            val panelJson = JsonObject()
            panelJson.addProperty("IsShowModules", panel.isShowModules)
            panelJson.addProperty("X", panel.x)
            panelJson.addProperty("Y", panel.y)
            jsonObject.add(panel.category.getName(), panelJson)
        }
        return jsonObject
    }

    private fun loadClickGui() {
        val guiFile = File(path, "Guis.json")
        if (!guiFile.exists()) return
        val json = Gson().fromJson(
            String(Files.readAllBytes(guiFile.toPath()), StandardCharsets.UTF_8),
            JsonObject::class.java
        ) ?: return
        val guiJson = json.get("ClickGui").asJsonObject
        for (panel in Cube.clickGui!!.panels) {
            if (guiJson.has(panel.category.getName())) {
                val panelJson = guiJson.get(panel.category.getName()).asJsonObject
                panel.isShowModules = panelJson.get("IsShowModules").asBoolean
                panel.x = panelJson.get("X").asFloat
                panel.y = panelJson.get("Y").asFloat
            }
        }
    }

    private fun loadHudEditor() {
        val guiFile = File(path, "Guis.json")
        if (!guiFile.exists()) return
        val json = Gson().fromJson(
            String(Files.readAllBytes(guiFile.toPath()), StandardCharsets.UTF_8),
            JsonObject::class.java
        ) ?: return
        val guiJson = json.get("HudEditor").asJsonObject
        for (panel in Safepoint.hudComponentInitializer!!.hudModules) {
            if (guiJson.has(panel.name)) {
                val panelJson = guiJson.get(panel.name).asJsonObject
                panel.renderX = panelJson.get("X").asInt
                panel.renderY = panelJson.get("Y").asInt
            }
        }
    }

    private fun saveAllModules() {
        for (module in Safepoint.moduleInitializer!!.moduleList) {
            saveModuleConfig(module)
        }
    }


    private fun loadAllModules() {
        for (module in Safepoint.moduleInitializer!!.moduleList) {
            loadModule(module)
        }
    }

    private fun saveAllGuis() {
        if (!path.exists()) {
            path.mkdirs()
        }
        val guiFile = File(path, "Guis.json")
        if (!guiFile.exists()) {
            guiFile.parentFile.mkdirs()
            guiFile.createNewFile()
        }
        val guiJson = JsonObject()
        guiJson.add("ClickGui", clickGuiJson())
        guiJson.add("HudEditor", hudEditorJson())
        Files.write(
            guiFile.toPath(), GsonBuilder().setPrettyPrinting().create().toJson(guiJson).toByteArray(
                StandardCharsets.UTF_8
            )
        )
    }

    private fun loadAllGuis() {
        loadClickGui()
        loadHudEditor()
    }

    private fun saveFriends() {
        if (!path.exists()) {
            path.mkdirs()
        }
        val friendFile = File(path, "Friends.json")
        if (!friendFile.exists()) {
            friendFile.parentFile.mkdirs()
            friendFile.createNewFile()
        }
        Files.write(
            friendFile.toPath(), Gson().toJson(Safepoint.friendInitializer!!.getFriendList()).toByteArray(
                StandardCharsets.UTF_8
            )
        )
    }

    private fun loadFriends() {
        val friendFile = File(path, "Friends.json")
        if (!friendFile.exists()) {
            return
        }
        val moduleJson = Gson().fromJson(
            String(Files.readAllBytes(friendFile.toPath()), StandardCharsets.UTF_8),
            ArrayList::class.java
        ) ?: return
        moduleJson.forEach { Safepoint.friendInitializer!!.addFriend(it.toString()) }
    }

//    fun saveCommand() {
//        if (!path.exists()) {
//            path.mkdirs()
//        }
//        val commandFile = File(path, "Command.json")
//        if (!commandFile.exists()) {
//            commandFile.parentFile.mkdirs()
//            commandFile.createNewFile()
//        }
//        val commandJson = JsonObject()
//        commandJson.addProperty("Prefix", Cube.commandPrefix)
//        Files.write(
//            commandFile.toPath(), GsonBuilder().setPrettyPrinting().create().toJson(commandJson).toByteArray(
//                StandardCharsets.UTF_8
//            )
//        )
//    }

//    private fun loadCommand() {
//        val commandFile = File(path, "Command.json")
//        val commandJson = Gson().fromJson(
//            String(Files.readAllBytes(commandFile.toPath()), StandardCharsets.UTF_8),
//            JsonObject::class.java
//        ) ?: return
//        Cube.commandPrefix = commandJson.get("Prefix").asString
//    }

    private fun saveSetting(setting: Setting<*>, jsonObject: JsonObject): JsonObject {
        when (setting) {
            is BooleanSetting -> jsonObject.addProperty(setting.name, setting.value)
            is KeySetting -> jsonObject.addProperty(setting.name, setting.value)
            is DoubleSetting -> jsonObject.addProperty(setting.name, setting.value)
            is FloatSetting -> jsonObject.addProperty(setting.name, setting.value)
            is IntegerSetting -> jsonObject.addProperty(setting.name, setting.value)
            is EnumSetting -> jsonObject.addProperty(setting.name, setting.value)
            is StringSetting -> jsonObject.addProperty(setting.name, setting.value)
            is ColorSetting -> jsonObject.addProperty(setting.name, setting.value.toString())
        }
        return jsonObject
    }

    private fun setSetting(setting: Setting<*>, jsonObject: JsonObject) {
        if (jsonObject.has(setting.name)) {
            when (setting) {
                is BooleanSetting -> setting.value = jsonObject.get(setting.name).asBoolean
                is KeySetting -> setting.value = (jsonObject.get(setting.name).asInt)
                is DoubleSetting -> setting.value = jsonObject.get(setting.name).asDouble
                is FloatSetting -> setting.value = jsonObject.get(setting.name).asFloat
                is IntegerSetting -> setting.value = jsonObject.get(setting.name).asInt
                is StringSetting -> setting.value = jsonObject.get(setting.name).asString
                is EnumSetting -> setting.setValue(jsonObject.get(setting.name).asString)
            }
        }
    }

    private fun getModulePath(module: Module): File {
        return File("$path/modules/${module.category.name}/${module.name}.json")
    }

    fun saveAll() {
        saveAllModules()
        saveAllGuis()
        saveFriends()

    }

    fun loadAll() {
        loadAllModules()
        loadAllGuis()
        loadFriends()
    }
}*/
