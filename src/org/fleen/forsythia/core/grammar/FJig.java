package org.fleen.forsythia.core.grammar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.fleen.forsythia.core.Forsythia;
import org.fleen.forsythia.core.composition.FGridTransform;
import org.fleen.forsythia.core.composition.FPolygon;
import org.fleen.forsythia.core.composition.ForsythiaTreeNode;
import org.fleen.geom_2D.DPolygon;
import org.fleen.geom_2D.IncircleCalculator;
import org.fleen.geom_Kisrhombille.KPolygon;
import org.fleen.util.tag.TagManager;
import org.fleen.util.tag.Tagged;

/*
 * given a polygon, create new polygons
 * like cutting a board into puzzle pieces
 * each metagon has a lits of these associated with it
 */
public class FJig implements Serializable,Tagged,Forsythia {
  
  private static final long serialVersionUID=-5737903972508676140L;
  
  /*
   * ################################
   * CONSTRUCTOR
   * ################################
   */

  public FJig(int griddensity,List<FJigSection> sections,String[] tags){
    this.griddensity=griddensity;
    this.sections=sections;
    tagmanager.setTags(tags);}
  
  /*
   * ################################
   * GRID
   * 
   * A jig produces a grid and then the sections are used to create polygons in terms of that grid
   * The grid is defined in terms of the target poylgon and a density value
   * 
   * Grid density is the density of this jig's grid relative to that of it's parent grid (target.parent)
   * For example, density 1 means that this grid is the same size as it's parent. 2 means it's twice as dense,
   *  eg : half the size. 5 means 1/5 size, etc. 
   *  
   * Fish factor is the reciprocal of grid density. It is the value of fish (the fundamental 
   * grid unit of length) for this jig's grid in terms of the parentpolygon's value 
   *    for fish (dis(v0,v1)/ghost.baseinterval)
   * ################################
   */
  
  public int griddensity;
  
  public int getGridDensity(){
    return griddensity;}
  
  public double getFishFactor(){
    return 1.0/(double)getGridDensity();}
  
  /*
   * ################################
   * SECTIONS
   * 
   * A Jig has 2..n sections
   * These specify, along with the grid, the produced polygons
   * ################################
   */
  
  public List<FJigSection> sections;
  
  /*
   * ################################
   * NODE PRODUCTION
   * 
   * create a grid node
   * create 2..n polygon nodes
   * hook everything up to the tree appropriately
   * return the new polygon nodes
   * 
   * we define the grid in terms of the target polygon and this jig's griddensity value
   * the grid origin is the v0 of the target polygon
   * the grid fish is the fish of the target polygon's founding grid * this jig's fishfactor
   * the grid foreward is the target's base foreward
   * the grid twist is the target's twist
   * 
   * The target direction(v0,v1) is always 0
   * ################################
   */
  
  public List<ForsythiaTreeNode> createNodes(FPolygon target){
    List<ForsythiaTreeNode> newnodes=new ArrayList<ForsythiaTreeNode>();
    //create the grid node
    FGridTransform newgrid=new FGridTransform(
      target.anchor.v0,
      target.getLocalBaseForeward(),
      target.anchor.twist,
      getFishFactor()*target.getLocalBaseInterval()/target.metagon.baseinterval);
    //hook up the grid node to the target
    target.setChild(newgrid);
    newgrid.setParent(target);
    //create nodes. hook them up.
    ForsythiaTreeNode newnode;
    for(FJigSection section:sections){
      newnode=section.createNode();
      newnode.setParent(newgrid);
      newnodes.add(newnode);}
    newgrid.setChildren(newnodes);
    return newnodes;}

  /*
   * ################################
   * DETAIL SIZE PREVIEW
   * This tells us the size of the polygons that this jig will generate
   * before we actually generate them, so we know.
   * Because we filter jigs by this. 
   * Because we try to control detail size in our compositions.
   * 
   * detail size is the smallest incircle diameter of the generated polygons, scaled to local fish 
   * that is : smallestincirclerad*localfish
   * controlling detail size helps us control the fine detail level of the composition
   * the composition logic goes like this :
   *   if the detail size is below our specified limit then the proposed 
   *   polygons are too finely detailed, so we reject the jig
   *   
   * The detail size of this jig can be described as a ratio (basedetailsize) of the target fish value.
   * basedetailsize is constant throughout the lifetime of this jig
   * so we need only calculate it once. 
   * 
   * TODO
   * surely we could use the unscaled default detail size and simply scale it to whatever grid
   * 
   * ################################
   */
  
  private Double detailsize=null;
  
  /*
   * TODO ##########
   * this is our normal detail size
   * it's gotten from the default polygon
   */
  public double getDetailSize(){
    if(detailsize==null)
      initDetailSize();
    return detailsize;}
  
//  /*
//   * TODO ############
//   * This is our normal detail size, scaled to the context to the specified polygon
//   */
//  public double getDetailSizePreview(FPolygon target){
//    double bds=getDetailSize();
//    double fish=
//      target.getFirstAncestorGrid().getLocalKGrid().getFish()*
//      getFishFactor()*target.getLocalBaseInterval()/target.metagon.baseinterval;
//    double detailsize=bds*fish;
//    return detailsize;}
  
  //get the incircle radius of all generated polygons at default scale (fish==1.0)
  //we double the smallest incircle radius to get our base detail size
  void initDetailSize(){
    List<KPolygon> polygons=getDSPTestPolygons();
    double testradius,minradius=Double.MAX_VALUE;
    for(KPolygon p:polygons){
      testradius=IncircleCalculator.getIncircle(p.getDefaultPolygon2D()).r;
      if(testradius<minradius)
        minradius=testradius;}
    detailsize=minradius*2.0;}
  
  private List<KPolygon> getDSPTestPolygons(){
    List<KPolygon> polygons=new ArrayList<KPolygon>(sections.size());
    KPolygon p;
    for(FJigSection section:sections){
      p=section.productmetagon.getPolygon(section.productanchor.v0,section.productanchor.v1);
      polygons.add(p);}
    return polygons;}
  
  //TODO it's a good idea for test and tool but how does it fit into our design
  public List<DPolygon> getTestPolygons(){
    List<DPolygon> polygons=new ArrayList<DPolygon>();
    for(FJigSection s:sections)
      polygons.add(s.getTestPolygon());
    return polygons;}
  
  /*
   * ################################
   * TAGS
   * ################################
   */
  
  private TagManager tagmanager=new TagManager();
  
  public void setTags(String... tags){
    tagmanager.setTags(tags);}
  
  public void setTags(List<String> tags){
    tagmanager.setTags(tags);}
  
  public List<String> getTags(){
    return tagmanager.getTags();}
  
  public boolean hasTag(String tag){
    return tagmanager.hasTag(tag);}
  
  public boolean hasTags(String... tags){
    return tagmanager.hasTags(tags);}
  
  public boolean hasTags(List<String> tags){
    return tagmanager.hasTags(tags);}
  
  public void addTag(String tag){
    tagmanager.addTags(tag);}
  
  public void addTags(String... tags){
    tagmanager.addTags(tags);}
  
  public void addTags(List<String> tags){
    tagmanager.addTags(tags);}
  
  public void removeTag(String tag){
    tagmanager.removeTags(tag);}
  
  public void removeTags(String... tags){
    tagmanager.removeTags(tags);}
  
  public void removeTags(List<String> tags){
    tagmanager.removeTags(tags);}
  
  /*
   * ################################
   * GENERAL PURPOSE OBJECT
   * ################################
   */
  
  public Object gpobject;
  
  /*
   * ################################
   * OBJECT
   * ################################
   */
  
  public String toString(){
    StringBuffer a=new StringBuffer();
    a.append("["+getClass().getSimpleName()+" ");
    a.append("griddensity="+griddensity+" ");
    a.append("tags="+tagmanager.toString()+"\n");
    for(FJigSection s:sections)
      a.append(s.toString()+"\n");
    return a.toString();}
  
}
