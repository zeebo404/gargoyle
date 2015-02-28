package com.zeebo.gargoyle

import com.zeebo.gargoyle.behavior.BehaviorManager
import com.zeebo.gargoyle.behavior.camera.Camera
import com.zeebo.gargoyle.scene.Scene
import org.lwjgl.Sys
import org.lwjgl.opengl.ContextAttribs
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.PixelFormat
import org.lwjgl.util.vector.Vector3f

/**
 * User: Eric
 */
class DisplayController {

	ConfigObject settings
	File propsFile = new File('settings.properties')

	GameDefinition gameDefinition

	Scene currentScene

	DisplayController() {

		loadSettings()

		ContextAttribs contextAttribs = new ContextAttribs(3, 2).withForwardCompatible(true).withProfileCore(true)
		Display.displayMode = new DisplayMode(settings.width, settings.height)
		Display.create(new PixelFormat(), contextAttribs)
	}

	def loop() {

		int fps = 0

		def getTime = { (Sys.time * 1000) / Sys.timerResolution }

		long lastFPS = getTime()

		def updateFps = {
			if (getTime() - lastFPS > 1000) {
				Display.title = "FPS: $fps"
				fps = 0
				lastFPS += 1000
			}
			fps++
		}

		while (!Display.closeRequested) {
			updateFps()
			BehaviorManager.runEvent 'update'
			gameDefinition.renderer.render(currentScene.sceneGraph)
			Display.update()
			Display.sync(60)
		}
	}

	def destroy() {

		saveSettings()

		Display.destroy()
	}

	def loadSettings() {
		Properties props = new Properties()
		props.load(new File('defaults.properties').newReader())
		settings = new ConfigSlurper().parse(props)

		if (propsFile.exists()) {
			props.clear()
			props.load(propsFile.newReader())
			settings.merge new ConfigSlurper().parse(props)
		}

		settings.width = settings.width as int
		settings.height = settings.height as int
	}

	def saveSettings() {
		settings.toProperties().store propsFile.newWriter(), null
	}
}
