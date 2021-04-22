/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fleen.forsythia.core.grammar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.fleen.forsythia.core.Forsythia;
import org.fleen.forsythia.core.composition.FGridTransform;
import org.fleen.forsythia.core.composition.FPolygon;
import org.fleen.forsythia.core.composition.ForsythiaTreeNode;
import org.fleen.forsythia.core.grammar.Jig;
import org.fleen.forsythia.core.grammar.JigSection;
import org.fleen.geom_2D.DPolygon;
import org.fleen.geom_2D.IncircleCalculator;
import org.fleen.geom_Kisrhombille.KPolygon;
import org.fleen.util.tag.TagManager;
import org.fleen.util.tag.Tagged;
import org.fleen.util.tree.TreeNode;

public class Jig implements Serializable, Tagged, Forsythia {
  private static final long serialVersionUID = -5737903972508676140L;
  
  public int griddensity;
  
  public List<JigSection> sections;
  
  private Double detailsizepreviewbasedetailsize;
  
  private TagManager tagmanager;
  
  public Object gpobject;
  
  
  public Jig(int griddensity, List<JigSection> sections, String[] tags) {
    this.detailsizepreviewbasedetailsize = null;
    this.tagmanager = new TagManager();
    this.griddensity = griddensity;
    this.sections = sections;
    this.tagmanager.setTags(tags);
  }
  
  public int getGridDensity() {
    return this.griddensity;
  }
  
  public double getFishFactor() {
    return 1.0D / getGridDensity();
  }
  
  public List<ForsythiaTreeNode> createNodes(FPolygon target) {
    List<ForsythiaTreeNode> newnodes = new ArrayList<>();
    FGridTransform newgrid = new FGridTransform(target.anchor.v0, target.getLocalBaseForeward(), target.anchor.twist, getFishFactor() * target.getLocalBaseInterval() / target.metagon.baseinterval);
    target.setChild((TreeNode)newgrid);
    newgrid.setParent((TreeNode)target);
    for (JigSection section : this.sections) {
      ForsythiaTreeNode newnode = section.createNode();
      newnode.setParent((TreeNode)newgrid);
      newnodes.add(newnode);
    } 
    newgrid.setChildren(newnodes);
    return newnodes;
  }
  
  public double getDetailSizePreview(FPolygon target) {
    double bds = getDetailSizePreviewBaseDetailSize();
    double fish = target.getFirstAncestorGrid().getLocalKGrid().getFish() * getFishFactor() * target.getLocalBaseInterval() / target.metagon.baseinterval;
    double detailsize = bds * fish;
    return detailsize;
  }
  
  public double getDetailSizePreviewBaseDetailSize() {
    if (this.detailsizepreviewbasedetailsize == null)
      initDetailSizePreviewBaseDetailSize(); 
    return this.detailsizepreviewbasedetailsize.doubleValue();
  }
  
  void initDetailSizePreviewBaseDetailSize() {
    List<KPolygon> polygons = getDSPTestPolygons();
    double minradius = Double.MAX_VALUE;
    for (KPolygon p : polygons) {
      double testradius = (IncircleCalculator.getIncircle((List)p.getDefaultPolygon2D())).r;
      if (testradius < minradius)
        minradius = testradius; 
    } 
    this.detailsizepreviewbasedetailsize = Double.valueOf(minradius * 2.0D);
  }
  
  private List<KPolygon> getDSPTestPolygons() {
    List<KPolygon> polygons = new ArrayList<>(this.sections.size());
    for (JigSection section : this.sections) {
      KPolygon p = section.productmetagon.getPolygon(section.productanchor.v0, section.productanchor.v1);
      polygons.add(p);
    } 
    return polygons;
  }
  
  public List<DPolygon> getTestPolygons() {
    List<DPolygon> polygons = new ArrayList<>();
    for (JigSection s : this.sections)
      polygons.add(s.getTestPolygon()); 
    return polygons;
  }
  
  public void setTags(String... tags) {
    this.tagmanager.setTags(tags);
  }
  
  public void setTags(List<String> tags) {
    this.tagmanager.setTags(tags);
  }
  
  public List<String> getTags() {
    return this.tagmanager.getTags();
  }
  
  public boolean hasTags(String... tags) {
    return this.tagmanager.hasTags(tags);
  }
  
  public boolean hasTags(List<String> tags) {
    return this.tagmanager.hasTags(tags);
  }
  
  public void addTags(String... tags) {
    this.tagmanager.addTags(tags);
  }
  
  public void addTags(List<String> tags) {
    this.tagmanager.addTags(tags);
  }
  
  public void removeTags(String... tags) {
    this.tagmanager.removeTags(tags);
  }
  
  public void removeTags(List<String> tags) {
    this.tagmanager.removeTags(tags);
  }
  
  public String toString() {
    StringBuffer a = new StringBuffer();
    a.append("[" + getClass().getSimpleName() + " ");
    a.append("griddensity=" + this.griddensity + " ");
    a.append("tags=" + this.tagmanager.toString() + "\n");
    for (JigSection s : this.sections)
      a.append(String.valueOf(s.toString()) + "\n"); 
    return a.toString();
  }
}
