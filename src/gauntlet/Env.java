package gauntlet;

import java.util.HashMap;
import java.util.Map;

class Env {
  private final Map<String,Object> values = new HashMap<>();

  void define(String name, Object value){
      values.put(name,value);
  }

  Object get(Token name){
      if(values.containsKey(name.lexeme())){
          return values.get(name.lexeme());
      }
      throw new RuntimeError(name,"Undefined variable '"+name.lexeme()+"'.");

  }
}
