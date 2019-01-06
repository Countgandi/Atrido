package com.countgandi.com.engine.renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.countgandi.com.engine.OpenGlUtils;
import com.countgandi.com.engine.renderEngine.models.TexturedModel;
import com.countgandi.com.engine.renderEngine.shaders.StaticShader;
import com.countgandi.com.engine.renderEngine.skybox.SkyboxRenderer;
import com.countgandi.com.engine.renderEngine.terrain.Terrain;
import com.countgandi.com.engine.renderEngine.terrain.TerrainRenderer;
import com.countgandi.com.engine.renderEngine.terrain.TerrainShader;
import com.countgandi.com.game.entities.Camera;
import com.countgandi.com.game.entities.Entity;
import com.countgandi.com.game.entities.Light;

public class MasterRenderer {

	private static final Vector3f skyColor = new Vector3f(0.5444F, 0.62F, 0.69F);// (0.5F, 0.5F, 0.5F);

	private StaticShader shader = new StaticShader();
	private TerrainShader terrainShader = new TerrainShader();

	private EntityRenderer renderer;
	private TerrainRenderer terrainRenderer;

	private Map<TexturedModel, List<Entity>> entities = new HashMap<TexturedModel, List<Entity>>();
	public Map<TexturedModel, List<Entity>> grass = new HashMap<TexturedModel, List<Entity>>();
	private List<Terrain> terrains = new ArrayList<Terrain>();
	private SkyboxRenderer skyboxRenderer;
	private Loader loader;

	public MasterRenderer(Camera camera, Loader loader) {
		this.loader = loader;
		OpenGlUtils.cullBackFaces(true);
		renderer = new EntityRenderer(shader);
		terrainRenderer = new TerrainRenderer(terrainShader);
		skyboxRenderer = new SkyboxRenderer(loader);
		shader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
		terrainShader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
	}

	public void render(List<Light> lights, Camera camera, Vector4f clipPlane) {
		prepare();

		shader.start();
		shader.plane.loadVec4(clipPlane);
		shader.skyColor.loadVec3(skyColor);
		shader.loadLights(lights);
		shader.viewMatrix.loadMatrix(camera.getViewMatrix());
		renderer.render(entities);
		shader.stop();
		
		terrainShader.start();
		terrainShader.plane.loadVec4(clipPlane);
		terrainShader.skyColor.loadVec3(skyColor);
		terrainShader.loadLights(lights);
		terrainShader.viewMatrix.loadMatrix(camera.getViewMatrix());
		terrainRenderer.render(terrains);
		terrainShader.stop();

		skyboxRenderer.render(camera, skyColor);

		terrains.clear();
		entities.clear();
	}

	public void processTerrain(Terrain terrain) {
		terrains.add(terrain);
	}

	public void processEntity(Entity entity) {
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = entities.get(entityModel);
		if (batch != null) {
			batch.add(entity);
		} else {
			List<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch);
		}
	}

	public void cleanUp() {
		shader.cleanUp();
		terrainShader.cleanUp();
	}

	public void prepare() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(skyColor.x, skyColor.y, skyColor.z, 1);
	}

	public Loader getLoader() {
		return loader;
	}

}
