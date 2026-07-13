package net.mine_diver.smoothbeta.client.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mine_diver.smoothbeta.client.render.gl.*;
import net.mine_diver.smoothbeta.util.JsonHelper;
import net.mine_diver.smoothbeta.util.PathUtil;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static net.mine_diver.smoothbeta.SmoothBeta.LOGGER;
import static net.mine_diver.smoothbeta.SmoothBeta.MOD_ID;

@Environment(EnvType.CLIENT)
public class Shader implements GlShader, AutoCloseable {
	private static final String CORE_DIRECTORY = "assets/minecraft/" + MOD_ID + "/shaders/core/";
	private static final String INCLUDE_DIRECTORY = "assets/minecraft/" + MOD_ID + "/shaders/include/";
	private static int activeShaderId;
	private final List<String> samplerNames = new ArrayList<>();
	private final IntList loadedSamplerIds = new IntArrayList();
	private final List<GlUniform> uniforms = new ArrayList<>();
	private final Map<String, GlUniform> loadedUniforms = new HashMap<>();
	private final int programId;
	private final String name;
	private final Program vertexShader;
	private final Program fragmentShader;
	private boolean samplersInitialized;
	public final GlUniform fogMode,
			chunkOffset;

	public Shader(String name, VertexFormat format) throws IOException {
		this.name = name;
		String jsonPath = CORE_DIRECTORY + name + ".json";
		try (InputStream is = getResource(jsonPath);
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			JsonArray jsonArray3;
			JsonArray jsonArray2;
			JsonObject jsonObject = JsonHelper.deserialize(reader);
			String string = JsonHelper.getString(jsonObject, "vertex");
			String string2 = JsonHelper.getString(jsonObject, "fragment");
			JsonArray jsonArray = JsonHelper.getArray(jsonObject, "samplers", null);
			if (jsonArray != null) {
				int i = 0;
				for (JsonElement jsonElement : jsonArray) {
					try {
						this.readSampler(jsonElement);
					} catch (Exception exception) {
						ShaderParseException shaderParseException = ShaderParseException.wrap(exception);
						shaderParseException.addFaultyElement("samplers[" + i + "]");
						throw shaderParseException;
					}
					++i;
				}
			}
			List<String> attributeNames;
			List<Integer> loadedAttributeIds;
			if ((jsonArray2 = JsonHelper.getArray(jsonObject, "attributes", null)) != null) {
				int j = 0;
				loadedAttributeIds = Lists.newArrayListWithCapacity(jsonArray2.size());
				attributeNames = Lists.newArrayListWithCapacity(jsonArray2.size());
				for (JsonElement jsonElement2 : jsonArray2) {
					try {
						attributeNames.add(JsonHelper.asString(jsonElement2, "attribute"));
					} catch (Exception exception2) {
						ShaderParseException shaderParseException2 = ShaderParseException.wrap(exception2);
						shaderParseException2.addFaultyElement("attributes[" + j + "]");
						throw shaderParseException2;
					}
					++j;
				}
			} else {
				loadedAttributeIds = null;
				attributeNames = null;
			}
			if ((jsonArray3 = JsonHelper.getArray(jsonObject, "uniforms", null)) != null) {
				int k = 0;
				for (JsonElement jsonElement3 : jsonArray3) {
					try {
						this.addUniform(jsonElement3);
					} catch (Exception exception3) {
						ShaderParseException shaderParseException3 = ShaderParseException.wrap(exception3);
						shaderParseException3.addFaultyElement("uniforms[" + k + "]");
						throw shaderParseException3;
					}
					++k;
				}
			}
			this.vertexShader = Shader.loadProgram(Program.Type.VERTEX, string);
			this.fragmentShader = Shader.loadProgram(Program.Type.FRAGMENT, string2);
			this.programId = GlProgramManager.createProgram();
			if (attributeNames != null) {
				int k = 0;
				for (String string3 : format.getAttributeNames()) {
					GlUniform.bindAttribLocation(this.programId, k, string3);
					loadedAttributeIds.add(k);
					++k;
				}
			}
			GlProgramManager.linkProgram(this);
			this.loadReferences();
		} catch (Exception exception4) {
			ShaderParseException shaderParseException4 = ShaderParseException.wrap(exception4);
			shaderParseException4.addFaultyFile(jsonPath);
			throw shaderParseException4;
		}
		this.fogMode = this.getUniform("FogMode");
		this.chunkOffset = this.getUniform("ChunkOffset");
	}

	private static InputStream getResource(String path) throws IOException {
		InputStream is = Shader.class.getClassLoader().getResourceAsStream(path);
		if (is == null)
			throw new IOException("Resource not found: " + path);
		return is;
	}

	private static Program loadProgram(Program.Type type, String name) throws IOException {
		Program program2;
		Program program = type.getProgramCache().get(name);
		if (program == null) {
			String string = CORE_DIRECTORY + name + type.getFileExtension();
			try (InputStream inputStream = getResource(string)) {
				final String string2 = PathUtil.getPosixFullPath(string);
				program2 = Program.createFromResource(type, name, inputStream, "classpath", new GLImportProcessor() {
					private final Set<String> visitedImports = Sets.newHashSet();

					@Override
					public String loadImport(boolean inline, String name) {
						String string;
						name = PathUtil.normalizeToPosix((inline ? string2 : Shader.INCLUDE_DIRECTORY) + name);
						if (!this.visitedImports.add(name))
							return null;
						try (InputStream is = getResource(name);
								BufferedReader reader = new BufferedReader(
										new InputStreamReader(is, StandardCharsets.UTF_8))) {
							string = IOUtils.toString(reader);
						} catch (IOException e) {
							LOGGER.error("Could not open GLSL import {}: {}", name, e.getMessage());
							return "#error " + e.getMessage();
						}
						return string;
					}
				});
			}
		} else
			program2 = program;
		return program2;
	}

	public void close() {

		for (GlUniform glUniform : this.uniforms)
			glUniform.close();

		GlProgramManager.deleteProgram(this);
	}

	public void unbind() {
		GlProgramManager.useProgram(0);
		activeShaderId = -1;
	}

	public void bind() {
		if (this.programId != activeShaderId) {
			GlProgramManager.useProgram(this.programId);
			activeShaderId = this.programId;
		}

		if (!this.samplersInitialized) {
			for (int i = 0; i < this.loadedSamplerIds.size(); ++i)
				GlUniform.uniform1(this.loadedSamplerIds.getInt(i), i);
			this.samplersInitialized = true;
		}

		for (GlUniform glUniform : this.uniforms)
			glUniform.upload();

	}

	public GlUniform getUniform(String name) {
		return this.loadedUniforms.get(name);
	}

	private void loadReferences() {
		IntList intList = new IntArrayList();

		int i;
		for (i = 0; i < this.samplerNames.size(); ++i) {
			String string = this.samplerNames.get(i);
			int j = GlUniform.getUniformLocation(this.programId, string);
			if (j == -1) {
				LOGGER.warn("Shader {} could not find sampler named {} in the specified shader program.", this.name,
						string);
				intList.add(i);
			} else
				this.loadedSamplerIds.add(j);
		}

		for (i = intList.size() - 1; i >= 0; --i) {
			int k = intList.getInt(i);
			this.samplerNames.remove(k);
		}

		for (GlUniform glUniform : this.uniforms) {
			String string2 = glUniform.getName();
			int l = GlUniform.getUniformLocation(this.programId, string2);
			if (l == -1)
				LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", this.name,
						string2);
			else {
				glUniform.setLocation(l);
				this.loadedUniforms.put(string2, glUniform);
			}
		}

	}

	private void readSampler(JsonElement json) {
		JsonObject jsonObject = JsonHelper.asObject(json, "sampler");
		String string = JsonHelper.getString(jsonObject, "name");
		this.samplerNames.add(string);
	}

	private void addUniform(JsonElement json) throws ShaderParseException {
		JsonObject jsonObject = JsonHelper.asObject(json, "uniform");
		String string = JsonHelper.getString(jsonObject, "name");
		int i = GlUniform.getTypeIndex(JsonHelper.getString(jsonObject, "type"));
		int j = JsonHelper.getInt(jsonObject, "count");
		float[] fs = new float[Math.max(j, 16)];
		JsonArray jsonArray = JsonHelper.getArray(jsonObject, "values");
		if (jsonArray.size() != j && jsonArray.size() > 1)
			throw new ShaderParseException(
					"Invalid amount of values specified (expected " + j + ", found " + jsonArray.size() + ")");
		else {
			int k = 0;

			for (Iterator<JsonElement> var9 = jsonArray.iterator(); var9.hasNext(); ++k) {
				JsonElement jsonElement = var9.next();

				try {
					fs[k] = JsonHelper.asFloat(jsonElement, "value");
				} catch (Exception var13) {
					ShaderParseException shaderParseException = ShaderParseException.wrap(var13);
					shaderParseException.addFaultyElement("values[" + k + "]");
					throw shaderParseException;
				}
			}

			if (j > 1 && jsonArray.size() == 1)
				while (k < j) {
					fs[k] = fs[0];
					++k;
				}

			int l = j > 1 && j <= 4 && i < 8 ? j - 1 : 0;
			GlUniform glUniform = new GlUniform(string, i + l, j);
			if (i <= 3)
				glUniform.setForDataType((int) fs[0], (int) fs[1], (int) fs[2], (int) fs[3]);
			else if (i <= 7)
				glUniform.setForDataType(fs[0], fs[1], fs[2], fs[3]);
			else
				glUniform.set(Arrays.copyOfRange(fs, 0, j));

			this.uniforms.add(glUniform);
		}
	}

	public Program getVertexShader() {
		return this.vertexShader;
	}

	public Program getFragmentShader() {
		return this.fragmentShader;
	}

	public void attachReferencedShaders() {
		this.fragmentShader.attachTo(this);
		this.vertexShader.attachTo(this);
	}

	public int getProgramRef() {
		return this.programId;
	}
}
