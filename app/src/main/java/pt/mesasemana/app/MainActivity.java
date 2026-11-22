package pt.mesasemana.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.regex.*;
import android.graphics.BitmapFactory;

/** Native, local-first Android application. No account, tracking or paid services. */
public class MainActivity extends Activity {
 JSONObject state;
 JSONArray catalogue;
 LinearLayout content;
 int page=0;
 final int GREEN=Color.rgb(18,61,50), CREAM=Color.rgb(248,243,231);
 final ExecutorService network=Executors.newFixedThreadPool(2);
 final String[] tabs={"Despensa","Ementa","Trends","Compras","Guardadas"};
 JSONArray arr(String key){return state.optJSONArray(key);}
 JSONObject prefs(){return state.optJSONObject("preferences");}
 void put(JSONObject o,String k,Object v){try{o.put(k,v);}catch(Exception e){throw new IllegalStateException(e);}}
 void putAt(JSONArray a,int i,Object v){try{a.put(i,v);}catch(Exception e){throw new IllegalStateException(e);}}
 @Override public void onCreate(Bundle b){
  super.onCreate(b);
  try{
   catalogue=new JSONArray(read(getAssets().open("recipes.json"),2000000));
   String saved=getPreferences(0).getString("state",null);
   state=saved==null?new JSONObject("{\"schema\":1,\"pantry\":[],\"plan\":[],\"favorites\":[],\"preferences\":{\"people\":4,\"minutes\":45,\"fish\":3,\"meat\":3}}"):new JSONObject(saved);
   validate(state); show();
  }catch(Exception e){new AlertDialog.Builder(this).setMessage("Não foi possível abrir os dados. Os dados existentes foram preservados.").setPositiveButton("Fechar",(d,w)->finish()).show();}
 }
 void save(){if(!getPreferences(0).edit().putString("state",state.toString()).commit())toast("Não foi possível gravar no dispositivo. Exporta antes de fechar.");}
 JSONObject cleanRecipe(JSONObject r){try{JSONObject copy=new JSONObject(r.toString());copy.remove("cooked");return copy;}catch(Exception e){throw new IllegalStateException(e);}}
 String read(InputStream in,int max)throws Exception{
  try(InputStream source=in; ByteArrayOutputStream out=new ByteArrayOutputStream()){
   byte[] b=new byte[8192];int n;
   while((n=source.read(b))!=-1){if(out.size()+n>max)throw new IOException("Ficheiro demasiado grande");out.write(b,0,n);}
   return out.toString("UTF-8");
  }
 }
 void validate(JSONObject s)throws Exception{
  if(s.getInt("schema")!=1)throw new Exception("Versão incompatível");
  JSONObject p=s.getJSONObject("preferences");
  if(p.getInt("people")<1||p.getInt("people")>20||p.getInt("minutes")<10||p.getInt("minutes")>180||p.getInt("fish")<0||p.getInt("meat")<0||p.getInt("fish")+p.getInt("meat")>7)throw new Exception("Preferências inválidas");
  for(String k:new String[]{"pantry","plan","favorites"})if(s.getJSONArray(k).length()>1000)throw new Exception("Limite excedido");
  if(s.getJSONArray("plan").length()>7)throw new Exception("Ementa inválida");
  for(int i=0;i<s.getJSONArray("pantry").length();i++){
   JSONObject x=s.getJSONArray("pantry").getJSONObject(i);
   if(x.getString("name").trim().isEmpty()||!Double.isFinite(x.getDouble("qty"))||x.getDouble("qty")<0||!Arrays.asList("g","ml","un").contains(x.getString("unit")))throw new Exception("Ingrediente inválido");
   x.getString("category");
  }
  for(String k:new String[]{"plan","favorites"})for(int i=0;i<s.getJSONArray(k).length();i++){
   JSONObject r=s.getJSONArray(k).getJSONObject(i);r.getString("title");r.getString("id");r.getString("type");
   if(r.getDouble("servings")<=0||r.getInt("minutes")<1)throw new Exception("Receita inválida");
   JSONArray items=r.getJSONArray("ingredients");r.getJSONArray("steps");
   for(int j=0;j<items.length();j++){JSONObject x=items.getJSONObject(j);x.getString("name");x.getString("category");if(x.getDouble("qty")<=0||!Arrays.asList("g","ml","un").contains(x.getString("unit")))throw new Exception("Quantidade inválida");}
  }
 }
 int dp(float value){return Math.round(value*getResources().getDisplayMetrics().density);}
 Typeface face(){try{return Typeface.createFromAsset(getAssets(),"manrope.ttf");}catch(Exception e){return Typeface.create("sans-serif",Typeface.NORMAL);}}
 GradientDrawable shape(int color,int radius){GradientDrawable b=new GradientDrawable();b.setColor(color);b.setCornerRadius(dp(radius));return b;}
 TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTypeface(face());t.setTextColor(GREEN);t.setPadding(0,dp(6),0,dp(6));t.setLineSpacing(dp(3),1);return t;}
 void label(String s,int size){TextView t=text(s,size);if(size>=20)t.setTypeface(face(),Typeface.BOLD);content.addView(t);}
 void button(String s,Runnable action){Button b=new Button(this);b.setText(s);b.setTypeface(face(),Typeface.BOLD);b.setTextSize(15);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setBackground(shape(GREEN,18));b.setMinHeight(dp(54));b.setPadding(dp(14),dp(12),dp(14),dp(12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(10),0,dp(10));content.addView(b,lp);b.setOnClickListener(v->action.run());}
 void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 LinearLayout box(){LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);b.setPadding(dp(18),dp(16),dp(18),dp(16));GradientDrawable bg=shape(Color.WHITE,22);bg.setStroke(dp(1),Color.rgb(220,225,215));b.setBackground(bg);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(8),0,dp(12));content.addView(b,lp);return b;}
 void hero(){
  android.widget.FrameLayout hero=new android.widget.FrameLayout(this);hero.setBackgroundColor(GREEN);content.addView(hero,new LinearLayout.LayoutParams(-1,dp(280)));
  ImageView photo=new ImageView(this);photo.setScaleType(ImageView.ScaleType.CENTER_CROP);photo.setContentDescription("Inspiração gastronómica");hero.addView(photo,new android.widget.FrameLayout.LayoutParams(-1,-1));
  try{photo.setImageDrawable(android.graphics.drawable.Drawable.createFromStream(getAssets().open("hero.jpg"),null));}catch(Exception ignored){}
  View shade=new View(this);GradientDrawable gradient=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0xf011382d,0xbb11382d,0x3311382d});shade.setBackground(gradient);hero.addView(shade,new android.widget.FrameLayout.LayoutParams(-1,-1));
  LinearLayout copy=new LinearLayout(this);copy.setOrientation(LinearLayout.VERTICAL);copy.setPadding(dp(22),dp(22),dp(22),dp(20));hero.addView(copy,new android.widget.FrameLayout.LayoutParams(-1,-1));
  TextView name=text("✳   MESA DA SEMANA",12);name.setTextColor(0xffe3e7b5);name.setTypeface(face(),Typeface.BOLD);copy.addView(name);
  TextView title=text("O que há para\njantar?",37);title.setTypeface(Typeface.create("serif",Typeface.NORMAL));title.setTextColor(Color.WHITE);copy.addView(title);
  TextView subtitle=text("A tua cozinha, novas ideias todos os dias. Planeia jantares e aproveita os ingredientes.",15);subtitle.setTextColor(0xfff3f1e6);copy.addView(subtitle);
 }
 void dashboard(){LinearLayout row=new LinearLayout(this);int done=0;for(int i=0;i<arr("plan").length();i++)if(arr("plan").optJSONObject(i).optBoolean("cooked"))done++;String[] numbers={""+arr("pantry").length(),done+" / 7",""+arr("favorites").length()};String[] captions={"Ingredientes\nem casa","Jantares\nfeitos","Receitas\nguardadas"};for(int i=0;i<3;i++){LinearLayout stat=new LinearLayout(this);stat.setOrientation(LinearLayout.VERTICAL);stat.setPadding(dp(12),dp(10),dp(8),dp(10));stat.setBackground(shape(i==0?GREEN:Color.WHITE,18));TextView n=text(numbers[i],25),caption=text(captions[i],11);n.setTypeface(face(),Typeface.BOLD);if(i==0){n.setTextColor(0xffe6edcd);caption.setTextColor(0xffe6edcd);}stat.addView(n);stat.addView(caption);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);lp.setMargins(dp(3),dp(16),dp(3),dp(16));row.addView(stat,lp);}content.addView(row);}
 void show(){
  getWindow().setStatusBarColor(GREEN);getWindow().setNavigationBarColor(CREAM);
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(CREAM);root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(0,insets.getSystemWindowInsetTop(),0,insets.getSystemWindowInsetBottom());return insets;});
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(16),0,dp(16),dp(24));scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  hero();dashboard();
  LinearLayout heading=new LinearLayout(this);TextView title=text(tabs[page],27);title.setTypeface(face(),Typeface.BOLD);heading.addView(title,new LinearLayout.LayoutParams(0,-2,1));TextView settings=text("⚙",25);settings.setContentDescription("Preferências e cópia de segurança");settings.setPadding(dp(16),dp(6),dp(16),dp(6));settings.setOnClickListener(v->settings());heading.addView(settings);content.addView(heading);
  LinearLayout nav=new LinearLayout(this);nav.setBackgroundColor(Color.WHITE);nav.setPadding(dp(6),dp(8),dp(6),dp(8));String[] icons={"▤","▦","✧","☑","♧"};
  for(int i=0;i<5;i++){final int index=i;LinearLayout item=new LinearLayout(this);item.setOrientation(LinearLayout.VERTICAL);item.setGravity(Gravity.CENTER);item.setMinimumHeight(dp(66));if(i==page)item.setBackground(shape(0xffe7efcf,16));TextView icon=text(icons[i],24),caption=text(tabs[i],11);icon.setGravity(Gravity.CENTER);caption.setGravity(Gravity.CENTER);caption.setTypeface(face(),i==page?Typeface.BOLD:Typeface.NORMAL);item.addView(icon);item.addView(caption);nav.addView(item,new LinearLayout.LayoutParams(0,-2,1));item.setOnClickListener(v->{page=index;show();});}
  root.addView(nav);setContentView(root);
  if(page==0)pantry();if(page==1)plan();if(page==2)trends();if(page==3)shopping();if(page==4)favorites();
 }
 EditText input(LinearLayout parent,String hint,String value){EditText e=new EditText(this);e.setTypeface(face());e.setHint(hint);e.setText(value);e.setSingleLine();parent.addView(e);return e;}
 LinearLayout form(){LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);f.setPadding(24,8,24,8);return f;}
 void pantry(){
  label("O que tens em casa?",22);label("Adiciona os ingredientes e quantidades para planear os jantares da tua família.",15);
  button("+ Adicionar ingrediente",()->ingredient(-1));
  for(int i=0;i<arr("pantry").length();i++){final int n=i;JSONObject x=arr("pantry").optJSONObject(i);LinearLayout b=box();b.addView(text(x.optString("name")+" · "+number(x.optDouble("qty"))+" "+x.optString("unit"),18));b.addView(text(x.optString("category")+"  ·  Tocar para editar",13));b.setOnClickListener(v->ingredient(n));}
 }
 void ingredient(int index){
  JSONObject old=index<0?new JSONObject():arr("pantry").optJSONObject(index);
  LinearLayout f=form();EditText name=input(f,"Ingrediente",old.optString("name")),qty=input(f,"Quantidade",old.has("qty")?number(old.optDouble("qty")):""),unit=input(f,"Unidade: g, ml ou un",old.optString("unit","g")),cat=input(f,"Categoria de supermercado",old.optString("category","Outros"));
  new AlertDialog.Builder(this).setTitle("Despensa").setView(f).setPositiveButton("Guardar",(d,w)->{
   try{double q=Double.parseDouble(qty.getText().toString().replace(',','.'));String u=unit.getText().toString().trim();if(name.getText().toString().trim().isEmpty()||!Double.isFinite(q)||q<=0||!Arrays.asList("g","ml","un").contains(u))throw new Exception();
    JSONObject x=new JSONObject();put(x,"name",name.getText().toString().trim());put(x,"qty",q);put(x,"unit",u);put(x,"category",cat.getText().toString());if(index<0)arr("pantry").put(x);else arr("pantry").put(index,x);save();show();
   }catch(Exception e){toast("Usa um nome, quantidade positiva e unidade g, ml ou un.");}
  }).setNegativeButton("Cancelar",null).setNeutralButton(index<0?"":"Eliminar",(d,w)->{if(index>=0){arr("pantry").remove(index);save();show();}}).show();
 }
 String canonical(String value){
  String n=java.text.Normalizer.normalize(value.toLowerCase(new Locale("pt","PT")),java.text.Normalizer.Form.NFD).replaceAll("\\p{M}","").replaceAll("[^a-z0-9 ]"," ").replaceAll(" +"," ").trim();
  if(n.equals("bifes de frango")||n.equals("bife de frango")||n.equals("peito de frango")||n.equals("peitos de frango"))return "peito de frango";
  if(n.equals("frango partido")||n.equals("frango partigo")||n.equals("frango em pedacos"))return "frango em pedacos";
  if(n.equals("costeletas do cachaco")||n.equals("costeletas de cachaco")||n.equals("costeletas de porco"))return "costeletas de porco";
  if(n.equals("lombo cachaco sem osso")||n.equals("cachaco sem osso")||n.equals("cachaco de porco"))return "cachaco de porco";
  if(n.equals("grao de bico cozido")||n.equals("grao cozido"))return "grao cozido";
  return n;
 }
 String key(JSONObject x){return canonical(x.optString("name"))+"|"+x.optString("unit");}
 double available(JSONObject x){double q=0;for(int i=0;i<arr("pantry").length();i++){JSONObject p=arr("pantry").optJSONObject(i);if(key(p).equals(key(x)))q+=p.optDouble("qty");}return q;}
 double mainCoverage(JSONObject r){JSONObject x=r.optJSONArray("ingredients").optJSONObject(0);return Math.min(1,available(x)/amount(r,x));}
 double amount(JSONObject r,JSONObject x){return x.optDouble("qty")*prefs().optInt("people")/r.optDouble("servings",4);}
 double score(JSONObject r){double coverage=mainCoverage(r);double score=(coverage>=0.999?10000:coverage>0?1000:0)+coverage*100;JSONArray ingredients=r.optJSONArray("ingredients");for(int i=1;i<ingredients.length();i++){JSONObject x=ingredients.optJSONObject(i);score+=Math.min(1,available(x)/amount(r,x));}return score+(coverage>=0.999&&"peixe".equals(r.optString("type"))?50:0)+("carne".equals(r.optString("type"))?0.1:0)+Math.random()*0.01;}
 JSONObject choose(String type,Set<String> used){
  JSONObject best=null;double value=-1;
  JSONArray all=new JSONArray();for(int i=0;i<catalogue.length();i++)all.put(catalogue.optJSONObject(i));for(int i=0;i<arr("favorites").length();i++)all.put(arr("favorites").optJSONObject(i));
  for(int i=0;i<all.length();i++){JSONObject r=all.optJSONObject(i);if((!prefs().optBoolean("allowShopping",false)&&mainCoverage(r)<0.999)||(!"carne".equals(r.optString("type"))&&!"peixe".equals(r.optString("type")))||(!"qualquer".equals(type)&&!type.equals(r.optString("type")))||r.optInt("minutes")>prefs().optInt("minutes")||used.contains(r.optString("id")))continue;double s=score(r);if(s>value){best=r;value=s;}}
  return best==null?null:cleanRecipe(best);
 }
 void generate(){
  JSONArray next=new JSONArray();Set<String> used=new HashSet<>();List<String> types=new ArrayList<>();
  for(int i=0;i<7;i++)types.add("qualquer");
  JSONArray actual=arr("pantry");
  try{
   put(state,"pantry",new JSONArray(actual.toString()));
   for(String type:types){
    JSONObject r=choose(type,used);
    if(r==null&&!prefs().optBoolean("allowShopping",false)){
     double best=-1;for(String alternative:new String[]{"carne","peixe"}){JSONObject option=choose(alternative,used);if(option!=null&&score(option)>best){r=option;best=score(option);}}
    }
    if(r==null){if(prefs().optBoolean("allowShopping",false)){toast("Faltam receitas distintas dentro do tempo escolhido. A ementa anterior foi mantida.");return;}break;}
    put(r,"mainFromPantry",mainCoverage(r)>=0.999);used.add(r.optString("id"));next.put(r);consume(r);
   }
  }catch(Exception e){toast("Não foi possível gerar a ementa. Dados preservados.");return;}
  finally{put(state,"pantry",actual);}
  put(state,"plan",next);save();show();if(next.length()<7)toast("Encontradas "+next.length()+" refeições compatíveis com os principais e quantidades da despensa. Não foram acrescentados pratos para comprar.");
 }
 void plan(){
  label(prefs().optInt("people")+" pessoas · até "+prefs().optInt("minutes")+" min · carne e peixe, sem quotas semanais",14);
  label("Modo despensa: só entram pratos com o ingrediente principal suficiente. Só carne e peixe, com prioridade ao peixe disponível. Acompanhamentos e temperos em falta continuam na lista de compras.",14);
  button("Gerar com o que tenho",()->{put(prefs(),"allowShopping",false);if(arr("plan").length()==0)generate();else new AlertDialog.Builder(this).setMessage("Substituir a ementa em curso?").setPositiveButton("Gerar",(d,w)->generate()).setNegativeButton("Cancelar",null).show();});
  button("Completar sete jantares com compras",()->new AlertDialog.Builder(this).setMessage("Este modo pode incluir peixe ou carne que não tens. Os ingredientes em falta entram nas compras. Continuar?").setPositiveButton("Incluir compras",(d,w)->{put(prefs(),"allowShopping",true);generate();}).setNegativeButton("Cancelar",null).show());
  label(arr("plan").length()+" / 7 jantares planeados",16);
  button("Aprender a cozinhar peixe",()->fish());
  String[] days={"Segunda","Terça","Quarta","Quinta","Sexta","Sábado","Domingo"};
  for(int i=0;i<arr("plan").length();i++){final int n=i;JSONObject r=arr("plan").optJSONObject(i);card(r,days[i]+(r.optBoolean("cooked")?" · Preparada":""),()->recipe(r,n));}
  if(arr("plan").length()==0)label("Começa pela despensa e gera a tua primeira semana.",17);
 }
 void card(JSONObject r,String eyebrow,Runnable click){LinearLayout b=box();String image=r.optString("image");if(image.startsWith("https://")){ImageView photo=new ImageView(this);photo.setScaleType(ImageView.ScaleType.CENTER_CROP);photo.setContentDescription(r.optString("title"));b.addView(photo,new LinearLayout.LayoutParams(-1,180));network.execute(()->{try{byte[] bytes=fetch(image,5000000);android.graphics.Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length);runOnUiThread(()->{if(!isDestroyed()&&bitmap!=null)photo.setImageBitmap(bitmap);});}catch(Exception ignored){}});}b.addView(text(eyebrow+" · "+r.optInt("minutes")+" min",13));b.addView(text(r.optString("title"),21));b.addView(text(r.optString("technique","Receita importada")+" · "+(r.optBoolean("mainFromPantry")?"Principal da despensa":"Principal exige compras"),14));b.setOnClickListener(v->click.run());}
 String number(double n){return String.format(new Locale("pt","PT"),"%.1f",n).replace(",0","");}
 void recipe(JSONObject r,int day){
  LinearLayout f=form();TextView details=text("",16);StringBuilder s=new StringBuilder();
  s.append(prefs().optInt("people")).append(" pessoas\n\n");JSONArray ing=r.optJSONArray("ingredients");
  for(int i=0;i<ing.length();i++){JSONObject x=ing.optJSONObject(i);s.append("• ").append(number(amount(r,x))).append(" ").append(x.optString("unit")).append(" ").append(x.optString("name")).append("\n");}
  s.append("\n");JSONArray steps=r.optJSONArray("steps");for(int i=0;i<steps.length();i++)s.append(i+1).append(". ").append(steps.optString(i)).append("\n\n");
  details.setText(s.toString());f.addView(details);ScrollView sc=new ScrollView(this);sc.addView(f);
  AlertDialog dlg=new AlertDialog.Builder(this).setTitle(r.optString("title")).setView(sc).setPositiveButton("Fechar",null).setNeutralButton("Guardar",(d,w)->{for(int i=0;i<arr("favorites").length();i++)if(arr("favorites").optJSONObject(i).optString("id").equals(r.optString("id"))){toast("Já está guardada.");return;}arr("favorites").put(cleanRecipe(r));save();toast("Receita guardada.");}).create();
  if(day>=0){
   Button swap=new Button(this);swap.setText("Trocar este jantar");f.addView(swap);swap.setEnabled(!r.optBoolean("cooked"));swap.setOnClickListener(v->{Set<String> used=new HashSet<>();for(int i=0;i<arr("plan").length();i++)used.add(arr("plan").optJSONObject(i).optString("id"));JSONObject other=choose(r.optString("type"),used);if(other==null){toast("Sem alternativa dentro das preferências.");return;}putAt(arr("plan"),day,other);save();dlg.dismiss();show();});
   Button cook=new Button(this);cook.setText(r.optBoolean("cooked")?"Já preparada":"Cozinhei · atualizar despensa");f.addView(cook);cook.setEnabled(!r.optBoolean("cooked"));cook.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Deduzir da despensa as quantidades disponíveis? Ingredientes em falta não serão inventados.").setPositiveButton("Confirmar",(d,w)->{consume(r);put(r,"cooked",true);save();dlg.dismiss();show();}).setNegativeButton("Cancelar",null).show());
  }
  if(r.optString("source").startsWith("https://")){Button source=new Button(this);source.setText("Fonte original");f.addView(source);source.setOnClickListener(v->open(r.optString("source")));}
  dlg.show();
 }
 void consume(JSONObject r){JSONArray ing=r.optJSONArray("ingredients");for(int i=0;i<ing.length();i++){JSONObject x=ing.optJSONObject(i);double remaining=amount(r,x);for(int j=0;j<arr("pantry").length()&&remaining>0;j++){JSONObject p=arr("pantry").optJSONObject(j);if(key(p).equals(key(x))){double take=Math.min(remaining,p.optDouble("qty"));put(p,"qty",p.optDouble("qty")-take);remaining-=take;}}}for(int i=arr("pantry").length()-1;i>=0;i--)if(arr("pantry").optJSONObject(i).optDouble("qty")<=0)arr("pantry").remove(i);}
 void shopping(){
  TreeMap<String,JSONObject> needed=new TreeMap<>();
  for(int i=0;i<arr("plan").length();i++){JSONObject r=arr("plan").optJSONObject(i);if(r.optBoolean("cooked"))continue;JSONArray ing=r.optJSONArray("ingredients");for(int j=0;j<ing.length();j++){JSONObject x=ing.optJSONObject(j);String k=key(x);if(!needed.containsKey(k)){try{needed.put(k,new JSONObject(x.toString()));put(needed.get(k),"qty",0);}catch(Exception ignored){}}JSONObject total=needed.get(k);put(total,"qty",total.optDouble("qty")+amount(r,x));}}
  List<JSONObject> rows=new ArrayList<>();for(JSONObject x:needed.values()){put(x,"qty",Math.max(0,x.optDouble("qty")-available(x)));if(x.optDouble("qty")>0)rows.add(x);}
  rows.sort(Comparator.comparing(x->x.optString("category")));List<JSONObject> checked=new ArrayList<>();
  label("Faltas calculadas para os jantares por preparar. Unidades diferentes mantêm-se separadas.",14);
  String last="";for(JSONObject x:rows){String cat=x.optString("category");if(!last.equals(cat)){label(cat,20);last=cat;}CheckBox c=new CheckBox(this);c.setText(x.optString("name")+" · "+number(x.optDouble("qty"))+" "+x.optString("unit"));content.addView(c);c.setOnCheckedChangeListener((b,on)->{if(on)checked.add(x);else checked.remove(x);});}
  button("Transferir produtos marcados para a despensa",()->{for(JSONObject x:checked)arr("pantry").put(x);save();show();toast("Compras transferidas.");});
  if(rows.isEmpty())label("Não há ingredientes em falta.",18);
 }

 JSONObject manualIngredient(String name,double quantity,String unit,String category)throws Exception{
  if(name.trim().isEmpty()||!Double.isFinite(quantity)||quantity<=0||!Arrays.asList("g","ml","un").contains(unit))throw new Exception("Preenche o nome, quantidade positiva e unidade de cada ingrediente.");
  JSONObject item=new JSONObject();put(item,"name",name.trim());put(item,"qty",quantity);put(item,"unit",unit);put(item,"category",category);return item;
 }
 JSONObject buildManualRecipe(String title,String type,double servings,int minutes,JSONObject main,JSONArray extras,String preparation)throws Exception{
  if(title.trim().isEmpty()||!Arrays.asList("carne","peixe").contains(type)||!Double.isFinite(servings)||servings<1||servings>20||minutes<1||minutes>180)throw new Exception("Indica nome, carne ou peixe, 1–20 porções e 1–180 minutos.");
  JSONArray ingredients=new JSONArray();Set<String> seen=new HashSet<>();
  JSONObject checked=manualIngredient(main.getString("name"),main.getDouble("qty"),main.getString("unit"),"peixe".equals(type)?"Peixaria":"Talho");ingredients.put(checked);seen.add(key(checked));
  for(int i=0;i<extras.length();i++){JSONObject x=extras.getJSONObject(i);checked=manualIngredient(x.getString("name"),x.getDouble("qty"),x.getString("unit"),x.optString("category","Outros"));if(!seen.add(key(checked)))throw new Exception("Junta as quantidades dos ingredientes repetidos numa só linha.");ingredients.put(checked);}
  JSONArray steps=new JSONArray();for(String line:preparation.split("\\n"))if(!line.trim().isEmpty())steps.put(line.trim());
  if(steps.length()==0)throw new Exception("Escreve pelo menos um passo de preparação.");
  JSONObject r=new JSONObject();put(r,"id","manual-"+UUID.randomUUID());put(r,"title",title.trim());put(r,"type",type);put(r,"servings",servings);put(r,"minutes",minutes);put(r,"ingredients",ingredients);put(r,"steps",steps);put(r,"technique","Receita de família");put(r,"source","");put(r,"image","");return r;
 }
 void storeManualRecipe(JSONObject recipe)throws Exception{
  JSONObject candidate=new JSONObject(state.toString());candidate.getJSONArray("favorites").put(recipe);validate(candidate);state=candidate;save();
 }
 Spinner picker(LinearLayout parent,String[] options){Spinner p=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,options);adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);p.setAdapter(adapter);parent.addView(p);return p;}
 double decimal(EditText field)throws Exception{return Double.parseDouble(field.getText().toString().trim().replace(',','.'));}
 void manualRecipe(){
  LinearLayout f=form();f.addView(text("A receita fica nas Guardadas e pode entrar na ementa. As quantidades são para o número de porções indicado.",14));
  EditText title=input(f,"Nome da receita","");
  f.addView(text("Tipo de receita",14));Spinner kind=picker(f,new String[]{"carne","peixe"});
  EditText servings=input(f,"Porções da receita","4"),minutes=input(f,"Tempo total em minutos","30");
  servings.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);minutes.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
  f.addView(text("Ingrediente principal — o que deve existir na despensa",17));
  EditText primaryName=input(f,"Ex.: bifes de frango",""),primaryQty=input(f,"Quantidade do principal","");primaryQty.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
  Spinner primaryUnit=picker(f,new String[]{"g","ml","un"});
  f.addView(text("Restantes ingredientes",17));
  LinearLayout extras=new LinearLayout(this);extras.setOrientation(LinearLayout.VERTICAL);f.addView(extras);
  List<EditText[]> entries=new ArrayList<>();List<Spinner> units=new ArrayList<>();
  Button add=new Button(this);add.setText("+ Adicionar ingrediente");add.setTypeface(face());f.addView(add);
  add.setOnClickListener(v->{
   LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(0,dp(8),0,dp(8));extras.addView(row);
   EditText name=input(row,"Nome do ingrediente",""),qty=input(row,"Quantidade","");qty.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
   Spinner unit=picker(row,new String[]{"g","ml","un"});EditText[] entry={name,qty};entries.add(entry);units.add(unit);
   Button remove=new Button(this);remove.setText("Remover ingrediente");row.addView(remove);remove.setOnClickListener(clicked->{int index=entries.indexOf(entry);if(index>=0){entries.remove(index);units.remove(index);}extras.removeView(row);});
  });
  f.addView(text("Preparação — um passo por linha",17));EditText steps=input(f,"Descreve como preparas esta receita","");steps.setSingleLine(false);steps.setMinLines(5);steps.setGravity(Gravity.TOP);
  ScrollView sc=new ScrollView(this);sc.addView(f);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Adicionar receita").setView(sc).setPositiveButton("Guardar receita",null).setNegativeButton("Cancelar",null).create();
  dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
   try{
    String type=kind.getSelectedItem().toString();
    JSONObject principal=manualIngredient(primaryName.getText().toString(),decimal(primaryQty),primaryUnit.getSelectedItem().toString(),"Outros");
    JSONArray others=new JSONArray();for(int i=0;i<entries.size();i++){EditText[] e=entries.get(i);others.put(manualIngredient(e[0].getText().toString(),decimal(e[1]),units.get(i).getSelectedItem().toString(),"Outros"));}
    JSONObject recipe=buildManualRecipe(title.getText().toString(),type,decimal(servings),Integer.parseInt(minutes.getText().toString().trim()),principal,others,steps.getText().toString());
    storeManualRecipe(recipe);dialog.dismiss();page=4;show();toast("Receita guardada. Já pode entrar na ementa.");
   }catch(Exception e){toast(e instanceof NumberFormatException?"Verifica as porções, os minutos e as quantidades.":e.getMessage());}
  }));
  dialog.show();
 }

 void favorites(){button("+ Adicionar receita",()->manualRecipe());label("Só fica aqui aquilo que decides guardar.",15);for(int i=0;i<arr("favorites").length();i++){final int n=i;JSONObject r=arr("favorites").optJSONObject(i);card(r,"A tua coleção",()->recipe(r,-1));button("Eliminar "+r.optString("title"),()->{arr("favorites").remove(n);save();show();});}}
 void open(String url){try{Uri u=Uri.parse(url);if(!"https".equals(u.getScheme()))throw new Exception();startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception e){toast("Não foi possível abrir a ligação HTTPS.");}}
 void trends(){
  label("Inspiração em movimento",21);label("Pesquisa atual nas plataformas originais. Sem rankings ou métricas de popularidade verificados. Nenhuma pesquisa fica guardada.",15);
  EditText query=input(content,"Ex.: robalo air fryer receita portuguesa","jantar rápido peixe");
  button("Descobrir no YouTube",()->open("https://www.youtube.com/results?search_query="+Uri.encode(query.getText()+" shorts receita")));
  button("Descobrir no TikTok",()->open("https://www.tiktok.com/search?q="+Uri.encode(query.getText().toString())));
  button("Descobrir Reels",()->open("https://www.google.com/search?q="+Uri.encode("site:instagram.com/reel/ "+query.getText())));
  label("Adicionar inspiração à tua coleção",20);label("Importa uma página portuguesa com dados Recipe. A receita só fica guardada se confirmares. Importação de páginas sujeita à disponibilidade e permissão da fonte; páginas bloqueadas e vídeos sem receita estruturada não são suportados.",14);
  button("Importar receita por ligação HTTPS",()->importRecipe());
  button("Aprender técnicas de peixe",()->fish());
 }
 byte[] fetch(String address,int limit)throws Exception{
  URL url=new URL(address);if(!url.getProtocol().equals("https"))throw new IOException("Apenas HTTPS");
  HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setConnectTimeout(10000);c.setReadTimeout(15000);c.setInstanceFollowRedirects(false);c.setRequestProperty("User-Agent","MesaDaSemana/0.1 (personal recipe importer)");
  try{if(c.getResponseCode()!=200)throw new IOException("Fonte indisponível ou redirecionada");try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>limit)throw new IOException("Resposta demasiado grande");out.write(b,0,n);}return out.toByteArray();}}finally{c.disconnect();}
 }
 JSONObject findRecipe(Object o){
  if(o instanceof JSONArray){JSONArray a=(JSONArray)o;for(int i=0;i<a.length();i++){JSONObject r=findRecipe(a.opt(i));if(r!=null)return r;}}
  if(o instanceof JSONObject){JSONObject j=(JSONObject)o;Object t=j.opt("@type");if("Recipe".equals(t)||(t instanceof JSONArray&&t.toString().contains("\"Recipe\"")))return j;Iterator<String> keys=j.keys();while(keys.hasNext()){JSONObject r=findRecipe(j.opt(keys.next()));if(r!=null)return r;}}
  return null;
 }
 void instruction(Object o,JSONArray out){
  if(o instanceof JSONArray){JSONArray a=(JSONArray)o;for(int i=0;i<a.length();i++)instruction(a.opt(i),out);}
  else if(o instanceof JSONObject){JSONObject j=(JSONObject)o;if(j.has("text"))out.put(android.text.Html.fromHtml(j.optString("text"),0).toString());else instruction(j.opt("itemListElement"),out);}
  else if(o instanceof String)out.put(android.text.Html.fromHtml((String)o,0).toString());
 }
 void importRecipe(){
  LinearLayout f=form();EditText url=input(f,"Ligação HTTPS da receita","");CheckBox permitted=new CheckBox(this);permitted.setText("A fonte permite esta importação para uso pessoal");f.addView(permitted);
  new AlertDialog.Builder(this).setTitle("Importar receita").setView(f).setPositiveButton("Ler página",(d,w)->{if(!permitted.isChecked()){toast("Confirma primeiro a permissão da fonte.");return;}String address=url.getText().toString().trim();toast("A ler a receita…");network.execute(()->{
   try{
    String html=new String(fetch(address,2000000),"UTF-8");Matcher scripts=Pattern.compile("<script[^>]*type\\s*=\\s*['\"]application/ld\\+json['\"][^>]*>([\\s\\S]*?)</script>",Pattern.CASE_INSENSITIVE).matcher(html);
    JSONObject found=null;while(scripts.find()&&found==null){try{found=findRecipe(new JSONTokener(scripts.group(1).trim()).nextValue());}catch(Exception ignored){}}
    if(found==null)throw new IOException("A página não disponibiliza uma receita estruturada.");
    JSONObject raw=found;runOnUiThread(()->reviewImport(raw,address));
   }catch(Exception e){runOnUiThread(()->toast("Não foi possível importar: "+e.getMessage()));}
  });}).setNegativeButton("Cancelar",null).show();
 }
 void reviewImport(JSONObject raw,String address){
  LinearLayout f=form();EditText title=input(f,"Nome em português",raw.optString("name"));EditText type=input(f,"peixe ou carne","peixe"),servings=input(f,"Porções originais (confirmar)","4"),minutes=input(f,"Tempo total em minutos (confirmar)","30");
  TextView info=text("Confirma as porções e o tempo na fonte. Normaliza cada ingrediente: nome | quantidade | g/ml/un | categoria. Quantidades originais abaixo; não são convertidas automaticamente.",14);f.addView(info);
  JSONArray originals=raw.optJSONArray("recipeIngredient");StringBuilder lines=new StringBuilder();if(originals!=null)for(int i=0;i<originals.length();i++)lines.append(originals.optString(i)).append("\n");
  EditText ingredients=input(f,"nome | quantidade | unidade | categoria",lines.toString());ingredients.setSingleLine(false);ingredients.setMinLines(4);
  JSONArray rawSteps=new JSONArray();instruction(raw.opt("recipeInstructions"),rawSteps);StringBuilder stepsText=new StringBuilder();for(int i=0;i<rawSteps.length();i++)stepsText.append(rawSteps.optString(i)).append("\n");
  EditText steps=input(f,"Um passo em português por linha",stepsText.toString());steps.setSingleLine(false);steps.setMinLines(4);
  ScrollView scroll=new ScrollView(this);scroll.addView(f);
  new AlertDialog.Builder(this).setTitle("Rever e guardar").setView(scroll).setPositiveButton("Guardar receita",(d,w)->{
   try{JSONObject r=new JSONObject();put(r,"id",UUID.randomUUID().toString());put(r,"title",title.getText().toString());String category=type.getText().toString().trim();if(!Arrays.asList("peixe","carne").contains(category))throw new Exception();put(r,"type",category);put(r,"minutes",Integer.parseInt(minutes.getText().toString()));put(r,"servings",Integer.parseInt(servings.getText().toString()));put(r,"source",address);
    Object image=raw.opt("image");String imageUrl=image instanceof String?(String)image:image instanceof JSONArray?((JSONArray)image).optString(0):image instanceof JSONObject?((JSONObject)image).optString("url"):"";put(r,"image",imageUrl);
    JSONArray ing=new JSONArray();for(String line:ingredients.getText().toString().split("\n")){if(line.trim().isEmpty())continue;String[] p=line.split("\\|");if(p.length!=4)throw new Exception();JSONObject x=new JSONObject();put(x,"name",p[0].trim());put(x,"qty",Double.parseDouble(p[1].trim().replace(',','.')));put(x,"unit",p[2].trim());put(x,"category",p[3].trim());ing.put(x);}if(ing.length()==0)throw new Exception();put(r,"ingredients",ing);
    JSONArray cookedSteps=new JSONArray();for(String line:steps.getText().toString().split("\n"))if(!line.trim().isEmpty())cookedSteps.put(line.trim());if(cookedSteps.length()==0)throw new Exception();put(r,"steps",cookedSteps);
    JSONObject candidate=new JSONObject(state.toString());candidate.getJSONArray("favorites").put(r);validate(candidate);state=candidate;save();page=4;show();
   }catch(Exception e){toast("Verifica as quantidades normalizadas, as porções, o tipo e os passos. Nada foi guardado.");}
  }).setNegativeButton("Descartar",null).show();
 }
 void fish(){
  new AlertDialog.Builder(this).setTitle("Peixe · técnica e ponto").setMessage(
   "Referência de segurança: atingir 63 °C no centro, medidos com termómetro. Os tempos são indicativos e dependem da espessura e do equipamento.\n\n"+
   "Frigideira · salmão ou robalo: secar, temperar com sal e azeite. Aquecer em lume médio-alto, colocar a pele para baixo 4–6 min e virar 2–4 min.\n\n"+
   "Forno · dourada ou robalo inteiro: 190 °C, cerca de 20–30 min para 400–600 g. Limão, alho e salsa; verificar junto à espinha.\n\n"+
   "Papelote · pescada: 190 °C, 15–20 min para lombos descongelados. Juntar alho-francês fino e azeite. Abrir longe do rosto.\n\n"+
   "Grelhado · carapau: grelha quente, untada; 4–6 min por lado conforme tamanho. Virar apenas quando se soltar.\n\n"+
   "Air fryer · salmão ou bacalhau demolhado: 180 °C, cerca de 10–15 min para lombos. Não sobrepor. Confirmar o centro.\n\n"+
   "O peixe deve lascar e perder a translucidez; o termómetro confirma a segurança. Descongelar no frigorífico. Separar utensílios de cru e cozinhado; confirmar espinhas ao servir às crianças. Bacalhau salgado precisa de demolha refrigerada.").setPositiveButton("Entendido",null).setNeutralButton("Fonte de segurança",(d,w)->open("https://www.foodsafety.gov/food-safety-charts/safe-minimum-internal-temperatures")).show();
 }
 void settings(){
  LinearLayout f=form();EditText people=input(f,"Pessoas (1–20)",""+prefs().optInt("people")),minutes=input(f,"Tempo máximo (10–180 min)",""+prefs().optInt("minutes"));
  f.addView(text("Só carne e peixe. A ementa segue a despensa, com prioridade ao peixe disponível; não há distribuição fixa.",14));
  Button export=new Button(this);export.setText("Exportar dados");f.addView(export);export.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").putExtra(Intent.EXTRA_TITLE,"mesa-da-semana.json");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,10);});
  Button imp=new Button(this);imp.setText("Importar dados · substitui os atuais");f.addView(imp);imp.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Importar substitui todos os dados. Exporta primeiro se precisares de uma cópia.").setPositiveButton("Escolher ficheiro",(d,w)->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/json");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,11);}).setNegativeButton("Cancelar",null).show());
  new AlertDialog.Builder(this).setTitle("Preferências").setView(f).setPositiveButton("Guardar",(d,w)->{try{JSONObject p=new JSONObject(prefs().toString());put(p,"people",Integer.parseInt(people.getText().toString()));put(p,"minutes",Integer.parseInt(minutes.getText().toString()));JSONObject candidate=new JSONObject(state.toString());put(candidate,"preferences",p);validate(candidate);state=candidate;save();show();}catch(Exception e){toast("Verifica os limites de pessoas e tempo.");}}).setNegativeButton("Cancelar",null).show();
 }
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;try{if(request==10){try(OutputStream out=getContentResolver().openOutputStream(data.getData())){out.write(state.toString(2).getBytes("UTF-8"));}toast("Dados exportados.");}if(request==11){JSONObject next=new JSONObject(read(getContentResolver().openInputStream(data.getData()),2000000));validate(next);state=next;save();show();toast("Dados importados.");}}catch(Exception e){toast("Operação falhou. Os dados existentes foram preservados.");}}
 @Override protected void onDestroy(){network.shutdownNow();super.onDestroy();}
}
