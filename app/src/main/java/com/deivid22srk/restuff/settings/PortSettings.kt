/*
 * Configurações REAIS do port Naughty Mobile Bears Community Edition.
 *
 * Cada opção deste modelo é de fato consumida (nada aqui é cenográfico):
 *
 *   fpsLimit          → cvar "fps_cap" do motor (hooks.cpp, cap por software)
 *   vblankHz          → cvar "vblank_hz" (thread de pump do backend Vulkan)
 *   resOption         → env RESTUFF_RES_SCALE (75 = 960x540, 100 = 1280x720)
 *   stickDeadzone/sensitivity/dpadEightWay → VirtualGamepadView
 *   showFpsCounter    → pill mono sobre o jogo lendo os presents Vulkan
 *                       REAIS (JNI nativeGetPresentCount — trampoline no
 *                       vulkan_device.cpp conta cada vkQueuePresentKHR)
 *   unlock60Fps       → flag --fps60 → env RESTUFF_FPS60 (hook de
 *                       PresentationInterval, 30 → 60)
 *   unlockAllCheat    → cvar "unlock_all" (cheats do motor)
 *   detailedLogs      → log_level (toml + REX_LOG_LEVEL) + log persistido
 *                       em /storage/emulated/0/Naughty Mobile Bears Community Edition/logs
 *   showOverlayControls / overlayOpacity / overlayScale / hapticFeedback
 *                     → VirtualGamepadView (gamepad virtual SDL sobre o jogo)
 *   particlesEnabled / reduceMotionOverride → efeitos da tela inicial
 *
 * A ponte Java→motor vive em GameActivity.getArguments() (argv do SDL_main +
 * restuff.toml gerado na hora) — é lá que se confere o que cada campo vira.
 */
package com.deivid22srk.restuff.settings

import android.content.Context

/**
 * Limite de quadros por segundo aplicado como cvar "fps_cap" do motor
 * (0 = ilimitado; vblank_hz continua limitando por hardware).
 *
 * 40/45/50 cobrem os alvos low-end do Community Edition
 * (Helio G99 40-50, G88/G100 30-40).
 */
enum class FpsLimitOption(val label: String, val fps: Int) {
    FPS_30("30", 30),
    FPS_40("40", 40),
    FPS_45("45", 45),
    FPS_50("50", 50),
    FPS_60("60", 60),
    FPS_90("90", 90),
    FPS_120("120", 120),
    UNLIMITED("Unlimited", 0),
}

/**
 * Resolução interna de render (Scene target = guest 1280x720 * pct).
 * P540 (75% = 960x540) é o default do Community Edition dev build:
 * ~56% dos fragmentos do 720p — decisivo em Mali-G52/G57/G68
 * (Helio G88/G99/G100) que são fill-rate bound.
 */
enum class ResOption(val label: String, val pct: Int, val w: Int, val h: Int) {
    P540("540p", 75, 960, 540),
    P720("720p", 100, 1280, 720),
}

/**
 * Presets de dispositivo do playtest (notes.txt, dev build 30/out/2026).
 * Todos em 540p; vblank fica em 120Hz (120 = 60fps com o unlock de
 * PresentationInterval; 60 quebraria o unlock — ver CHANGELOG v1.1.0).
 */
enum class DevicePreset(
    val label: String,
    val fps: FpsLimitOption,
    val vblank: Int,
    val res: ResOption,
    val desc: String,
) {
    SD680("SD 680 · 6GB", FpsLimitOption.FPS_60, 120, ResOption.P540, "50-60 FPS · Mid-Range"),
    G99("Helio G99 · 6+6GB", FpsLimitOption.FPS_45, 120, ResOption.P540, "40-50 FPS · Low-end"),
    G88("Helio G88 · 6GB", FpsLimitOption.FPS_30, 120, ResOption.P540, "30-40 FPS · Low-end"),
    G100("Helio G100 · 6GB", FpsLimitOption.FPS_30, 120, ResOption.P540, "30-40 FPS · Low-end"),
    D6080("Dimensity 6080 · 8GB", FpsLimitOption.FPS_60, 120, ResOption.P540, "Stable 60 · Mid-Range"),
}

/**
 * Snapshot imutável das preferências do port — todas aplicadas ao motor
 * ou ao launcher (ver cabeçalho do arquivo).
 */
data class PortSettings(
    // Desempenho (motor)
    val fpsLimit: FpsLimitOption = FpsLimitOption.FPS_60,
    val vblankHz: Int = 120,                    // 30 .. 240 (cvar vblank_hz)
    // Resolução interna (RESTUFF_RES_SCALE via env — ver GameActivity).
    // 540p default: todos os 5 aparelhos do playtest rodam em 960x540.
    val resOption: ResOption = ResOption.P540,
    // Preset de dispositivo aplicado por último (null = manual).
    val devicePresetName: String? = null,
    // Contador de FPS sobre o jogo: lê o total de vkQueuePresentKHR do
    // dispositivo Vulkan (contado por um trampoline no vulkan_device.cpp)
    // e calcula a taxa no próprio overlay — nada de estimativa por
    // Choreographer (que mede o vsync do painel, não o jogo).
    // Dev build: ligado por padrão (playtest mede FPS alvo por aparelho).
    val showFpsCounter: Boolean = true,
    // Motor ReStuff
    val unlockAllCheat: Boolean = false,        // unlock_all (cheats)
    val unlock60Fps: Boolean = true,            // RESTUFF_FPS60 (unlock vblank)
    // Texture mods do upstream PC (6b269c1): substituição de texturas por
    // content hash em <files>/texture_mods/<hash>.png (dump: tex_dump).
    // Sem root o usuário não edita restuff.toml — o toggle é a via oficial.
    val textureMods: Boolean = false,           // tex_mods (texture packs HD)
    // Controles (overlay do gamepad virtual)
    val showOverlayControls: Boolean = true,
    val overlayOpacity: Float = 0.65f,          // 0.2 .. 1.0
    val overlayScale: Float = 1f,               // 0.7 .. 1.6
    val hapticFeedback: Boolean = true,
    // Afinação dos analógicos + D-pad (Community Edition v1).
    val stickDeadzone: Float = 0.12f,           // 0.0 .. 0.3 (fração do curso)
    val stickSensitivity: Float = 1f,           // 0.5 .. 2.0 (ganho exponencial)
    val dpadEightWay: Boolean = true,           // true = 8 vias, false = 4 vias
    // Dev build: log detalhado desligado por padrão (custo mobile do
    // nível debug); tela de Diagnóstico liga por sessão.
    // (campo detailedLogs segue abaixo)
    // Diagnóstico (log detalhado persistido)
    // ARM PERF: default false — o nível "info" mantém erros/warnings (com
    // stack traces) no log persistido sem pagar o custo mobile do "debug":
    // fmt+mutex+write síncrono por evento em dezenas de sítios do kernel/FS
    // do SDK, rotacionando arquivo no storage público. O toggle continua
    // disponível na tela de Configurações para sessões de diagnóstico.
    val detailedLogs: Boolean = false,         // log_level=info no toml + REX_LOG_LEVEL
    // Efeitos da tela inicial
    val particlesEnabled: Boolean = true,
    val reduceMotionOverride: Boolean = false,
)

/**
 * Persistência simples e sem dependências (SharedPreferences). Chaves
 * estáveis: upgrades de versão mantêm as preferências do usuário.
 */
class PortSettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("port_screen_prefs", Context.MODE_PRIVATE)

    fun load(): PortSettings {
        return PortSettings(
            fpsLimit = enumOf(prefs.getString(K_FPS, null), FpsLimitOption.FPS_60),
            vblankHz = prefs.getInt(K_VBLANK, 120).coerceIn(30, 240),
            resOption = enumOf(prefs.getString(K_RES, null), ResOption.P540),
            devicePresetName = prefs.getString(K_PRESET, null),
            showFpsCounter = prefs.getBoolean(K_SHOW_FPS, true),
            unlockAllCheat = prefs.getBoolean(K_UNLOCK_ALL, false),
            unlock60Fps = prefs.getBoolean(K_UNLOCK_60FPS, true),
            textureMods = prefs.getBoolean(K_TEX_MODS, false),
            showOverlayControls = prefs.getBoolean(K_OVERLAY, true),
            overlayOpacity = prefs.getFloat(K_OPACITY, 0.65f).coerceIn(0.2f, 1f),
            overlayScale = prefs.getFloat(K_PADSCALE, 1f).coerceIn(0.7f, 1.6f),
            hapticFeedback = prefs.getBoolean(K_HAPTIC, true),
            stickDeadzone = prefs.getFloat(K_DEADZONE, 0.12f).coerceIn(0f, 0.3f),
            stickSensitivity = prefs.getFloat(K_SENS, 1f).coerceIn(0.5f, 2f),
            dpadEightWay = prefs.getBoolean(K_DPAD8, true),
            detailedLogs = prefs.getBoolean(K_DETAILED_LOGS, false),
            particlesEnabled = prefs.getBoolean(K_PARTICLES, true),
            reduceMotionOverride = prefs.getBoolean(K_REDUCE_MOTION, false),
        )
    }

    fun save(s: PortSettings) {
        prefs.edit()
            .putString(K_FPS, s.fpsLimit.name)
            .putInt(K_VBLANK, s.vblankHz)
            .putString(K_RES, s.resOption.name)
            .putString(K_PRESET, s.devicePresetName)
            .putBoolean(K_SHOW_FPS, s.showFpsCounter)
            .putBoolean(K_UNLOCK_ALL, s.unlockAllCheat)
            .putBoolean(K_UNLOCK_60FPS, s.unlock60Fps)
            .putBoolean(K_TEX_MODS, s.textureMods)
            .putBoolean(K_OVERLAY, s.showOverlayControls)
            .putFloat(K_OPACITY, s.overlayOpacity)
            .putFloat(K_PADSCALE, s.overlayScale)
            .putBoolean(K_HAPTIC, s.hapticFeedback)
            .putFloat(K_DEADZONE, s.stickDeadzone)
            .putFloat(K_SENS, s.stickSensitivity)
            .putBoolean(K_DPAD8, s.dpadEightWay)
            .putBoolean(K_DETAILED_LOGS, s.detailedLogs)
            .putBoolean(K_PARTICLES, s.particlesEnabled)
            .putBoolean(K_REDUCE_MOTION, s.reduceMotionOverride)
            .apply()
    }

    /** Aplica um preset de aparelho (540p + fps/vblank do alvo). */
    fun applyPreset(s: PortSettings, p: DevicePreset): PortSettings = s.copy(
        fpsLimit = p.fps,
        vblankHz = p.vblank,
        resOption = p.res,
        devicePresetName = p.name,
        textureMods = false,
    )

    private inline fun <reified T : Enum<T>> enumOf(name: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default

    private companion object {
        const val K_FPS = "fps_limit"
        const val K_VBLANK = "restuff_vblank_hz"
        const val K_RES = "restuff_res_option"
        const val K_PRESET = "device_preset"
        const val K_DEADZONE = "stick_deadzone"
        const val K_SENS = "stick_sensitivity"
        const val K_DPAD8 = "dpad_eight_way"
        const val K_SHOW_FPS = "show_fps_counter"
        const val K_UNLOCK_ALL = "restuff_unlock_all"
        const val K_UNLOCK_60FPS = "restuff_unlock_60fps"
        const val K_TEX_MODS = "restuff_tex_mods"
        const val K_OVERLAY = "overlay_controls"
        const val K_OPACITY = "overlay_opacity"
        const val K_PADSCALE = "overlay_pad_scale"
        const val K_HAPTIC = "haptic_feedback"
        const val K_DETAILED_LOGS = "restuff_detailed_logs"
        const val K_PARTICLES = "particles_enabled"     // mesma chave da v1.0
        const val K_REDUCE_MOTION = "reduce_motion"     // mesma chave da v1.0
    }
}
