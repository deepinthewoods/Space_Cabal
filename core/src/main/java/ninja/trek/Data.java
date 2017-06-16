package ninja.trek;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;

public class Data {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static  class IntMapSerializer implements Json.Serializer<IntMap> {
	    @Override
	    public void write(Json json, IntMap object, Class knownType) {
	        json.writeObjectStart();
	        for(IntMap.Entry entry : (IntMap.Entries<?>) object.entries())
	            json.writeValue(String.valueOf(entry.key), entry.value, null);
	        json.writeObjectEnd();
	    }
	 
	    @Override
	    public IntMap read(Json json, JsonValue jsonData, Class type) {
	        IntMap intMap = new IntMap();
	        for(JsonValue entry = jsonData.child; entry != null; entry = entry.next) {
	            intMap.put(Integer.parseInt(entry.name), json.readValue(entry.name, null, jsonData));
	        }
	        return intMap;
	    }
	}

	public static Pool<Json> jsonPool;
	
	static{
		jsonPool = new Pool<Json>(){

			@Override
			protected Json newObject() {
				Json json = new Json();
				json.setSerializer(IntMap.class, new IntMapSerializer());

				return json;
			}
			
		};
	}
}
