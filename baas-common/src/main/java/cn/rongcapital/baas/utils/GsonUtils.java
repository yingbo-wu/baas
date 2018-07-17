package cn.rongcapital.baas.utils;

import com.google.gson.Gson;

/**
 * GSON工具类
 * @author 英博
 *
 */
public class GsonUtils {

	private static Gson exposeGson;

	private static Gson unexposeGson;

	public static Gson createExpose() {
		if (null == exposeGson) {
			exposeGson = GsonBuilderUtils.gsonBuilder(true).create();
		}
		return exposeGson;
	}

	public static Gson create() {
		if (null == unexposeGson) {
			unexposeGson = GsonBuilderUtils.gsonBuilder(false).create();
		}
		return unexposeGson;
	}

}
