package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import io.github.chrislo27.rhre3.playalong.Playalong
import io.github.chrislo27.rhre3.playalong.PlayalongChars
import io.github.chrislo27.rhre3.playalong.PlayalongControls
import io.github.chrislo27.rhre3.playalong.PlayalongInput
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.TrueCheckbox
import io.github.chrislo27.rhre3.util.TempoUtils
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.util.MathHelper
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sign


class PlayalongSettingsScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, PlayalongSettingsScreen>(main) {

    companion object {
        private val FILLED_A_BUTTON = PlayalongChars.FILLED_A
        private val BORDERED_A_BUTTON = PlayalongChars.BORDERED_A

        private val CALIBRATION_MUSIC_OFFSET = 0.126f
        private val CALIBRATION_BPM = 125f
        private val CALIBRATION_DURATION_BEATS = 32f
        private val FIVE_DECIMAL_PLACES_FORMATTER = DecimalFormat("0.00000", DecimalFormatSymbols())
    }

    override val stage: GenericStage<PlayalongSettingsScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val titleLabelIcon: TextLabel<PlayalongSettingsScreen>

    private val controlsLabel: TextLabel<PlayalongSettingsScreen>
    private val pressedControls: EnumSet<PlayalongInput> = EnumSet.noneOf(PlayalongInput::class.java)
    private val helperPressedControls: EnumSet<PlayalongInput> = EnumSet.noneOf(PlayalongInput::class.java)
    private val playStopButton: Button<PlayalongSettingsScreen>

    private val music: Music get() = AssetRegistry["playalong_settings_input_calibration"]
    private val preferences: Preferences get() = main.preferences

    private inner class Calibration(val key: String, var calibration: Float = main.preferences.getFloat(key, 0f),
                                    var summed: Float = 0f, var inputs: Int = 0) {
        fun reset() {
            summed = 0f
            inputs = 0
            calibration = 0f
        }

        fun compute() {
            calibration = summed / inputs.coerceAtLeast(1)
        }

        fun fireInput() {
            val musicPos = music.position - CALIBRATION_MUSIC_OFFSET
            val beat = TempoUtils.secondsToBeats(musicPos, CALIBRATION_BPM) % CALIBRATION_DURATION_BEATS
            val beatOffset = beat - beat.roundToInt()
            val secOffset = TempoUtils.beatsToSeconds(beatOffset, CALIBRATION_BPM)
            if (secOffset.absoluteValue <= Playalong.MAX_OFFSET_SEC * 2) {
                inputs++
                summed += secOffset
                compute()
            }
        }

        fun persist() {
            main.preferences.putFloat(key, calibration).flush()
        }
    }

    private val keyCalibration = Calibration(PreferenceKeys.PLAYALONG_CALIBRATION_KEY)
    private val mouseCalibration = Calibration(PreferenceKeys.PLAYALONG_CALIBRATION_MOUSE)

    init {
        val palette = main.uiPalette
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry["editor"]
        }
        stage.titleLabel.text = "screen.playalongSettings.title"
        stage.titleIcon.visible = false
        titleLabelIcon = TextLabel(palette.copy(ftfont = main.defaultFontLargeFTF), stage, stage).apply {
            this.alignment = Align.topLeft
            this.location.set(this@PlayalongSettingsScreen.stage.titleIcon.location)
            this.text = FILLED_A_BUTTON
            this.isLocalizationKey = false
            this.textWrapping = false
        }
        stage.elements += titleLabelIcon

        val inputCalibrationTitle = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.textWrapping = false
            this.text = "screen.playalongSettings.calibration"
            this.location.set(screenY = 0.875f, screenHeight = 0.1f)
        }
        stage.centreStage.elements += inputCalibrationTitle
        val inputCalibrationControls = object : TextLabel<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun canBeClickedOn(): Boolean = true
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (music.isPlaying && isMouseOver() && button == Input.Buttons.LEFT) {
                    mouseCalibration.fireInput()
                    return true
                }
                return super.touchDown(screenX, screenY, pointer, button)
            }
        }.apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.text = Localization["screen.playalongSettings.calibration.controls", FILLED_A_BUTTON]
            this.location.set(screenY = 0.875f, screenHeight = 0.1f)
            this.visible = false
            this.background = true
        }
        stage.centreStage.elements += inputCalibrationControls
        stage.centreStage.elements += object : TextLabel<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun getRealText(): String {
                return Localization["screen.playalongSettings.calibration.offset.key", FIVE_DECIMAL_PLACES_FORMATTER.format(keyCalibration.calibration.toDouble())]
            }
        }.apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.location.set(screenX = 0f, screenWidth = 0.475f, screenY = 0.775f, screenHeight = 0.1f)
        }
        stage.centreStage.elements += object : TextLabel<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun getRealText(): String {
                return Localization["screen.playalongSettings.calibration.offset.mouse", FIVE_DECIMAL_PLACES_FORMATTER.format(mouseCalibration.calibration.toDouble())]
            }
        }.apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.location.set(screenX = 0.525f, screenWidth = 0.475f, screenY = 0.775f, screenHeight = 0.1f)
        }
        playStopButton = object : Button<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            val playLabel = ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_play"))
                this.tint = Color(0f, 0.5f, 0.055f, 1f)
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            }
            val stopLabel = ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_stop"))
                this.tint = Color(242 / 255f, 0.0525f, 0.0525f, 1f)
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                this.visible = false
            }

            init {
                addLabel(playLabel)
                addLabel(stopLabel)
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                if (!music.isPlaying) {
                    music.play()
                    music.isLooping = true
                    stopLabel.visible = true
                    playLabel.visible = false
                    inputCalibrationTitle.visible = false
                    inputCalibrationControls.visible = true
                } else {
                    music.stop()
                    stopLabel.visible = false
                    playLabel.visible = true
                    inputCalibrationTitle.visible = true
                    inputCalibrationControls.visible = false
                }
            }
        }.apply {
            this.location.set(screenHeight = 0.1f, screenY = 0.65f)
            this.location.set(screenWidth = 0.035f)
            this.location.set(screenX = 0.5f - location.screenWidth / 2)
        }
        stage.centreStage.elements += playStopButton
        stage.centreStage.elements += object : Button<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                keyCalibration.reset()
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.text = "screen.playalongSettings.calibration.reset.key"
                this.textWrapping = false
                this.fontScaleMultiplier = 0.85f
            })
            this.location.set(screenHeight = 0.1f, screenY = 0.65f)
            this.location.set(screenWidth = 0.4f)
            this.location.set(screenX = playStopButton.location.screenX - location.screenWidth - 0.025f)
        }
        stage.centreStage.elements += object : Button<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                mouseCalibration.reset()
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.text = "screen.playalongSettings.calibration.reset.mouse"
                this.textWrapping = false
                this.fontScaleMultiplier = 0.85f
            })
            this.location.set(screenHeight = 0.1f, screenY = 0.65f)
            this.location.set(screenWidth = 0.4f)
            this.location.set(screenX = playStopButton.location.screenX + playStopButton.location.screenWidth + 0.025f)
        }

        stage.centreStage.elements += ColourPane(stage.centreStage, stage.centreStage).apply {
            this.colour.set(1f, 1f, 1f, 1f)
            val barHeight = 0.005f
            this.location.set(screenY = 0.625f - barHeight / 2, screenHeight = barHeight)
        }

        controlsLabel = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.location.set(screenHeight = 0.1f)
        }
        stage.centreStage.elements += controlsLabel

        stage.centreStage.elements += TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.textWrapping = false
            this.text = "screen.playalongSettings.sfxTitle"
            this.location.set(screenX = 0f, screenY = 0.5f, screenWidth = 0.5f, screenHeight = 0.1f)
        }
        stage.centreStage.elements += object : TrueCheckbox<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override val checkLabelPortion: Float = 0.1f
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                preferences.putBoolean(PreferenceKeys.PLAYALONG_SFX_PERFECT_FAIL, checked).flush()
                if (checked) {
                    AssetRegistry.get<Sound>("playalong_sfx_perfect_fail").play(0.5f)
                }
            }
        }.apply {
            this.checked = preferences.getBoolean(PreferenceKeys.PLAYALONG_SFX_PERFECT_FAIL, true)

            this.checkLabel.location.set(screenWidth = checkLabelPortion)
            this.textLabel.location.set(screenX = checkLabelPortion * 1.25f, screenWidth = 1f - checkLabelPortion * 1.25f)

            this.textLabel.apply {
                this.isLocalizationKey = true
                this.fontScaleMultiplier = 0.9f
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "screen.playalongSettings.perfectFailSfx"
            }
            this.location.set(screenX = 0f, screenY = 0.5f - (0.1f + 0.025f), screenWidth = 0.5f, screenHeight = 0.1f)
        }
        stage.centreStage.elements += object : TrueCheckbox<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override val checkLabelPortion: Float = 0.1f
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                preferences.putBoolean(PreferenceKeys.PLAYALONG_SFX_MONSTER_FAIL, checked).flush()
                if (checked) {
                    AssetRegistry.get<Sound>("playalong_sfx_monster_fail").play(0.5f)
                }
            }
        }.apply {
            this.checked = preferences.getBoolean(PreferenceKeys.PLAYALONG_SFX_MONSTER_FAIL, true)

            this.checkLabel.location.set(screenWidth = checkLabelPortion)
            this.textLabel.location.set(screenX = checkLabelPortion * 1.25f, screenWidth = 1f - checkLabelPortion * 1.25f)

            this.textLabel.apply {
                this.isLocalizationKey = true
                this.fontScaleMultiplier = 0.9f
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "screen.playalongSettings.monsterFailSfx"
            }
            this.location.set(screenX = 0f, screenY = 0.5f - (0.1f + 0.025f) * 2, screenWidth = 0.5f, screenHeight = 0.1f)
        }
        stage.centreStage.elements += object : TrueCheckbox<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override val checkLabelPortion: Float = 0.1f
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                preferences.putBoolean(PreferenceKeys.PLAYALONG_SFX_MONSTER_ACE, checked).flush()
                if (checked) {
                    AssetRegistry.get<Sound>("playalong_sfx_monster_ace").play(1f)
                }
            }
        }.apply {
            this.checked = preferences.getBoolean(PreferenceKeys.PLAYALONG_SFX_MONSTER_ACE, true)

            this.checkLabel.location.set(screenWidth = checkLabelPortion)
            this.textLabel.location.set(screenX = checkLabelPortion * 1.25f, screenWidth = 1f - checkLabelPortion * 1.25f)

            this.textLabel.apply {
                this.isLocalizationKey = true
                this.fontScaleMultiplier = 0.9f
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "screen.playalongSettings.monsterAceSfx"
            }
            this.location.set(screenX = 0f, screenY = 0.5f - (0.1f + 0.025f) * 3, screenWidth = 0.5f, screenHeight = 0.1f)
        }

        val currentControls = main.playalongControls.copy()
        val isCustom = currentControls !in PlayalongControls.standardControls.values
        val controlsList = (if (isCustom) listOf(PlayalongControls.strCustom to currentControls) else listOf()) + PlayalongControls.standardControls.entries.map { it.toPair() }
        stage.bottomStage.elements += object : Button<PlayalongSettingsScreen>(palette, stage.bottomStage, stage.bottomStage) {

            var index: Int = controlsList.indexOfFirst { currentControls == it.second }.coerceAtLeast(0)

            fun cycle(dir: Int) {
                var newIndex = index + dir.sign
                if (newIndex < 0) {
                    newIndex = controlsList.size - 1
                } else if (newIndex >= controlsList.size) {
                    newIndex = 0
                }
                index = newIndex
                main.playalongControls = controlsList[index].second
                updateLabel()
            }

            fun updateLabel() {
                val label = labels.first() as TextLabel
                label.text = Localization["screen.playalongSettings.controls", controlsList[index].first]
                updateControlsLabel()
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                cycle(1)
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                cycle(-1)
            }

            init {
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.isLocalizationKey = false
                    this.text = ""
                    this.textWrapping = false
                })
                updateLabel()
            }
        }.apply {
            this.location.set(screenX = 0.15f, screenWidth = 0.7f)
        }
    }

    fun updateControlsLabel() {
        controlsLabel.text = main.playalongControls.toInputString(pressedControls)
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.visible && stage.backButton.enabled) {
            stage.onBackButtonClick()
        }

        if (music.isPlaying) {
            if (Gdx.input.isKeyJustPressed(main.playalongControls.buttonA)) {
                keyCalibration.fireInput()
            }
        }

        val interval = TempoUtils.beatsToSeconds(2f, 125f)
        val pressDuration = 0.15f
        if (MathHelper.getSawtoothWave(interval) < pressDuration / interval && !music.isPlaying) {
            titleLabelIcon.text = BORDERED_A_BUTTON
            titleLabelIcon.fontScaleMultiplier = 0.7f
        } else {
            titleLabelIcon.text = FILLED_A_BUTTON
            titleLabelIcon.fontScaleMultiplier = 0.8f
        }

        val controls = main.playalongControls
        updatePressedControl(controls.buttonA, PlayalongInput.BUTTON_A)
        updatePressedControl(controls.buttonB, PlayalongInput.BUTTON_B)
        updatePressedControl(controls.buttonLeft, PlayalongInput.BUTTON_DPAD_LEFT)
        updatePressedControl(controls.buttonRight, PlayalongInput.BUTTON_DPAD_RIGHT)
        updatePressedControl(controls.buttonDown, PlayalongInput.BUTTON_DPAD_DOWN)
        updatePressedControl(controls.buttonUp, PlayalongInput.BUTTON_DPAD_UP)
        if (helperPressedControls != pressedControls) {
            pressedControls.clear()
            pressedControls.addAll(helperPressedControls)
            updateControlsLabel()
        }
    }

    private fun updatePressedControl(keycode: Int, input: PlayalongInput) {
        if (Gdx.input.isKeyPressed(keycode)) {
            helperPressedControls.add(input)
        } else {
            helperPressedControls.remove(input)
        }
    }

    override fun hide() {
        super.hide()
        music.stop()

        keyCalibration.persist()
        mouseCalibration.persist()

        fun mapSfxSettings(vararg key: String): Map<String, Any> {
            return key.associate { it to preferences.getBoolean(it, true) }
        }
        AnalyticsHandler.track("Exit Playalong Settings", mapOf("keyCalibration" to keyCalibration.calibration, "mouseCalibration" to mouseCalibration.calibration) + mapSfxSettings(PreferenceKeys.PLAYALONG_SFX_PERFECT_FAIL, PreferenceKeys.PLAYALONG_SFX_MONSTER_FAIL, PreferenceKeys.PLAYALONG_SFX_MONSTER_ACE))
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}