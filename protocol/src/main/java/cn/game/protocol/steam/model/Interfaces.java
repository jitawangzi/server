/**
  * Copyright 2022 json.cn 
  */
package cn.game.protocol.steam.model;
import java.util.List;

/**
 * Auto-generated: 2022-09-29 17:23:5
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class Interfaces {

    private String name;
    private List<Methods> methods;
    public void setName(String name) {
         this.name = name;
     }
     public String getName() {
         return name;
     }

    public void setMethods(List<Methods> methods) {
         this.methods = methods;
     }
     public List<Methods> getMethods() {
         return methods;
     }

}