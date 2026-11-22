package pt.mesasemana.app;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.*;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MealFlowTest {
 @Test public void pantryAliasesCutsAndWeeklyReservations(){
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   scenario.onActivity(a->{
    assertEquals(a.canonical("bifes de frango"),a.canonical("peito de frango"));
    assertEquals(a.canonical("frango partigo"),a.canonical("frango em pedaços"));
    assertNotEquals(a.canonical("lombo cachaço sem osso"),a.canonical("lombo de porco"));
    assertNotEquals(a.canonical("frango partido"),a.canonical("peito de frango"));
    a.put(a.prefs(),"allowShopping",false);a.put(a.state,"pantry",new JSONArray());a.put(a.state,"favorites",new JSONArray());a.put(a.state,"plan",new JSONArray());
    a.put(a.prefs(),"people",4);a.put(a.prefs(),"minutes",45);a.put(a.prefs(),"fish",0);a.put(a.prefs(),"meat",7);
    JSONObject stock=new JSONObject();a.put(stock,"name","bifes de frango");a.put(stock,"qty",700);a.put(stock,"unit","g");a.put(stock,"category","Outros");a.arr("pantry").put(stock);
    a.generate();int supported=0;
    for(int i=0;i<a.arr("plan").length();i++){JSONObject r=a.arr("plan").optJSONObject(i);if(r.optBoolean("mainFromPantry")){supported++;assertEquals("peito de frango",a.canonical(r.optJSONArray("ingredients").optJSONObject(0).optString("name")));}}
    assertEquals(1,supported);assertEquals(1,a.arr("plan").length());assertEquals(700,a.available(stock),0.001);
    a.put(a.prefs(),"fish",3);a.put(a.prefs(),"meat",3);a.put(a.state,"pantry",new JSONArray());a.save();
   });
  }
 }
 @Test public void weeklyDistributionScalingAndStockNeverNegative(){
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   scenario.onActivity(a->{
    a.put(a.prefs(),"allowShopping",true);a.put(a.state,"plan",new JSONArray());a.put(a.state,"pantry",new JSONArray());
    a.put(a.prefs(),"people",4);a.put(a.prefs(),"minutes",45);a.put(a.prefs(),"fish",3);a.put(a.prefs(),"meat",3);
    a.generate();assertEquals(7,a.arr("plan").length());
    Map<String,Integer> count=new HashMap<>();Set<String> ids=new HashSet<>();
    for(int i=0;i<7;i++){JSONObject r=a.arr("plan").optJSONObject(i);String type=r.optString("type");count.put(type,count.getOrDefault(type,0)+1);assertTrue(ids.add(r.optString("id")));assertTrue(r.optInt("minutes")<=45);}
    assertFalse(count.containsKey("vegetariana"));assertTrue(count.getOrDefault("carne",0)+count.getOrDefault("peixe",0)==7);
    JSONObject recipe=a.arr("plan").optJSONObject(0), ingredient=recipe.optJSONArray("ingredients").optJSONObject(0);
    double base=a.amount(recipe,ingredient);a.put(a.prefs(),"people",6);assertEquals(base*1.5,a.amount(recipe,ingredient),0.0001);
    a.arr("pantry").put(copy(ingredient));a.put(a.arr("pantry").optJSONObject(0),"qty",1);
    a.consume(recipe);assertEquals(0,a.available(ingredient),0.0001);
    JSONObject cleaned=a.cleanRecipe(recipe);a.put(cleaned,"cooked",true);assertFalse(a.cleanRecipe(cleaned).has("cooked"));
   });
  }
 }
 @Test public void impossibleMenuPreservesExistingPlanAndDataSurvivesRecreation(){
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   scenario.onActivity(a->{a.put(a.prefs(),"allowShopping",true);a.put(a.prefs(),"minutes",45);a.generate();String existing=a.arr("plan").toString();a.put(a.prefs(),"minutes",10);a.generate();assertEquals(existing,a.arr("plan").toString());a.put(a.prefs(),"minutes",45);a.put(a.prefs(),"people",5);a.save();});
   scenario.recreate();
   scenario.onActivity(a->{assertEquals(5,a.prefs().optInt("people"));assertEquals(7,a.arr("plan").length());});
  }
 }

 @Test public void manualRecipePersistsScalesAndFishGetsPriorityWithoutVegetarianMeals(){
  final String[] id={null};
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   scenario.onActivity(a->{
    try{
     a.put(a.state,"pantry",new JSONArray());a.put(a.state,"favorites",new JSONArray());a.put(a.state,"plan",new JSONArray());a.put(a.prefs(),"people",4);a.put(a.prefs(),"minutes",45);a.put(a.prefs(),"allowShopping",false);
     JSONObject main=a.manualIngredient("bifes de frango",700,"g","Talho");JSONArray extra=new JSONArray();extra.put(a.manualIngredient("natas",200,"ml","Laticínios"));extra.put(a.manualIngredient("cogumelos",250,"g","Hortofrutícolas"));
     JSONObject recipe=a.buildManualRecipe("Bifinhos com natas e cogumelos","carne",4,30,main,extra,"Aloura os bifes.\nJunta os cogumelos e as natas e termina a cozedura.");id[0]=recipe.getString("id");a.storeManualRecipe(recipe);
     assertEquals(3,recipe.getJSONArray("ingredients").length());assertEquals(2,recipe.getJSONArray("steps").length());
     a.put(a.prefs(),"people",6);assertEquals(1050,a.amount(recipe,main),0.001);a.put(a.prefs(),"people",4);
     JSONObject fish=a.manualIngredient("salmão",600,"g","Peixaria");a.arr("pantry").put(fish);a.arr("pantry").put(main);
     a.generate();assertEquals("peixe",a.arr("plan").getJSONObject(0).getString("type"));
     for(int i=0;i<a.arr("plan").length();i++)assertNotEquals("vegetariana",a.arr("plan").getJSONObject(i).getString("type"));
     a.save();
     int before=a.arr("favorites").length();try{a.buildManualRecipe("Inválida","carne",4,30,main,extra,"");fail("Passos vazios aceites");}catch(Exception expected){}assertEquals(before,a.arr("favorites").length());
    }catch(Exception e){throw new AssertionError(e);}
   });
   scenario.recreate();scenario.onActivity(a->{boolean found=false;for(int i=0;i<a.arr("favorites").length();i++)if(id[0].equals(a.arr("favorites").optJSONObject(i).optString("id")))found=true;assertTrue(found);});
  }
 }
 private static JSONObject copy(JSONObject source){try{return new JSONObject(source.toString());}catch(Exception e){throw new AssertionError(e);}}
}
