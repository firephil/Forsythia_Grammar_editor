package org.fleen.forsythia.app.grammarEditor.editor_Generator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fleen.forsythia.core.composition.FPolygon;
import org.fleen.forsythia.core.composition.FPolygonSignature;
import org.fleen.forsythia.core.composition.ForsythiaComposition;
import org.fleen.geom_2D.DPoint;
import org.fleen.util.tree.TreeNode;

public class Renderer implements Serializable{
  
  private static final long serialVersionUID=1885473221116487499L;
  
  private static final Color BACKGROUNDCOLOR=new Color(128,128,128);
  private static final int MARGIN=10;
  
  public static final HashMap<RenderingHints.Key,Object> RENDERING_HINTS=
      new HashMap<RenderingHints.Key,Object>();
    
  static{
    RENDERING_HINTS.put(
      RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    RENDERING_HINTS.put(
      RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
    RENDERING_HINTS.put(
      RenderingHints.KEY_DITHERING,RenderingHints.VALUE_DITHER_DEFAULT);
    RENDERING_HINTS.put(
      RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    RENDERING_HINTS.put(
      RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    RENDERING_HINTS.put(
      RenderingHints.KEY_COLOR_RENDERING,RenderingHints.VALUE_COLOR_RENDER_QUALITY); 
    RENDERING_HINTS.put(
      RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_NORMALIZE);}
  
  Random rnd=new Random();
  
  /*
   * ################################
   * GET IMAGE
   * ################################
   */
  
  BufferedImage image;
  AffineTransform transform;
  Graphics2D graphics;
  
  public BufferedImage getImage(int width,int height,ForsythiaComposition composition){
    image=getInitImage(width,height);
    transform=getTransform(width,height,composition);
    graphics=(Graphics2D)image.getGraphics();
    graphics.setTransform(transform);
    graphics.setRenderingHints(RENDERING_HINTS);
    render(composition);
    return image;}
  
  private BufferedImage getInitImage(int w,int h){
    BufferedImage image=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics=image.createGraphics();
    graphics.setPaint(BACKGROUNDCOLOR);
    graphics.fillRect(0,0,w,h);
    return image;}
  
  private AffineTransform getTransform(int imagewidth,int imageheight,ForsythiaComposition composition){
    //get all the relevant metrics
    Rectangle2D.Double compositionbounds=composition.getRootPolygon().getDPolygon().getBounds();
    double
      dmargin=MARGIN,
      cbwidth=compositionbounds.getWidth(),
      cbheight=compositionbounds.getHeight(),
      cbxmin=compositionbounds.getMinX(),
      cbymin=compositionbounds.getMinY();
    AffineTransform transform=new AffineTransform();
    //scale
    double 
      sw=(imagewidth-dmargin*2)/cbwidth,
      sh=(imageheight-dmargin*2)/cbheight;
    double scale=Math.min(sw,sh);
    transform.scale(scale,-scale);//flip y for proper cartesian orientation
    //offset
    double
      xoff=((imagewidth/scale-cbwidth)/2.0)-cbxmin,
      yoff=-(((imageheight/scale+cbheight)/2.0)+cbymin);
    transform.translate(xoff,yoff);
    //
    return transform;}
  
  /*
   * ################################
   * RENDER FORSYTHIA COMPOSITION TO IMAGE
   * ################################
   */
  
  private void render(ForsythiaComposition forsythia){
    initStroke();
    initColorPairs();
        
    Path2D path;
    //FILL POLYGONS
    Color color;
    for(FPolygon polygon:forsythia.getLeafPolygons()){
      color=getColor(polygon);
      graphics.setPaint(color);
      path=getPath2D(polygon);
      graphics.fill(path);}
    //STROKE POLYGONS
    graphics.setPaint(strokecolor);
    graphics.setStroke(stroke);
    for(FPolygon polygon:forsythia.getLeafPolygons()){
      path=getPath2D(polygon);
      graphics.draw(path);}}
  
  /*
   * ################################
   * COLOR
   * use black stroke
   * use or palette mother for the 2 color pairs
   * ################################
   */
  
  private Color strokecolor=Color.black;
  private Color[] color0,color1;
  
  private Map<FPolygonSignature,Color> polygoncolors=new Hashtable<FPolygonSignature,Color>();
  
  public Color getColor(FPolygon polygon){
    FPolygonSignature sig=polygon.getSignature();
    Color color=polygoncolors.get(sig);
    if(color==null){
      int 
        eggdepth=getTagDepth(polygon,"egg"),
        colorindex;
      //even level
      if(eggdepth%2==0){
        colorindex=rnd.nextInt(color0.length);
        color=color0[colorindex];
      //odd level
      }else{
        colorindex=rnd.nextInt(color1.length);
        color=color1[colorindex];}
      polygoncolors.put(sig,color);}
    return color;}
  
 

  public static final Color[] P_TOY_STORY_ADJUSTED2= new Color[]{
      new Color(168,67,39),
      new Color(251,206,89),
      new Color(88,184,121),
      new Color(154,94,154),
      new Color(234,61,65),
      new Color(248,237,23),
      new Color(249,139,90),
      new Color(0,146,232),
      new Color(254,178,213)
  };
  
  
public static final Color[] colors = getColors() ;


 public static final Color[]  getColors(){

        Color[] rgb = getColorsRBG();
        Color[] hex = getColorsHex();
        Color[] res ;

        if(rgb!=null) 
            res = rgb;
        else 
            res = hex;
        
        if(hex == null && rgb == null)
            res = P_TOY_STORY_ADJUSTED2;
        
        return res;
    }
 
public static final Color[] getColorsRBG() {
         Color[] colors = null;
        try {
            List<String> colorList;
            colorList = Files.readAllLines(Paths.get(getJarPath()+"//colors.txt"));
            
            colors = new Color[colorList.size()];
            
            for(int i =0; i<colorList.size();i++){
                String[] tmp = colorList.get(i).split(",");
                colors[i] = new Color(Integer.parseInt(tmp[0]),Integer.parseInt(tmp[1]),
                        Integer.parseInt(tmp[2]));
                System.out.println(colors[i]);
            }
            
        } catch (IOException ex) {
            System.out.println("RGB file not Found");
        }
        return colors;
    }
     public static final Color[] getColorsHex() {
         Color[] colors = null;
        try {
            List<String> colorList;
            colorList = Files.readAllLines(Paths.get(getJarPath()+"//colors.hex"));
            
            colors = new Color[colorList.size()];
            
            for(int i =0; i<colorList.size();i++){
                  colors[i] = Color.decode("#"+colorList.get(i));
                  System.out.println(colors[i]);
            }
        
        return colors;       
        
        } catch (IOException ex) {
            System.out.println("HEX file not Found");
        }
        return null;
    }

    public static final String getJarPath() {
            try {
                String s  = new File(Renderer.class.getProtectionDomain().getCodeSource().getLocation()
                        .toURI()).getPath();
                String  path = new File(s).getParent();
                return path;
            } catch (URISyntaxException ex) {
                System.out.println("Cannot Find File");
            }
            return "";
    }

    private void initColorPairs1(){
    int a=P_TOY_STORY_ADJUSTED2.length/2;
    color0=new Color[a];
    for(int i=0;i<a;i++)
      color0[i]=P_TOY_STORY_ADJUSTED2[i];
    color1=new Color[P_TOY_STORY_ADJUSTED2.length-a];
    for(int i=a;i<P_TOY_STORY_ADJUSTED2.length;i++)
      color1[i-a]=P_TOY_STORY_ADJUSTED2[i];
    System.out.println("foo");
  }
  
  private void initColorPairs(){
    int a=colors.length/2;
    color0=new Color[a];
    for(int i=0;i<a;i++)
      color0[i]=colors[i];
    color1=new Color[colors.length-a];
    for(int i=a;i<colors.length;i++)
      color1[i-a]=colors[i];
    System.out.println("foo");
  }
   
  /*
   * ################################
   * STROKE
   * ################################
   */
  
  private static final float STROKEWIDTH=0.006f;
  
  Stroke stroke;
  
  private void initStroke(){
    stroke=new BasicStroke(STROKEWIDTH,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_ROUND,0,null,0);}
  
  /*
   * ################################
   * 2D GEOMETRY UTILS
   * ################################
   */
  
  Map<FPolygon,Path2D> pathbypolygon=new Hashtable<FPolygon,Path2D>();
  
  private Path2D getPath2D(FPolygon polygon){
    Path2D path=pathbypolygon.get(polygon);
    if(path==null){
      path=createPath2D(polygon);
      pathbypolygon.put(polygon,path);}
    return path;}
  
  private Path2D createPath2D(FPolygon polygon){
    Path2D.Double path=new Path2D.Double();
    List<DPoint> points=polygon.getDPolygon();
    DPoint p=points.get(0);
    path.moveTo(p.x,p.y);
    for(int i=1;i<points.size();i++){
      p=points.get(i);
      path.lineTo(p.x,p.y);}
    path.closePath();
    return path;}
  
  /*
   * ################################
   * TAG DEPTH
   * 
   * returns the number of times the specified tag is encountered in a polygon's ancestry
   * examples :
   *   if the tag exists only once in the ancestry then the tagdepth is 1
   *   if the tag exists in this polygon and its grandparent's polygon then the tagdepth is 2
   *   if the tag exists nowhere in this polygon's ancestry then the tagdepth is 0
   *   
   * for example we sometimes use it to get "egg" level.
   * An egg is a polygon that has been completely divorced from its parent's polygon's edge.
   *   That is to say, an interior shape.
   *   
   * ################################
   */
  
  protected int getTagDepth(TreeNode node,String tag){
    int c=0;
    TreeNode n=node;
    FPolygon p;
    while(n!=null){
      if(n instanceof FPolygon){
        p=(FPolygon)n;
        if(p.hasTags(tag))
          c++;}
      n=n.getParent();}
    return c;}
  
}